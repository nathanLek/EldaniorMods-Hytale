package com.eldanior.system.luo;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRole;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.util.UUID;

/**
 * Plaques d'identification du Clan Luo (objets de scripts/familles/gen_plaques_luo.py) : une par rang, plus celle des
 * marchands agrees du Comptoir. Personnelles : LuoPlaqueSystem remet la sienne a chacun et fait disparaitre les autres.
 * Exception (2026-11-16) : le laissez-passer VISITEUR, vendu aux etrangers par les marchands Luo, valable 2 jours puis retire.
 *
 * Effets (lus ici par les systemes du territoire et des PNJ Luo) : porter sa plaque = entrer sur le territoire sans etre
 * attaque ; tenir en main celle d'un Noble ou au-dessus = les PNJ Luo saluent (Noble) ou s'inclinent (Seigneur, Doyen ; profondement
 * devant le Patriarche).
 */
public final class LuoPlaques {

    public static final String FAMILY = "luo";
    static final String PREFIX = "Eldanior_Plaque_Luo_";

    public enum Plaque {
        RECRUE("Recrue", "Recrue"),
        NOBLE("Noble", "Noble"),
        SEIGNEUR("Seigneur", "Seigneur"),
        DOYEN("Doyen", "Doyen"),
        PATRIARCHE("Patriarche", "Patriarche"),
        MARCHAND("Marchand", "Marchand agréé du Comptoir"),
        /** Laissez-passer des etrangers (2026-11-16) : vendu par les marchands Luo, valable VISITOR_MINUTES, non personnel. */
        VISITEUR("Visiteur", "Visiteur (laissez-passer)"),
        CLAN_SAMA("Clan_Sama", "Clan Sama"),
        CLAN_HANAZAKURA("Clan_Hanazakura", "Clan Hanazakura"),
        CLAN_KAGE("Clan_Kage", "Clan Kage"),
        CLAN_SETSUKA("Clan_Setsuka", "Clan Setsuka");

        /** Plaque des membres d'un clan de l'empire. */
        static Plaque member(LuoClans.Clan clan) {
            return switch (clan) {
                case SAMA -> CLAN_SAMA;
                case HANAZAKURA -> CLAN_HANAZAKURA;
                case KAGE -> CLAN_KAGE;
                case SETSUKA -> CLAN_SETSUKA;
            };
        }

        private final String itemId;
        private final String label;

        Plaque(String suffix, String label) {
            this.itemId = PREFIX + suffix;
            this.label = label;
        }

        public String itemId() { return itemId; }
        public String label() { return label; }

        /** Plaque personnelle (remise et reprise selon le rang) ; le laissez-passer, lui, s'achete et se garde. */
        public boolean personal() { return this != VISITEUR; }

        /** Tenue en main, cette plaque fait-elle saluer les PNJ Luo ? (Noble et au-dessus) */
        public boolean commandsBow() { return this == NOBLE || this == SEIGNEUR || this == DOYEN || this == PATRIARCHE; }

        /** Geste des PNJ devant cette plaque : salut (Noble), inclinaison (Seigneur, Doyen), inclinaison profonde (Patriarche). */
        public String bowGesture() {
            return this == PATRIARCHE ? "InclinaisonProfonde" : this == NOBLE ? "Salut" : "Inclinaison";
        }

        public static Plaque of(String itemId) {
            if (itemId == null || !itemId.startsWith(PREFIX)) return null;
            for (Plaque p : values()) if (p.itemId.equals(itemId)) return p;
            return null;
        }

        static Plaque of(FamilyRole role) {
            return switch (role) {
                case MEMBER, CHEVALIER, CHEVALIER_ROYAL -> RECRUE;
                case NOBLE -> NOBLE;
                case VICE -> SEIGNEUR;
                case ELDER -> DOYEN;
                case PATRIARCH -> PATRIARCHE;
            };
        }
    }

    private LuoPlaques() {}

    /**
     * Plaque a laquelle le joueur a droit : celle de son rang s'il est du Clan Luo ; Doyen s'il est le Patriarche d'un des
     * quatre clans, sinon la plaque de son clan ; celle de marchand s'il est agree ; sinon null.
     */
    public static Plaque owned(PlayerLevelData data, UUID uuid) {
        if (data != null && FAMILY.equals(data.getNobleFamilyId())) {
            FamilyRole role = FamilyRole.of(data.getStatus());
            return Plaque.of(role == null ? FamilyRole.MEMBER : role);
        }
        LuoClans.Clan clan = data != null ? LuoClans.of(data.getNobleFamilyId()) : null;
        if (clan != null) return data.isPatriarch() ? Plaque.DOYEN : Plaque.member(clan);
        return FamilyManager.isMerchant(FAMILY, uuid) ? Plaque.MARCHAND : null;
    }

    /** Plaque tenue en main, ou null. */
    public static Plaque held(Player player) {
        ItemStack hand = player.getInventory().getActiveHotbarItem();
        return hand == null || hand.isEmpty() ? null : Plaque.of(hand.getItemId());
    }

    /** Le joueur porte-t-il cette plaque (sac, reserve ou barre d'action) ? */
    public static boolean carries(Player player, Plaque plaque) {
        if (plaque == null) return false;
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        for (short slot = 0; slot < bag.getCapacity(); slot++) {
            ItemStack stack = bag.getItemStack(slot);
            if (stack != null && !stack.isEmpty() && plaque.itemId.equals(stack.getItemId())) return true;
        }
        return false;
    }

    /** Laisser passer ce joueur sur le territoire Luo : il porte la plaque a laquelle il a droit, ou un laissez-passer valable. */
    public static boolean admitted(Player player, PlayerLevelData data, UUID uuid) {
        if (com.eldanior.system.classes.observateur.ObservateurManager.isObservateur(data)) return true;   // Observateur : s'infiltre sans plaque
        Plaque own = owned(data, uuid);
        return own != null && carries(player, own) || carriesPass(player);
    }

    // ===== Laissez-passer (2026-11-16) =====

    /** Duree du laissez-passer : 2 jours reels (= MaxDurability de l'objet, en minutes ; scripts/familles/gen_plaques_luo.py). */
    public static final int VISITOR_MINUTES = 2 * 24 * 60;
    /** Fin de validite (ms), posee dans les metadonnees de l'objet la premiere fois qu'il est vu dans un sac. */
    static final String PASS_END = "Eldanior_LaissezPasserFin";

    /** Fin de validite du laissez-passer (0 : pas encore date). */
    static long passEnd(ItemStack stack) {
        org.bson.BsonDocument md = stack != null ? stack.getMetadata() : null;
        return md != null && md.isInt64(PASS_END) ? md.getInt64(PASS_END).getValue() : 0;
    }

    /** Le joueur porte-t-il un laissez-passer encore valable ? */
    public static boolean carriesPass(Player player) {
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        long now = System.currentTimeMillis();
        for (short slot = 0; slot < bag.getCapacity(); slot++) {
            ItemStack stack = bag.getItemStack(slot);
            if (stack == null || stack.isEmpty() || !Plaque.VISITEUR.itemId.equals(stack.getItemId())) continue;
            long end = passEnd(stack);
            if (end == 0 || end > now) return true;
        }
        return false;
    }
}
