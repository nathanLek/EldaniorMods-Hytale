package com.eldanior.system.territory.systems;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.territory.ArenaManager;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParcelRangeSystem extends EntityTickingSystem<EntityStore> {

    private static final Map<UUID, String> playerCurrentParcel = new ConcurrentHashMap<>();
    // Track si le joueur est dans un donjon (parcelId du donjon)
    private static final Map<UUID, String> playerInDungeon = new ConcurrentHashMap<>();
    // Track si le joueur est actuellement dans une zone PvP
    private static final Map<UUID, Boolean> playerInPvpZone = new ConcurrentHashMap<>();
    private int tickCounter = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) tickCounter++;
        if (tickCounter % 20 != 0) return;

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getWorld() == null) return;

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        UUID playerUUID = getUUID(store, ref);
        if (playerUUID == null) return;

        Vector3d pos = transform.getPosition();
        String world = player.getWorld().getName();

        // === BARRIERE ROYAUME ===
        // Si le joueur est hors de tout Royaume, le repousser vers l'interieur
        checkKingdomBarrier(player, transform, pos, world, store, ref);

        ParcelData currentParcel = ParcelManager.getParcelAt(world, pos.x, pos.y, pos.z);
        String currentId = currentParcel != null ? currentParcel.getId() : null;
        String previousId = playerCurrentParcel.get(playerUUID);

        // Verifier si le joueur est toujours dans les limites du donjon
        String dungeonId = playerInDungeon.get(playerUUID);
        if (dungeonId != null) {
            ParcelData dungeon = ParcelManager.get(dungeonId);
            if (dungeon != null && dungeon.contains(pos.x, pos.y, pos.z)) {
                // Toujours dans le donjon — ignorer tous les changements de sous-parcelle
                if (currentId != null) playerCurrentParcel.put(playerUUID, currentId);
                else playerCurrentParcel.remove(playerUUID);
                return;
            } else {
                // Sorti du donjon
                playerInDungeon.remove(playerUUID);
                try {
                    PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (pRef != null) {
                        NotificationHelper.showEventTitle(pRef, "SORTIE DE DONJON", "Vous quittez le donjon", false);
                    }
                } catch (Exception e) { EldaniorLogger.error("ParcelRangeSystem", e); }
            }
        }

        if (!java.util.Objects.equals(currentId, previousId)) {
            try {
                PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                ParcelData previousParcel = previousId != null ? ParcelManager.get(previousId) : null;

                // Gestion sortie d'arene
                if (previousParcel != null && previousParcel.getType() == ParcelType.ARENA) {
                    if (currentParcel == null || currentParcel.getType() != ParcelType.ARENA) {
                        ArenaManager.leaveArena(playerUUID);
                        if (pRef != null) {
                            NotificationHelper.showEventTitle(pRef, "SORTIE D'ARENE", "Vous quittez le combat", false);
                        }
                    }
                }

                if (pRef != null && currentParcel != null) {
                    // === TRACKING DECOUVERTE ===
                    trackDiscovery(currentParcel, player, playerUUID, pRef, store, ref);

                    if (currentParcel.getType() == ParcelType.ARENA) {
                        // ARENE : message + inscription
                        ArenaManager.enterArena(playerUUID, currentParcel.getId());
                        NotificationHelper.showEventTitle(pRef, fmt(currentParcel.getName()), "ARENE — PvP Libre", false);
                    } else if (currentParcel.getType() == ParcelType.DUNGEON) {
                        // DONJON : message avec nom + rank — entrer en mode donjon
                        String rank = currentParcel.getDungeonRank();
                        String subtitle = "DONJON — Rank " + rank;
                        NotificationHelper.showEventTitle(pRef, fmt(currentParcel.getName()), subtitle, false);
                        playerInDungeon.put(playerUUID, currentParcel.getId());
                    } else if (currentParcel.getType() == ParcelType.MINE) {
                        NotificationHelper.showEventTitle(pRef, fmt(currentParcel.getName()), "MINE — Zone de Minage", false);
                    } else if (currentParcel.getType() == ParcelType.FOREST) {
                        NotificationHelper.showEventTitle(pRef, fmt(currentParcel.getName()), "FORET — Zone de Recolte", false);
                    } else if (isBigZone(currentParcel.getType())) {
                        // Verifier toute la hierarchie (parent, grand-parent, etc.)
                        boolean isAncestorOfPrevious = false;
                        if (previousParcel != null) {
                            String pid = previousParcel.getParentId();
                            while (pid != null) {
                                if (currentParcel.getId().equals(pid)) {
                                    isAncestorOfPrevious = true;
                                    break;
                                }
                                ParcelData pp = ParcelManager.get(pid);
                                pid = pp != null ? pp.getParentId() : null;
                            }
                        }

                        if (!isAncestorOfPrevious) {
                            showZoneTitle(pRef, currentParcel);
                        }
                    } else {
                        showZoneNotification(pRef, currentParcel);
                    }
                } else if (pRef != null && currentParcel == null && previousId != null) {
                    NotificationHelper.showEventTitle(pRef, "ZONE SAUVAGE", "Territoire inexplore", false);
                }
            } catch (Exception e) { EldaniorLogger.error("ParcelRangeSystem", e); }

            if (currentId != null) playerCurrentParcel.put(playerUUID, currentId);
            else playerCurrentParcel.remove(playerUUID);

            // === NOTIFICATION PVP ENTREE/SORTIE ===
            try {
                boolean wasPvp = Boolean.TRUE.equals(playerInPvpZone.get(playerUUID));
                boolean nowPvp = currentParcel != null && currentParcel.isPvpEnabled();

                if (nowPvp != wasPvp) {
                    PlayerRef pvpRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (pvpRef != null) {
                        if (nowPvp) {
                            NotificationHelper.showEventTitle(pvpRef,
                                    "<color:red>ZONE PVP</color>",
                                    "<color:red>Les combats entre joueurs sont autorises</color>", false);
                            NotificationHelper.sendNotification(pvpRef,
                                    "<color:red>Attention ! Vous entrez en zone PvP.</color>",
                                    NotificationStyle.Danger);
                        } else {
                            NotificationHelper.showEventTitle(pvpRef,
                                    "<color:green>ZONE SURE</color>",
                                    "<color:green>Les combats entre joueurs sont desactives</color>", false);
                            NotificationHelper.sendNotification(pvpRef,
                                    "<color:green>Vous quittez la zone PvP.</color>",
                                    NotificationStyle.Success);
                        }
                    }
                }

                if (nowPvp) playerInPvpZone.put(playerUUID, true);
                else playerInPvpZone.remove(playerUUID);
            } catch (Exception e) { EldaniorLogger.error("ParcelRangeSystem:pvp", e); }
        }
    }

    // ==================== BARRIERE ROYAUME ====================

    /**
     * Verifie si le joueur est hors de tout Royaume.
     * Si oui, le teleporte au bord le plus proche du Royaume le plus proche.
     */
    private void checkKingdomBarrier(Player player, TransformComponent transform,
                                     Vector3d pos, String world,
                                     Store<EntityStore> store, Ref<EntityStore> ref) {
        // Verifier si le joueur est dans un Royaume
        for (ParcelData p : ParcelManager.getAll()) {
            if (p.getType() == ParcelType.KINGDOM && world.equals(p.getWorld())
                    && p.contains(pos.x, pos.y, pos.z)) {
                return; // Dans un Royaume, tout va bien
            }
        }

        // Le joueur n'est dans aucun Royaume — trouver le plus proche
        ParcelData closest = null;
        double closestDist = Double.MAX_VALUE;

        for (ParcelData p : ParcelManager.getAll()) {
            if (p.getType() != ParcelType.KINGDOM || !world.equals(p.getWorld())) continue;

            // Distance au centre du Royaume
            double cx = (p.getMinX() + p.getMaxX()) / 2.0;
            double cz = (p.getMinZ() + p.getMaxZ()) / 2.0;
            double dist = (pos.x - cx) * (pos.x - cx) + (pos.z - cz) * (pos.z - cz);

            if (dist < closestDist) {
                closestDist = dist;
                closest = p;
            }
        }

        if (closest == null) return; // Pas de Royaume du tout, pas de barriere

        // Calculer la position de repoussement (bord le plus proche du Royaume)
        double newX = Math.max(closest.getMinX() + 2, Math.min(closest.getMaxX() - 2, pos.x));
        double newZ = Math.max(closest.getMinZ() + 2, Math.min(closest.getMaxZ() - 2, pos.z));

        try {
            transform.teleportPosition(new Vector3d(newX, pos.y, newZ));

            // Avertissement
            UUID playerUUID = getUUID(store, ref);
            if (playerUUID != null) {
                PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (pRef != null) {
                    NotificationHelper.sendNotification(pRef,
                            "<color:red>Vous ne pouvez pas quitter le Royaume !</color>",
                            com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Warning);
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("ParcelRangeSystem.barrier", e);
        }
    }

    /** Formate un nom de parcelle pour l'affichage : remplace les _ par des espaces */
    private static String fmt(String name) {
        return name != null ? name.replace('_', ' ') : "";
    }

    private boolean isBigZone(ParcelType type) {
        return type == ParcelType.KINGDOM || type == ParcelType.GRAND_TERRITORY || type == ParcelType.TERRITORY || type == ParcelType.CITY;
    }

    private void showZoneTitle(PlayerRef pRef, ParcelData parcel) {
        String title = fmt(parcel.getName());
        String subtitle = parcel.getType().getLabel().toUpperCase();

        // Afficher l'indicateur PvP pour toutes les zones (pas seulement CITY)
        if (parcel.isPvpEnabled()) {
            subtitle += " — <color:red>PvP Active</color>";
        } else {
            subtitle += " — <color:green>Zone Sure</color>";
        }

        NotificationHelper.showEventTitle(pRef, title, subtitle, false);
    }

    private void showZoneNotification(PlayerRef pRef, ParcelData parcel) {
        String ownerInfo = parcel.getOwnerName().isEmpty() ? "Libre" : parcel.getOwnerName();
        String color = parcel.isProtectedByDefault() ? "gold" : "green";

        NotificationHelper.sendNotification(pRef,
                "<color:" + color + ">[" + parcel.getType().getLabel() + "] " + fmt(parcel.getName()) + "</color> <color:gray>(" + ownerInfo + ")</color>",
                NotificationStyle.Default);
    }

    /**
     * Retourne true si le joueur est actuellement dans une zone PvP.
     */
    public static boolean isInPvpZone(UUID playerUUID) {
        return Boolean.TRUE.equals(playerInPvpZone.get(playerUUID));
    }

    public static void handleDisconnect(UUID playerUUID) {
        playerCurrentParcel.remove(playerUUID);
        playerInDungeon.remove(playerUUID);
        playerInPvpZone.remove(playerUUID);
        ArenaManager.handleDisconnect(playerUUID);
    }

    private UUID getUUID(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef == null) return null;
            return UUIDExtractor.getUUID(pRef);
        } catch (Exception e) { return null; }
    }

    private void trackDiscovery(ParcelData parcel, Player player, UUID playerUUID,
                                PlayerRef pRef, Store<EntityStore> store, Ref<EntityStore> ref) {
        // Seuls certains types sont trackés
        ParcelType type = parcel.getType();
        if (type != ParcelType.DUNGEON && type != ParcelType.MINE
                && type != ParcelType.FOREST && type != ParcelType.ARENA) return;

        try {
            var lvlType = com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType();
            com.eldanior.system.config.Player.PlayerLevelData data = store.getComponent(ref, lvlType);
            if (data == null) return;

            boolean isDungeon = (type == ParcelType.DUNGEON);
            // Modifie directement l'objet (pas de putComponent dans un tick ECS)
            boolean isNew = data.discoverParcel(parcel.getId(), isDungeon);

            if (isNew) {
                // Notification de découverte
                NotificationHelper.sendNotification(pRef,
                        "<color:green>Zone decouverte : " + parcel.getName() + " !</color>",
                        com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Success);

                // Premier joueur à découvrir ?
                boolean isFirstDiscoverer = parcel.setFirstDiscoverer(player.getPlayerRef().getUsername(), playerUUID.toString());
                if (isFirstDiscoverer) {
                    ParcelManager.save();
                    NotificationHelper.showEventTitle(pRef,
                            "PREMIER EXPLORATEUR", "Vous etes le premier a decouvrir " + parcel.getName() + " !", true);
                }
            }
        } catch (Exception e) { EldaniorLogger.error("ParcelRangeSystem:discovery", e); }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
