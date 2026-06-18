package com.eldanior.system.territory;

import com.eldanior.system.config.TaxConfig;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.titles.nobility.family.FamilyManager;

import java.util.*;

public class ParcelEconomyManager {

    public static final float TAX_RATE = TaxConfig.TRANSACTION_TAX_RATE;
    private static long lastTaxCollection = 0;
    private static final long TAX_INTERVAL = 7L * 24 * 60 * 60 * 1000; // 7 jours

    public static void init() {
        lastTaxCollection = System.currentTimeMillis();
        System.out.println("[Eldanior] ParcelEconomyManager initialise. Taxe: " + (int)(TAX_RATE * 100) + "%");
    }

    // ==================== TRANSACTION AVEC TAXE ====================

    /**
     * Calcule le montant net apres taxe.
     * @return [netAmount, taxAmount]
     */
    public static long[] calculateTax(long amount) {
        long tax = (long) (amount * TAX_RATE);
        long net = amount - tax;
        return new long[]{net, tax};
    }

    /**
     * Distribue la taxe automatiquement dans la hierarchie.
     * Taxe -> Ville (30%) -> Territoire (70% de la ville) -> Royaume (30% du territoire)
     */
    public static void distributeTax(String parcelId, long taxAmount) {
        ParcelData parcel = ParcelManager.get(parcelId);
        if (parcel == null || taxAmount <= 0) return;

        // Trouver la ville parente
        ParcelData city = findParentOfType(parcel, ParcelType.CITY);
        if (city != null) {
            // Repartition FIXE sur le total de la taxe :
            // Ville: 15%, Duc: 20%, Marquis: 30%, Royaume: 35% (= 100%)
            // S'adapte si certains niveaux n'existent pas

            // Remonter la chaine : ville -> territoires -> royaume
            List<ParcelData> chain = new ArrayList<>();
            String pid = city.getParentId();
            int maxD = 5;
            while (pid != null && maxD-- > 0) {
                ParcelData pp = ParcelManager.get(pid);
                if (pp == null) break;
                chain.add(pp);
                if (pp.getType() == ParcelType.KINGDOM) break;
                pid = pp.getParentId();
            }

            // Parts fixes : [ville, duc, marquis, royaume] = [15, 20, 30, 35]
            // Si pas de duc : [ville, marquis, royaume] = [15, 25, 60]
            // Si pas de territoire : [ville, royaume] = [15, 85]
            ParcelData kingdom = null;
            List<ParcelData> territories = new ArrayList<>();
            for (ParcelData pp : chain) {
                if (pp.getType() == ParcelType.KINGDOM) kingdom = pp;
                else territories.add(pp);
            }

            // Ville: part fixe (TaxConfig.CITY_SHARE)
            long cityShare = (long) (taxAmount * TaxConfig.CITY_SHARE);
            city.addTreasury(cityShare);
            long distributed = cityShare;
            System.out.println("[Economy] " + cityShare + " Or (" + (int)(TaxConfig.CITY_SHARE * 100) + "%) -> Ville " + city.getName());

            if (territories.size() == 2) {
                // Duc (premier), Marquis (deuxieme)
                long ducShare = (long) (taxAmount * TaxConfig.DUCHY_SHARE);
                long marquisShare = (long) (taxAmount * TaxConfig.MARQUISATE_SHARE);
                territories.get(0).addTreasury(ducShare);
                territories.get(1).addTreasury(marquisShare);
                distributed += ducShare + marquisShare;
                System.out.println("[Economy] " + ducShare + " Or (" + (int)(TaxConfig.DUCHY_SHARE * 100) + "%) -> " + territories.get(0).getName());
                System.out.println("[Economy] " + marquisShare + " Or (" + (int)(TaxConfig.MARQUISATE_SHARE * 100) + "%) -> " + territories.get(1).getName());
            } else if (territories.size() == 1) {
                // Un seul territoire intermediaire
                long terrShare = (long) (taxAmount * TaxConfig.SINGLE_TERRITORY_SHARE);
                territories.get(0).addTreasury(terrShare);
                distributed += terrShare;
                System.out.println("[Economy] " + terrShare + " Or (" + (int)(TaxConfig.SINGLE_TERRITORY_SHARE * 100) + "%) -> " + territories.get(0).getName());
            }

            // Royaume: tout le reste
            if (kingdom != null) {
                long kingdomShare = taxAmount - distributed;
                if (kingdomShare > 0) {
                    kingdom.addTreasury(kingdomShare);
                    System.out.println("[Economy] " + kingdomShare + " Or (reste) -> Royaume " + kingdom.getName());
                }
            }
            ParcelManager.save();
        }
    }

