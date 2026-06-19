package com.eldanior.system.dungeon;

import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;

import java.util.UUID;

/**
 * Utilitaire de scaling de difficulte pour les donjons.
 * Les mobs de donjon voient leurs HP et XP scales en fonction
 * du nombre de joueurs dans le groupe du tueur.
 *
 * Formule : multiplicateur = 1.0 + (nbJoueursGroupe - 1) * 0.35
 *   - Solo (1 joueur)  : x1.00
 *   - Duo  (2 joueurs) : x1.35
 *   - Trio (3 joueurs) : x1.70
 *   - Quad (4 joueurs) : x2.05
 *   - Full (5 joueurs) : x2.40
 */
public final class DungeonScaling {

    private static final double SCALING_PER_EXTRA_PLAYER = 0.35;

    private DungeonScaling() {}

    /**
     * Retourne le nombre de joueurs dans le groupe du joueur,
     * ou 1 s'il n'est dans aucun groupe.
     */
    public static int getGroupSize(UUID playerUUID) {
        if (playerUUID == null) return 1;
        Party party = PartyManager.getParty(playerUUID);
        return (party != null) ? party.getSize() : 1;
    }

    /**
     * Calcule le multiplicateur de scaling pour un groupe de taille donnee.
     */
    public static double getScalingMultiplier(int groupSize) {
        if (groupSize <= 1) return 1.0;
        return 1.0 + (groupSize - 1) * SCALING_PER_EXTRA_PLAYER;
    }

    /**
     * Calcule le multiplicateur de scaling pour le groupe d'un joueur.
     */
    public static double getScalingMultiplier(UUID playerUUID) {
        return getScalingMultiplier(getGroupSize(playerUUID));
    }

    /**
     * Applique le scaling d'XP pour un kill en donjon.
     * L'XP est augmentee proportionnellement au nombre de joueurs
     * pour compenser la difficulte accrue.
     */
    public static int scaleXP(int baseXP, UUID killerUUID) {
        if (!isInDungeon(killerUUID)) return baseXP;
        double multiplier = getScalingMultiplier(killerUUID);
        return (int) Math.round(baseXP * multiplier);
    }

    /**
     * Verifie si un joueur se trouve actuellement dans une parcelle de type DUNGEON.
     * Utilise le PlayerPositionTracker pour la position.
     */
    public static boolean isInDungeon(UUID playerUUID) {
        if (playerUUID == null) return false;
        try {
            org.joml.Vector3d pos = com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(playerUUID);
            if (pos == null) return false;

            com.hypixel.hytale.server.core.universe.PlayerRef pRef =
                    com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(playerUUID);
            if (pRef == null) return false;

            var entityRef = pRef.getReference();
            if (entityRef == null) return false;

            var store = entityRef.getStore();
            var player = store.getComponent(entityRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (player == null || player.getWorld() == null) return false;

            String worldName = player.getWorld().getName();
            ParcelData parcel = ParcelManager.getParcelAt(worldName, pos.x, pos.y, pos.z);
            return parcel != null && parcel.getType() == ParcelType.DUNGEON;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retourne le rang du donjon dans lequel se trouve le joueur, ou "E" par defaut.
     */
    public static String getDungeonRank(UUID playerUUID) {
        if (playerUUID == null) return "E";
        try {
            org.joml.Vector3d pos = com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(playerUUID);
            if (pos == null) return "E";

            com.hypixel.hytale.server.core.universe.PlayerRef pRef =
                    com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(playerUUID);
            if (pRef == null) return "E";

            var entityRef = pRef.getReference();
            if (entityRef == null) return "E";

            var store = entityRef.getStore();
            var player = store.getComponent(entityRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (player == null || player.getWorld() == null) return "E";

            String worldName = player.getWorld().getName();
            ParcelData parcel = ParcelManager.getParcelAt(worldName, pos.x, pos.y, pos.z);
            if (parcel != null && parcel.getType() == ParcelType.DUNGEON) {
                return parcel.getDungeonRank();
            }
        } catch (Exception e) {
            // Silently fail
        }
        return "E";
    }

    /**
     * Retourne le tier du donjon (1-4) a partir du rang.
     *   E/D = Tier 1, C/B = Tier 2, A = Tier 3, S = Tier 4
     */
    public static int getTierFromRank(String rank) {
        if (rank == null) return 1;
        return switch (rank.toUpperCase()) {
            case "E", "D" -> 1;
            case "C", "B" -> 2;
            case "A" -> 3;
            case "S" -> 4;
            default -> 1;
        };
    }

    /**
     * Retourne l'ID de la loot table correspondant au tier du donjon.
     */
    public static String getLootTableIdForTier(int tier) {
        return switch (tier) {
            case 1 -> "donjon_tier1";
            case 2 -> "donjon_tier2";
            case 3 -> "donjon_tier3";
            case 4 -> "donjon_tier4";
            default -> "donjon_common";
        };
    }
}
