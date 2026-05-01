package com.eldanior.system.gui.tabs;

import com.eldanior.system.territory.*;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class TerritoiresTab {

    public static final int MAX_TERR_SLOTS = 4;
    public static final int MAX_DETAIL_CHILDREN = 6;

    private static final List<String> cachedTerrIds = new ArrayList<>();
    private static int selectedIndex = -1;
    private static String selectedTerrId = null;

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        String familyId = "";
        String guildId = "";
        boolean isAdmin = false;

        try {
            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data != null) {
                familyId = data.getNobleFamilyId() != null ? data.getNobleFamilyId() : "";
                guildId = data.getGuildId() != null ? data.getGuildId() : "";
            }
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) isAdmin = player.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        } catch (Exception e) { EldaniorLogger.error("TerritoiresTab", e); }

        List<ParcelData> territories = new ArrayList<>();

        if (isAdmin) {
            for (ParcelData p : ParcelManager.getAll()) {
                if (p.getType() == ParcelType.KINGDOM || p.getType() == ParcelType.TERRITORY || p.getType() == ParcelType.CITY) {
                    territories.add(p);
                }
            }
        } else {
            if (!familyId.isEmpty()) {
                for (ParcelData p : ParcelManager.getByFamily(familyId)) {
                    if (p.getType() == ParcelType.KINGDOM || p.getType() == ParcelType.TERRITORY || p.getType() == ParcelType.CITY) {
                        territories.add(p);
                    }
                }
            }
            if (!guildId.isEmpty()) {
                for (ParcelData p : ParcelManager.getAll()) {
                    if (guildId.equals(p.getGuildId()) && !territories.contains(p)) {
                        territories.add(p);
                    }
                }
            }
        }

        territories.sort(Comparator.comparingInt(p -> p.getType().ordinal()));

        cachedTerrIds.clear();
        ui.set("#TerrCount.Text", territories.size() + " TERRITOIRE(S)" + (isAdmin ? " - Admin" : ""));

        for (int i = 0; i < MAX_TERR_SLOTS; i++) {
            if (i < territories.size()) {
                ParcelData p = territories.get(i);
                cachedTerrIds.add(p.getId());
                ui.set("#TerrSlot" + i + ".Visible", true);

                String typeColor = switch (p.getType()) {
                    case KINGDOM -> "#FFD700";
                    case TERRITORY -> "#3498DB";
                    case CITY -> "#2ECC71";
                    default -> "#8899aa";
                };
                ui.set("#TerrType" + i + ".Text", "[" + p.getType().getLabel() + "]");
                ui.set("#TerrType" + i + ".Style.TextColor", typeColor);
                ui.set("#TerrName" + i + ".Text", p.getName());
                ui.set("#TerrTreasury" + i + ".Text", p.getTreasury() + " Or");

                String locationText = "";
                if (p.getType() == ParcelType.CITY) {
                    ParcelData pt = ParcelEconomyManager.findParentOfType(p, ParcelType.TERRITORY);
                    if (pt != null) locationText = "Dans : " + pt.getName();
                } else if (p.getType() == ParcelType.TERRITORY) {
                    ParcelData pk = ParcelEconomyManager.findParentOfType(p, ParcelType.KINGDOM);
                    if (pk != null) locationText = "Dans : " + pk.getName();
                }
                ui.set("#TerrLocation" + i + ".Text", locationText);
                ui.set("#TerrFamily" + i + ".Text", !p.getFamilyId().isEmpty() ? "Famille : " + p.getFamilyId() : "");

                // Stats boxes
                int[] counts = countAllChildren(p);
                ui.set("#TerrBoxTerr" + i + ".Visible", counts[0] > 0);
                ui.set("#TerrCountTerr" + i + ".Text", String.valueOf(counts[0]));
                ui.set("#TerrBoxCity" + i + ".Visible", counts[1] > 0);
                ui.set("#TerrCountCity" + i + ".Text", String.valueOf(counts[1]));
                ui.set("#TerrBoxHousing" + i + ".Visible", counts[2] > 0);
                ui.set("#TerrCountHousing" + i + ".Text", String.valueOf(counts[2]));
                ui.set("#TerrBoxPlot" + i + ".Visible", counts[3] > 0);
                ui.set("#TerrCountPlot" + i + ".Text", String.valueOf(counts[3]));
                ui.set("#TerrPopulation" + i + ".Text", String.valueOf(countPopulation(p)));
                ui.set("#TerrProt" + i + ".Text", p.isProtectedByDefault() ? "Protege" : "Ouvert");
                ui.set("#TerrProt" + i + ".Style.TextColor", p.isProtectedByDefault() ? "#4CAF50" : "#cc4444");

                // Highlight selected
                boolean sel = (i == selectedIndex);
                ui.set("#TerrSlot" + i + ".Style.Default.Background", sel ? "#1a2a3a" : "#0d1520");
            } else {
                cachedTerrIds.add("");
                ui.set("#TerrSlot" + i + ".Visible", false);
            }
        }

        // Detail panel
        populateDetail(ui, isAdmin, ref, store);

        ui.set("#TerrInfoLabel.Text", territories.isEmpty() ? "Aucun territoire sous votre gestion." : "");
    }

    // === PANNEAU DETAIL ===
    private static void populateDetail(UICommandBuilder ui, boolean isAdmin, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (selectedTerrId == null) {
            ui.set("#TerrDetail.Visible", false);
            return;
        }

        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) {
            ui.set("#TerrDetail.Visible", false);
            selectedTerrId = null;
            selectedIndex = -1;
            return;
        }

        ui.set("#TerrDetail.Visible", true);

        // Header
        ui.set("#TDetName.Text", p.getName());
        String typeColor = switch (p.getType()) {
            case KINGDOM -> "#FFD700";
            case TERRITORY -> "#3498DB";
            case CITY -> "#2ECC71";
            default -> "#8899aa";
        };
        ui.set("#TDetName.Style.TextColor", typeColor);
        ui.set("#TDetType.Text", p.getType().getLabel());

        // Infos
        ui.set("#TDetTreasury.Text", "Tresorerie : " + p.getTreasury() + " Or");
        ui.set("#TDetFamily.Text", !p.getFamilyId().isEmpty() ? "Famille : " + p.getFamilyId() : "Famille : Aucune");
        ui.set("#TDetGuild.Text", !p.getGuildId().isEmpty() ? "Guilde : " + p.getGuildId() : "");
        ui.set("#TDetProt.Text", p.isProtectedByDefault() ? "ZONE PROTEGEE" : "ZONE OUVERTE");
        ui.set("#TDetProt.Style.TextColor", p.isProtectedByDefault() ? "#4CAF50" : "#cc4444");

        int sX = p.getMaxX() - p.getMinX() + 1;
        int sZ = p.getMaxZ() - p.getMinZ() + 1;
        ui.set("#TDetSize.Text", "Taille : " + sX + " x " + sZ + " blocs");

        int population = countPopulation(p);
        ui.set("#TDetPopulation.Text", "Population : " + population + " habitant(s)");

        // PvP
        ui.set("#TDetPvp.Text", "PvP : " + (p.isPvpEnabled() ? "ACTIVE" : "DESACTIVE"));
        ui.set("#TDetPvp.Style.TextColor", p.isPvpEnabled() ? "#cc4444" : "#4CAF50");

        // Infos economiques
        ui.set("#TDetTaxRate.Text", "Taxe : 12% sur transactions");
        if (p.getLastTaxCollection() > 0) {
            long nextTax = (p.getLastTaxCollection() + 7L * 24 * 60 * 60 * 1000) - System.currentTimeMillis();
            if (nextTax > 0) {
                long days = nextTax / (24 * 60 * 60 * 1000);
                long hours = (nextTax % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
                ui.set("#TDetNextTax.Text", "Prochain impot : " + days + "j " + hours + "h");
            } else {
                ui.set("#TDetNextTax.Text", "Impots reclamables !");
                ui.set("#TDetNextTax.Style.TextColor", "#4CAF50");
            }
        } else {
            ui.set("#TDetNextTax.Text", "Aucun impot collecte");
        }
        if (p.getLastTaxAmount() > 0) {
            ui.set("#TDetLastTax.Text", "Dernier impot : " + p.getLastTaxAmount() + " Or");
        } else {
            ui.set("#TDetLastTax.Text", "");
        }

        // Liste des sous-zones directes
        List<String> childIds = p.getChildIds();
        for (int i = 0; i < MAX_DETAIL_CHILDREN; i++) {
            if (i < childIds.size()) {
                ParcelData child = ParcelManager.get(childIds.get(i));
                if (child == null) { ui.set("#TDetChild" + i + ".Visible", false); continue; }
                ui.set("#TDetChild" + i + ".Visible", true);
                ui.set("#TDetChildType" + i + ".Text", "[" + child.getType().getLabel() + "]");
                String childColor = switch (child.getType()) {
                    case TERRITORY -> "#3498DB";
                    case CITY -> "#2ECC71";
                    case PLOT -> "#aabbcc";
                    case HOUSING -> "#9B59B6";
                    default -> "#8899aa";
                };
                ui.set("#TDetChildType" + i + ".Style.TextColor", childColor);
                ui.set("#TDetChildName" + i + ".Text", child.getName());
                ui.set("#TDetChildOwner" + i + ".Text", child.getOwnerName().isEmpty() ? "-" : child.getOwnerName());
                ui.set("#TDetChildTreasury" + i + ".Text", child.getTreasury() > 0 ? child.getTreasury() + " Or" : "");
            } else {
                ui.set("#TDetChild" + i + ".Visible", false);
            }
        }

        // Boutons gestion (proprio + admin)
        ui.set("#TDetBtnPvp.Visible", p.getType() == ParcelType.CITY || isAdmin);
        ui.set("#TDetBtnPvp.Text", p.isPvpEnabled() ? "DESACTIVER PVP" : "ACTIVER PVP");
        ui.set("#TDetBtnTax.Visible", p.getType() != ParcelType.CITY && p.canCollectTax() && !p.getChildIds().isEmpty());
        ui.set("#TDetBtnTransfer.Visible", p.canTransferTreasury() && p.getTreasury() > 0
                && (!p.getFamilyId().isEmpty() || !p.getGuildId().isEmpty()));
        if (p.canTransferTreasury() && p.getTreasury() > 0) {
            ui.set("#TDetBtnTransfer.Text", "TRANSFERER " + (p.getTreasury() / 2) + " Or");
        }

        // Boutons admin
        ui.set("#TDetBtnProt.Visible", isAdmin);
        ui.set("#TDetBtnFamily.Visible", isAdmin);
        ui.set("#TDetBtnGuild.Visible", isAdmin && p.getType() == ParcelType.CITY);
        ui.set("#TDetBtnInvasion.Visible", isAdmin);
        ui.set("#TDetBtnDel.Visible", isAdmin);

        // Boutons DECRETS (Roi donne des decrets depuis le Royaume)
        // Marquis: depuis son territoire → 1 Comte + 2 Barons
        // Duc: depuis son territoire → 1 Baron
        boolean isKingdom = p.getType() == ParcelType.KINGDOM;
        boolean isTerritory = p.getType() == ParcelType.TERRITORY;

        // Determiner le rang du joueur qui gere ce territoire
        String playerRank = "";
        try {
            PlayerLevelData playerData = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (playerData != null) playerRank = playerData.getNobilityRank() != null ? playerData.getNobilityRank() : "";
        } catch (Exception e) { EldaniorLogger.error("TerritoiresTab", e); }

        boolean isRoi = "ROI".equals(playerRank) || isAdmin;
        boolean isMarquis = "MARQUIS".equals(playerRank);
        boolean isDuc = "DUC".equals(playerRank);

        // Roi → decrets depuis le Royaume
        ui.set("#TDetBtnDecretMarquis.Visible", isKingdom && isRoi);
        ui.set("#TDetBtnDecretDuc.Visible", isKingdom && isRoi);
        ui.set("#TDetBtnDecretComte.Visible", isKingdom && isRoi);
        ui.set("#TDetBtnDecretBaron.Visible", isKingdom && isRoi);

        // Marquis → decrets depuis son Territoire
        if (isTerritory && isMarquis) {
            ui.set("#TDetBtnDecretComte.Visible", true);
            ui.set("#TDetBtnDecretBaron.Visible", true);
        }

        // Duc → decret Baron depuis son Territoire
        if (isTerritory && isDuc) {
            ui.set("#TDetBtnDecretBaron.Visible", true);
        }

        // Afficher les slots restants et desactiver si limite atteinte
        var NM = com.eldanior.system.titles.nobility.NobilityManager.class;
        var NR = com.eldanior.system.titles.nobility.NobilityRank.class;

        int remMarquis = com.eldanior.system.titles.nobility.NobilityManager.getRemainingSlots(
                com.eldanior.system.titles.nobility.NobilityRank.MARQUIS);
        int remDuc = com.eldanior.system.titles.nobility.NobilityManager.getRemainingSlots(
                com.eldanior.system.titles.nobility.NobilityRank.DUC);
        int remComte = com.eldanior.system.titles.nobility.NobilityManager.getRemainingSlots(
                com.eldanior.system.titles.nobility.NobilityRank.COMTE);
        int remBaron = com.eldanior.system.titles.nobility.NobilityManager.getRemainingSlots(
                com.eldanior.system.titles.nobility.NobilityRank.BARON);

        ui.set("#TDetBtnDecretMarquis.Text", "DECRET MARQUIS (" + remMarquis + ")");
        ui.set("#TDetBtnDecretDuc.Text", "DECRET DUC (" + remDuc + ")");
        ui.set("#TDetBtnDecretComte.Text", "DECRET COMTE (" + remComte + ")");
        ui.set("#TDetBtnDecretBaron.Text", "DECRET BARON (" + remBaron + ")");

        // Desactiver si limite atteinte (sauf admin)
        if (!isAdmin) {
            if (remMarquis <= 0) ui.set("#TDetBtnDecretMarquis.Visible", false);
            if (remDuc <= 0) ui.set("#TDetBtnDecretDuc.Visible", false);
            if (remComte <= 0) ui.set("#TDetBtnDecretComte.Visible", false);
            if (remBaron <= 0) ui.set("#TDetBtnDecretBaron.Visible", false);
        }
    }

    // === HANDLERS ===

    public static boolean handleSelect(int index) {
        if (index < 0 || index >= cachedTerrIds.size()) return false;
        String id = cachedTerrIds.get(index);
        if (id == null || id.isEmpty()) return false;
        if (selectedIndex == index) {
            selectedIndex = -1;
            selectedTerrId = null;
        } else {
            selectedIndex = index;
            selectedTerrId = id;
        }
        return true;
    }

    public static boolean handleTogglePvp() {
        if (selectedTerrId == null) return false;
        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) return false;
        p.setPvpEnabled(!p.isPvpEnabled());
        ParcelManager.save();
        return true;
    }

    public static boolean handleCollectTax() {
        if (selectedTerrId == null) return false;
        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) return false;
        if (!p.canCollectTax()) return false;

        // Impots : chaque niveau prend de ses enfants directs
        // Duc prend 40% des villes, Marquis prend 60% du Duc, Royaume prend 80% du Marquis
        // Sur ville 20000 : Duc prend 8000 → Marquis prend 4800 → Royaume prend 3840
        // Resultat final : Ville 12000, Duc 3200, Marquis 960, Royaume 3840
        // NON — on veut Royaume > Marquis > Duc > Ville
        // Duc prend 90% de la ville, Marquis 85% du Duc, Royaume 80% du Marquis
        // Ville 20000 → Duc prend 18000 (reste ville 2000) → Marquis prend 15300 (reste Duc 2700) → Royaume prend 12240 (reste Marquis 3060)
        // = Royaume 12240 > Marquis 3060 > Duc 2700 > Ville 2000 ✓
        if (p.getType() == ParcelType.CITY) return false;

        double taxRate = switch (p.getType()) {
            case KINGDOM -> 0.57;   // Royaume prend 57% du Marquis → ~8000 sur 20000 de base
            case TERRITORY -> {
                ParcelData parent = p.getParentId() != null ? ParcelManager.get(p.getParentId()) : null;
                yield (parent != null && parent.getType() == ParcelType.KINGDOM) ? 0.80 : 0.875;
                // Marquis prend 80% du Duc, Duc prend 87.5% de la ville
            }
            default -> 0.0;
        };

        if (taxRate <= 0) return false;

        long totalCollected = 0;
        for (String childId : p.getChildIds()) {
            ParcelData child = ParcelManager.get(childId);
            if (child == null || child.getTreasury() <= 0) continue;
            long amount = (long) (child.getTreasury() * taxRate);
            child.addTreasury(-amount);
            totalCollected += amount;
        }
        p.addTreasury(totalCollected);
        p.setLastTaxCollection(System.currentTimeMillis());
        p.setLastTaxAmount(totalCollected);
        ParcelManager.save();
        return true;
    }

    public static boolean handleTransferTreasury() {
        if (selectedTerrId == null) return false;
        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) return false;
        if (!p.canTransferTreasury()) return false;
        if (p.getTreasury() <= 0) return false;

        long transferAmount = p.getTreasury() / 2; // 50%
        if (transferAmount <= 0) return false;

        boolean transferred = false;

        // Guilde
        if (!p.getGuildId().isEmpty()) {
            try {
                com.eldanior.system.guild.Guild guild = com.eldanior.system.guild.GuildManager.get(p.getGuildId());
                if (guild != null) {
                    guild.addTreasury(transferAmount);
                    p.addTreasury(-transferAmount);
                    transferred = true;
                }
            } catch (Exception e) { EldaniorLogger.error("TerritoiresTab", e); }
        }

        // Famille
        if (!transferred && !p.getFamilyId().isEmpty()) {
            try {
                var runtimeData = com.eldanior.system.titles.nobility.family.FamilyManager.getRuntimeData(p.getFamilyId());
                if (runtimeData != null) {
                    runtimeData.addTreasury(transferAmount);
                    p.addTreasury(-transferAmount);
                    transferred = true;
                }
            } catch (Exception e) { EldaniorLogger.error("TerritoiresTab", e); }
        }

        if (transferred) {
            p.setLastTreasuryTransfer(System.currentTimeMillis());
            ParcelManager.save();
        }
        return transferred;
    }

    public static boolean handleGiveDecret(String rank, Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return false;

            boolean isAdmin = player.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

            // Verifier la limite (sauf admin)
            if (!isAdmin) {
                com.eldanior.system.titles.nobility.NobilityRank nRank =
                        com.eldanior.system.titles.nobility.NobilityRank.fromString(rank);
                if (nRank != null && !com.eldanior.system.titles.nobility.NobilityManager.canKingPromote(nRank)) {
                    player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                            "§cLimite atteinte pour le rang " + rank + " !"));
                    return false;
                }
            }

            String itemId = switch (rank) {
                case "MARQUIS" -> "Decret_Marquis";
                case "DUC" -> "Decret_Duc";
                case "COMTE" -> "Decret_Comte";
                case "BARON" -> "Decret_Baron";
                case "CHEVALIER" -> "Decret_Chevalier";
                default -> null;
            };
            if (itemId == null) return false;

            var result = player.getInventory().getHotbar().addItemStack(
                    new ItemStack(itemId, 1));
            if (result.succeeded()) {
                // Comptabiliser la promotion (sauf admin)
                if (!isAdmin) {
                    com.eldanior.system.titles.nobility.NobilityRank nRank =
                            com.eldanior.system.titles.nobility.NobilityRank.fromString(rank);
                    if (nRank != null) {
                        com.eldanior.system.titles.nobility.NobilityManager.recordKingPromotion(nRank);
                    }
                }
                player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                        "§6§lDecret Royal recu ! §7Donnez-le a un joueur pour le nommer §e" + rank));
                return true;
            } else {
                player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§cInventaire plein !"));
            }
        } catch (Exception e) {
            System.err.println("[Territoire] Erreur decret: " + e.getMessage());
        }
        return false;
    }

    public static boolean handleToggleProtection() {
        if (selectedTerrId == null) return false;
        ParcelData p = ParcelManager.get(selectedTerrId);
        if (p == null) return false;
        p.setProtectedByDefault(!p.isProtectedByDefault());
        ParcelManager.save();
        return true;
    }

    public static boolean handleDelete() {
        if (selectedTerrId == null) return false;
        ParcelManager.deleteParcel(selectedTerrId);
        selectedTerrId = null;
        selectedIndex = -1;
        return true;
    }

    // === UTILS ===

    private static void collectCitiesRecursive(String parentId, List<ParcelData> cities, int depth) {
        if (depth > 5) return;
        for (String childId : ParcelManager.getChildrenOf(parentId)) {
            ParcelData child = ParcelManager.get(childId);
            if (child == null) continue;
            if (child.getType() == ParcelType.CITY) cities.add(child);
            collectCitiesRecursive(childId, cities, depth + 1);
        }
    }

    private static int[] countAllChildren(ParcelData parent) {
        int[] counts = new int[4];
        countChildrenRecursive(parent.getId(), counts, 0);
        return counts;
    }

    private static void countChildrenRecursive(String parentId, int[] counts, int depth) {
        if (depth > 5) return;
        for (String childId : ParcelManager.getChildrenOf(parentId)) {
            ParcelData child = ParcelManager.get(childId);
            if (child == null) continue;
            switch (child.getType()) {
                case TERRITORY -> counts[0]++;
                case CITY -> counts[1]++;
                case HOUSING, ROOM -> counts[2]++;
                case PLOT, FARM -> counts[3]++;
                default -> {}
            }
            countChildrenRecursive(childId, counts, depth + 1);
        }
    }

    private static int countPopulation(ParcelData parent) {
        Set<UUID> owners = new HashSet<>();
        countOwnersRecursive(parent.getId(), owners, 0);
        return owners.size();
    }

    private static void countOwnersRecursive(String parentId, Set<UUID> owners, int depth) {
        if (depth > 5) return;
        for (String childId : ParcelManager.getChildrenOf(parentId)) {
            ParcelData child = ParcelManager.get(childId);
            if (child == null) continue;
            if (child.getOwnerUUID() != null) owners.add(child.getOwnerUUID());
            if (child.getRenterUUID() != null) owners.add(child.getRenterUUID());
            countOwnersRecursive(childId, owners, depth + 1);
        }
    }
}