    // ==================== IMPOTS HEBDOMADAIRES ====================

    /**
     * Collecte les impots hebdomadaires : chaque parcelle de type TERRITORY,
     * GRAND_TERRITORY ou KINGDOM preleve un pourcentage (defini dans TaxConfig)
     * de la tresorerie de ses enfants directs.
     *
     * Appele periodiquement par PersistenceManager (toutes les 5 minutes),
     * mais ne s'execute reellement que si 7 jours se sont ecoules depuis
     * la derniere collecte de chaque parcelle (via canCollectTax()).
     */
    public static void collectWeeklyTaxes() {
        long now = System.currentTimeMillis();
        boolean changed = false;

        for (ParcelData collector : ParcelManager.getAll()) {
            // Seuls TERRITORY, GRAND_TERRITORY, KINGDOM collectent
            double rate = TaxConfig.getCollectionRate(collector.getType());
            if (rate <= 0.0) continue;

            // Cooldown de 7 jours par parcelle
            if (!collector.canCollectTax()) continue;

            // Collecter depuis les enfants directs
            List<String> childrenIds = ParcelManager.getChildrenOf(collector.getId());
            if (childrenIds.isEmpty()) continue;

            long totalCollected = 0;

            for (String childId : childrenIds) {
                ParcelData child = ParcelManager.get(childId);
                if (child == null) continue;

                long childTreasury = child.getTreasury();
                if (childTreasury <= 0) continue;

                long amount = (long) (childTreasury * rate);
                if (amount <= 0) continue;

                child.addTreasury(-amount);
                totalCollected += amount;

                System.out.println("[Economy] Impot hebdo: " + amount + " Or preleve de "
                        + child.getName() + " (" + child.getType().getLabel() + ")"
                        + " -> " + collector.getName() + " (" + collector.getType().getLabel() + ")"
                        + " [" + (int)(rate * 100) + "%]");
            }

            if (totalCollected > 0) {
                collector.addTreasury(totalCollected);
                collector.setLastTaxCollection(now);
                collector.setLastTaxAmount(totalCollected);
                changed = true;

                System.out.println("[Economy] " + collector.getName()
                        + " a collecte " + totalCollected + " Or d'impots hebdomadaires");

                // Redistribuer vers la famille/guilde associee si applicable
                sendToFamilyOrGuild(collector, totalCollected);
            } else {
                // Marquer la collecte meme si rien a prelever (eviter retry inutile)
                collector.setLastTaxCollection(now);
                collector.setLastTaxAmount(0);
                changed = true;
            }
        }

        if (changed) {
            ParcelManager.save();
        }
    }

    /**
     * Envoie les fonds de la parcelle a la famille ou guilde associee.
     */
    private static void sendToFamilyOrGuild(ParcelData parcel, long amount) {
        if (amount <= 0) return;

        // Guilde
        String guildId = parcel.getGuildId();
        if (guildId != null && !guildId.isEmpty()) {
            try {
                Guild guild = GuildManager.get(guildId);
                if (guild != null) {
                    guild.addTreasury(amount);
                    parcel.addTreasury(-amount);
                    System.out.println("[Economy] " + parcel.getName() + " -> " + amount + " Or -> Guilde " + guild.getName());
                }
            } catch (Exception e) {
                System.err.println("[Economy] Erreur guilde: " + e.getMessage());
            }
            return;
        }

        // Famille noble
        String familyId = parcel.getFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            try {
                var runtimeData = FamilyManager.getRuntimeData(familyId);
                if (runtimeData != null) {
                    runtimeData.addTreasury(amount);
                    parcel.addTreasury(-amount);
                    System.out.println("[Economy] " + parcel.getName() + " -> " + amount + " Or -> Famille " + familyId);
                }
            } catch (Exception e) {
                System.err.println("[Economy] Erreur famille: " + e.getMessage());
            }
        }
    }

    /**
     * Remonte la hierarchie en distribuant l'argent.
     * Chaque niveau garde 70% et envoie 30% au parent.
     * Si le parent est un Royaume, il recoit tout le reste.
     */

    // ==================== UTILS ====================

    /**
     * Remonte la hierarchie pour trouver un parent d'un type donne.
     */
    public static ParcelData findParentOfType(ParcelData parcel, ParcelType type) {
        String parentId = parcel.getParentId();
        int maxDepth = 5;
        while (parentId != null && maxDepth-- > 0) {
            ParcelData parent = ParcelManager.get(parentId);
            if (parent == null) return null;
            if (parent.getType() == type) return parent;
            parentId = parent.getParentId();
        }
        return null;
    }
}