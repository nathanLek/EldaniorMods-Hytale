package com.eldanior.system.territory.systems;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Systeme de regeneration des blocs dans les parcelles FARM, MINE, FOREST et DUNGEON.
 *
 * - FARM : regen simple par bloc + regen periodique par snapshot (cycle 24h, batchee)
 * - MINE : regen simple par bloc (le bloc casse par le joueur est restaure apres le delai)
 * - FOREST : regen par snapshot (detecte aussi les blocs tombes par gravite/physique)
 * - DUNGEON : regen automatique toutes les 24h par snapshot (reset complet du donjon)
 *
 * Un avertissement est envoye aux joueurs dans la zone 10s avant la regeneration.
 */
public class FarmRegenSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    // ==================== FARM / MINE : regen simple ====================

    private record BlockPos(String world, int x, int y, int z) {}
    private static final Map<BlockPos, Boolean> pendingSimpleRegen = new ConcurrentHashMap<>();

    // ==================== FOREST : regen par snapshot ====================

    private static final Map<String, Map<String, String>> parcelSnapshots = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> pendingSnapshotRegen = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastScanTime = new ConcurrentHashMap<>();

    private static final long SCAN_COOLDOWN_MS = 3000;
    private static final long CASCADE_DELAY_MS = 2000;
    private static final int WARNING_BEFORE_SEC = 10;

    // ==================== FARM : regen periodique par snapshot ====================

    private static final Map<String, Map<String, String>> farmSnapshots = new ConcurrentHashMap<>();
    private static final Set<String> farmTimersStarted = ConcurrentHashMap.newKeySet();
    private static final long FARM_REGEN_INTERVAL_MS = 24L * 60 * 60 * 1000; // 24h
    private static final int FARM_BATCH_SIZE = 50; // blocs restaures par batch pour eviter les lags

    // ==================== DUNGEON : regen auto 24h ====================

    private static final Map<String, Map<String, String>> dungeonSnapshots = new ConcurrentHashMap<>();
    private static final Set<String> dungeonTimersStarted = ConcurrentHashMap.newKeySet();
    private static final long DUNGEON_REGEN_INTERVAL_MS = 24L * 60 * 60 * 1000; // 24h
    // World par parcelId (evite le bug multi-mondes avec un seul volatile static)
    private static final Map<String, World> cachedWorlds = new ConcurrentHashMap<>();

    public FarmRegenSystem() { super(BreakBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl BreakBlockEvent event) {

        Player player = chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getWorld() == null) return;

        Vector3i target = event.getTargetBlock();
        if (target == null) return;

        String worldName = player.getWorld().getName();

        ParcelData parcel = ParcelManager.getParcelAt(worldName, target.x(), target.y(), target.z());
        if (parcel == null) return;

        ParcelType type = parcel.getType();
        World world = player.getWorld();

        // Stocker la reference au monde par parcelle (multi-mondes safe)
        cachedWorlds.put(parcel.getId(), world);

        // +1 XP par bloc casse dans une zone de farm/mine/forest
        if (type == ParcelType.FARM || type == ParcelType.MINE || type == ParcelType.FOREST) {
            Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
            com.hypixel.hytale.component.ComponentType<com.hypixel.hytale.server.core.universe.world.storage.EntityStore,
                    com.eldanior.system.config.Player.PlayerLevelData> dataType = com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType();
            com.eldanior.system.config.Player.PlayerLevelData data = store.getComponent(playerRef, dataType);
            if (data != null) {
                data.addExperience(1);
                commandBuffer.run(deferredStore -> deferredStore.putComponent(playerRef, dataType, data));
                // Notification XP
                try {
                    PlayerRef pRef = store.getComponent(playerRef, PlayerRef.getComponentType());
                    if (pRef != null) {
                        NotificationHelper.sendNotification(pRef,
                                "<color:green>+1 XP</color>",
                                com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Success);
                    }
                } catch (Exception e) { /* skip */ }
            }
        }

        if (type == ParcelType.FARM || type == ParcelType.MINE) {
            handleSimpleRegen(target, worldName, parcel, world);
            if (type == ParcelType.FARM) {
                ensureFarmSnapshot(parcel, world);
            }
        } else if (type == ParcelType.FOREST) {
            handleForestRegen(target, parcel, world);
        } else if (type == ParcelType.DUNGEON) {
            // Le donjon se regenere automatiquement, mais on prend le snapshot au premier break
            ensureDungeonSnapshot(parcel, world);
        }
    }

    // ==================== FARM / MINE : regen bloc par bloc ====================

    private void handleSimpleRegen(Vector3i target, String worldName, ParcelData parcel, World world) {
        String blockName;
        try {
            blockName = world.getBlockType(target.x(), target.y(), target.z()).getId();
        } catch (Exception e) {
            EldaniorLogger.error("FarmRegen: impossible de lire le bloc", e);
            return;
        }
        if (blockName == null || blockName.isEmpty()) return;

        BlockPos blockPos = new BlockPos(worldName, target.x(), target.y(), target.z());
        if (pendingSimpleRegen.containsKey(blockPos)) return;

        pendingSimpleRegen.put(blockPos, Boolean.TRUE);
        int delaySec = parcel.getRegenDelaySec();

        EldaniorLogger.info("[FarmRegen] Bloc casse dans " + parcel.getName() +
                " a " + target.x() + "," + target.y() + "," + target.z() +
                " block=" + blockName + " delai=" + delaySec + "s");

        // Avertissement 10s avant la regen
        scheduleWarning(parcel, delaySec);

        EldaniorLogger.SCHEDULER.schedule(() -> {
            try {
                world.execute(() -> {
                    try {
                        world.setBlock(target.x(), target.y(), target.z(), blockName);
                        EldaniorLogger.info("[FarmRegen] Bloc restaure a " +
                                target.x() + "," + target.y() + "," + target.z() +
                                " block=" + blockName);
                    } catch (Exception e) {
                        EldaniorLogger.error("FarmRegen.setBlock", e);
                    } finally {
                        pendingSimpleRegen.remove(blockPos);
                    }
                });
            } catch (Exception e) {
                EldaniorLogger.error("FarmRegen.schedule", e);
                pendingSimpleRegen.remove(blockPos);
            }
        }, delaySec, TimeUnit.SECONDS);
    }

    // ==================== FOREST : regen par snapshot ====================

    private void handleForestRegen(Vector3i target, ParcelData parcel, World world) {
        String parcelId = parcel.getId();

        if (!parcelSnapshots.containsKey(parcelId)) {
            takeSnapshot(parcelId, parcel, world);
        }

        EldaniorLogger.info("[ForestRegen] Bloc casse dans " + parcel.getName() +
                " a " + target.x() + "," + target.y() + "," + target.z() +
                " delai=" + parcel.getRegenDelaySec() + "s");

        long now = System.currentTimeMillis();
        Long lastScan = lastScanTime.get(parcelId);
        if (lastScan != null && (now - lastScan) < SCAN_COOLDOWN_MS) return;
        lastScanTime.put(parcelId, now);

        int delaySec = parcel.getRegenDelaySec();

        EldaniorLogger.SCHEDULER.schedule(() -> {
            try {
                world.execute(() -> {
                    try {
                        scanAndScheduleRegen(parcelId, parcel, world, delaySec);
                    } catch (Exception e) {
                        EldaniorLogger.error("ForestRegen.scan", e);
                    }
                });
            } catch (Exception e) {
                EldaniorLogger.error("ForestRegen.cascadeDelay", e);
            }
        }, CASCADE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void takeSnapshot(String parcelId, ParcelData parcel, World world) {
        Map<String, String> snapshot = new ConcurrentHashMap<>();

        try {
            world.execute(() -> {
                try {
                    int count = 0;
                    for (int x = parcel.getMinX(); x <= parcel.getMaxX(); x++) {
                        for (int y = parcel.getMinY(); y <= parcel.getMaxY(); y++) {
                            for (int z = parcel.getMinZ(); z <= parcel.getMaxZ(); z++) {
                                BlockType bt = world.getBlockType(x, y, z);
                                if (bt != null && bt != BlockType.EMPTY) {
                                    String id = bt.getId();
                                    if (id != null && !id.isEmpty()) {
                                        snapshot.put(posKey(x, y, z), id);
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                    parcelSnapshots.put(parcelId, snapshot);
                    EldaniorLogger.info("[ForestRegen] Snapshot pris pour " + parcel.getName() +
                            " : " + count + " blocs sauvegardes");
                } catch (Exception e) {
                    EldaniorLogger.error("ForestRegen.takeSnapshot", e);
                }
            });
        } catch (Exception e) {
            EldaniorLogger.error("ForestRegen.takeSnapshot.execute", e);
        }
    }

    private void scanAndScheduleRegen(String parcelId, ParcelData parcel, World world, int delaySec) {
        Map<String, String> snapshot = parcelSnapshots.get(parcelId);
        if (snapshot == null || snapshot.isEmpty()) {
            EldaniorLogger.info("[ForestRegen] Scan " + parcel.getName() + " : snapshot absent !");
            return;
        }

        Set<String> pending = pendingSnapshotRegen.computeIfAbsent(parcelId,
                k -> ConcurrentHashMap.newKeySet());

        List<String[]> toRegen = new ArrayList<>();

        for (Map.Entry<String, String> entry : snapshot.entrySet()) {
            String posKey = entry.getKey();
            String expectedBlock = entry.getValue();

            if (pending.contains(posKey)) continue;

            int[] coords = parseKey(posKey);
            if (coords == null) continue;

            try {
                BlockType currentType = world.getBlockType(coords[0], coords[1], coords[2]);
                if (currentType == null || currentType == BlockType.EMPTY) {
                    toRegen.add(new String[]{posKey, expectedBlock});
                    pending.add(posKey);
                }
            } catch (Exception e) {
                // Chunk non charge
            }
        }

        EldaniorLogger.info("[ForestRegen] Scan " + parcel.getName() +
                " : manquants=" + toRegen.size() + " snapshot=" + snapshot.size());

        if (toRegen.isEmpty()) return;

        EldaniorLogger.info("[ForestRegen] " + parcel.getName() +
                " : " + toRegen.size() + " blocs a regenerer dans " + delaySec + "s");

        // Avertissement 10s avant la regen
        scheduleWarning(parcel, delaySec);

        EldaniorLogger.SCHEDULER.schedule(() -> {
            try {
                world.execute(() -> {
                    int restored = 0;
                    for (String[] entry : toRegen) {
                        try {
                            int[] coords = parseKey(entry[0]);
                            if (coords != null) {
                                world.setBlock(coords[0], coords[1], coords[2], entry[1]);
                                restored++;
                            }
                        } catch (Exception e) {
                            EldaniorLogger.error("ForestRegen.restore " + entry[1], e);
                        } finally {
                            pending.remove(entry[0]);
                        }
                    }
                    EldaniorLogger.info("[ForestRegen] " + parcel.getName() +
                            " : " + restored + " blocs restaures !");
                });
            } catch (Exception e) {
                EldaniorLogger.error("ForestRegen.regenSchedule", e);
                for (String[] entry : toRegen) pending.remove(entry[0]);
            }
        }, delaySec, TimeUnit.SECONDS);
    }

    // ==================== FARM : regen periodique par snapshot ====================

    /**
     * Prend le snapshot de la ferme si pas encore fait, et demarre le timer periodique.
     */
    private void ensureFarmSnapshot(ParcelData parcel, World world) {
        String parcelId = parcel.getId();

        if (!farmSnapshots.containsKey(parcelId)) {
            takeFarmSnapshot(parcelId, parcel, world);
        }

        if (farmTimersStarted.add(parcelId)) {
            startFarmRegenTimer(parcelId, parcel);
            EldaniorLogger.info("[FarmRegen] Timer periodique demarre pour " + parcel.getName());
        }
    }

    private static void takeFarmSnapshot(String parcelId, ParcelData parcel, World world) {
        Map<String, String> snapshot = new ConcurrentHashMap<>();

        try {
            world.execute(() -> {
                try {
                    int count = 0;
                    for (int x = parcel.getMinX(); x <= parcel.getMaxX(); x++) {
                        for (int y = parcel.getMinY(); y <= parcel.getMaxY(); y++) {
                            for (int z = parcel.getMinZ(); z <= parcel.getMaxZ(); z++) {
                                BlockType bt = world.getBlockType(x, y, z);
                                if (bt != null && bt != BlockType.EMPTY) {
                                    String id = bt.getId();
                                    if (id != null && !id.isEmpty()) {
                                        snapshot.put(posKey(x, y, z), id);
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                    farmSnapshots.put(parcelId, snapshot);
                    EldaniorLogger.info("[FarmRegen] Snapshot pris pour " + parcel.getName() +
                            " : " + count + " blocs sauvegardes");
                } catch (Exception e) {
                    EldaniorLogger.error("FarmRegen.takeFarmSnapshot", e);
                }
            });
        } catch (Exception e) {
            EldaniorLogger.error("FarmRegen.takeFarmSnapshot.execute", e);
        }
    }

    /**
     * Demarre un timer periodique qui regenere la ferme.
     * Utilise le regenDelaySec de la parcelle comme intervalle (converti en ms),
     * avec un minimum de 24h pour eviter la surcharge.
     */
    private void startFarmRegenTimer(String parcelId, ParcelData parcel) {
        long intervalMs = FARM_REGEN_INTERVAL_MS;

        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                regenFarm(parcelId, parcel);
            } catch (Exception e) {
                EldaniorLogger.error("FarmRegen.timer", e);
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Regenere une ferme depuis son snapshot, avec batching.
     * Les blocs sont restaures par lots de FARM_BATCH_SIZE pour eviter les lags.
     */
    private void regenFarm(String parcelId, ParcelData parcel) {
        Map<String, String> snapshot = farmSnapshots.get(parcelId);
        if (snapshot == null || snapshot.isEmpty()) return;

        World world = cachedWorlds.get(parcelId);
        if (world == null) return;

        EldaniorLogger.info("[FarmRegen] Regeneration periodique de " + parcel.getName() +
                " dans " + WARNING_BEFORE_SEC + "s (" + snapshot.size() + " blocs)");

        // Avertissement aux joueurs dans la ferme
        notifyPlayersInParcel(parcel);

        // Attendre puis restaurer par batch
        EldaniorLogger.SCHEDULER.schedule(() -> {
            try {
                world.execute(() -> {
                    // Collecter les blocs a restaurer
                    List<Map.Entry<String, String>> toRestore = new ArrayList<>();
                    for (Map.Entry<String, String> entry : snapshot.entrySet()) {
                        try {
                            int[] coords = parseKey(entry.getKey());
                            if (coords == null) continue;

                            BlockType currentType = world.getBlockType(coords[0], coords[1], coords[2]);
                            String currentId = (currentType != null && currentType != BlockType.EMPTY)
                                    ? currentType.getId() : null;

                            if (!entry.getValue().equals(currentId)) {
                                toRestore.add(entry);
                            }
                        } catch (Exception e) {
                            // Chunk non charge, ignorer
                        }
                    }

                    if (toRestore.isEmpty()) {
                        EldaniorLogger.info("[FarmRegen] " + parcel.getName() + " : aucun bloc a restaurer");
                        return;
                    }

                    // Restaurer le premier batch immediatement
                    int totalToRestore = toRestore.size();
                    int batches = (totalToRestore + FARM_BATCH_SIZE - 1) / FARM_BATCH_SIZE;

                    EldaniorLogger.info("[FarmRegen] " + parcel.getName() +
                            " : " + totalToRestore + " blocs a restaurer en " + batches + " batch(es)");

                    for (int batchIdx = 0; batchIdx < batches; batchIdx++) {
                        int start = batchIdx * FARM_BATCH_SIZE;
                        int end = Math.min(start + FARM_BATCH_SIZE, totalToRestore);
                        List<Map.Entry<String, String>> batch = toRestore.subList(start, end);
                        int batchNum = batchIdx + 1;

                        if (batchIdx == 0) {
                            // Premier batch : restaurer immediatement
                            int restored = restoreBatch(batch, world);
                            EldaniorLogger.info("[FarmRegen] " + parcel.getName() +
                                    " batch 1/" + batches + " : " + restored + " blocs restaures");
                        } else {
                            // Batches suivants : decaler de 1s chacun
                            long delayMs = batchIdx * 1000L;
                            // Copier la sous-liste car subList est liee a la liste originale
                            List<Map.Entry<String, String>> batchCopy = new ArrayList<>(batch);
                            EldaniorLogger.SCHEDULER.schedule(() -> {
                                try {
                                    world.execute(() -> {
                                        int restored = restoreBatch(batchCopy, world);
                                        EldaniorLogger.info("[FarmRegen] " + parcel.getName() +
                                                " batch " + batchNum + "/" + batches +
                                                " : " + restored + " blocs restaures");
                                    });
                                } catch (Exception e) {
                                    EldaniorLogger.error("FarmRegen.batchRestore", e);
                                }
                            }, delayMs, TimeUnit.MILLISECONDS);
                        }
                    }
                });
            } catch (Exception e) {
                EldaniorLogger.error("FarmRegen.regenFarm.execute", e);
            }
        }, WARNING_BEFORE_SEC, TimeUnit.SECONDS);
    }

    /**
     * Restaure un lot de blocs. Retourne le nombre de blocs restaures.
     */
    private static int restoreBatch(List<Map.Entry<String, String>> batch, World world) {
        int restored = 0;
        for (Map.Entry<String, String> entry : batch) {
            try {
                int[] coords = parseKey(entry.getKey());
                if (coords != null) {
                    world.setBlock(coords[0], coords[1], coords[2], entry.getValue());
                    restored++;
                }
            } catch (Exception e) {
                EldaniorLogger.error("FarmRegen.restoreBatch", e);
            }
        }
        return restored;
    }

    // ==================== AVERTISSEMENT JOUEURS ====================

    /**
     * Envoie un avertissement aux joueurs dans la parcelle 10s avant la regen.
     * Si le delai est <= 10s, l'avertissement est envoye immediatement.
     */
    private void scheduleWarning(ParcelData parcel, int delaySec) {
        int warningDelay = Math.max(0, delaySec - WARNING_BEFORE_SEC);

        EldaniorLogger.SCHEDULER.schedule(() -> {
            try {
                notifyPlayersInParcel(parcel);
            } catch (Exception e) {
                EldaniorLogger.error("FarmRegen.warning", e);
            }
        }, warningDelay, TimeUnit.SECONDS);
    }

    /**
     * Envoie une notification a tous les joueurs presents dans la parcelle.
     */
    private void notifyPlayersInParcel(ParcelData parcel) {
        String zoneName = parcel.getName();
        int sec = Math.min(WARNING_BEFORE_SEC, parcel.getRegenDelaySec());

        // Parcourir les positions connues des joueurs connectes
        for (Map.Entry<UUID, Vector3d> entry : PlayerPositionTracker.PLAYER_POSITIONS.entrySet()) {
            try {
                Vector3d pos = entry.getValue();
                if (pos == null) continue;

                // Verifier si le joueur est dans la parcelle
                if (parcel.contains(pos.x, pos.y, pos.z)) {
                    PlayerRef pRef = Universe.get().getPlayer(entry.getKey());
                    if (pRef != null) {
                        NotificationHelper.sendNotification(pRef,
                                "<color:gold>Regeneration de " + zoneName +
                                        " dans " + sec + "s !</color>",
                                NotificationStyle.Warning);
                    }
                }
            } catch (Exception e) {
                // Joueur deconnecte ou erreur, ignorer
            }
        }
    }

    // ==================== DUNGEON : regen auto 24h ====================

    /**
     * Prend le snapshot du donjon si pas encore fait, et demarre le timer 24h.
     */
    private void ensureDungeonSnapshot(ParcelData parcel, World world) {
        String parcelId = parcel.getId();

        // Prendre le snapshot une seule fois
        if (!dungeonSnapshots.containsKey(parcelId)) {
            takeDungeonSnapshot(parcelId, parcel, world);
        }

        // Demarrer le timer 24h une seule fois
        if (dungeonTimersStarted.add(parcelId)) {
            startDungeonRegenTimer(parcelId, parcel);
            EldaniorLogger.info("[DungeonRegen] Timer 24h demarre pour " + parcel.getName());
        }
    }

    private void takeDungeonSnapshot(String parcelId, ParcelData parcel, World world) {
        Map<String, String> snapshot = new ConcurrentHashMap<>();

        try {
            world.execute(() -> {
                try {
                    int count = 0;
                    for (int x = parcel.getMinX(); x <= parcel.getMaxX(); x++) {
                        for (int y = parcel.getMinY(); y <= parcel.getMaxY(); y++) {
                            for (int z = parcel.getMinZ(); z <= parcel.getMaxZ(); z++) {
                                BlockType bt = world.getBlockType(x, y, z);
                                if (bt != null && bt != BlockType.EMPTY) {
                                    String id = bt.getId();
                                    if (id != null && !id.isEmpty()) {
                                        snapshot.put(posKey(x, y, z), id);
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                    dungeonSnapshots.put(parcelId, snapshot);
                    EldaniorLogger.info("[DungeonRegen] Snapshot pris pour " + parcel.getName() +
                            " : " + count + " blocs sauvegardes");
                } catch (Exception e) {
                    EldaniorLogger.error("DungeonRegen.takeSnapshot", e);
                }
            });
        } catch (Exception e) {
            EldaniorLogger.error("DungeonRegen.takeSnapshot.execute", e);
        }
    }

    /**
     * Demarre un timer periodique qui regenere le donjon toutes les 24h.
     */
    private void startDungeonRegenTimer(String parcelId, ParcelData parcel) {
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                regenDungeon(parcelId, parcel);
            } catch (Exception e) {
                EldaniorLogger.error("DungeonRegen.timer", e);
            }
        }, DUNGEON_REGEN_INTERVAL_MS, DUNGEON_REGEN_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Regenere un donjon complet depuis son snapshot.
     * Envoie un avertissement 10s avant.
     */
    private void regenDungeon(String parcelId, ParcelData parcel) {
        Map<String, String> snapshot = dungeonSnapshots.get(parcelId);
        if (snapshot == null || snapshot.isEmpty()) return;

        World world = cachedWorlds.get(parcelId);
        if (world == null) return;

        EldaniorLogger.info("[DungeonRegen] Regeneration de " + parcel.getName() +
                " dans 10s (" + snapshot.size() + " blocs)");

        // Avertissement aux joueurs dans le donjon
        notifyPlayersInParcel(parcel);

        // Attendre 10s puis restaurer
        EldaniorLogger.SCHEDULER.schedule(() -> {
            try {
                world.execute(() -> {
                    int restored = 0;
                    for (Map.Entry<String, String> entry : snapshot.entrySet()) {
                        try {
                            int[] coords = parseKey(entry.getKey());
                            if (coords == null) continue;

                            BlockType currentType = world.getBlockType(coords[0], coords[1], coords[2]);
                            String currentId = (currentType != null && currentType != BlockType.EMPTY)
                                    ? currentType.getId() : null;

                            // Restaurer seulement si le bloc a change
                            if (!entry.getValue().equals(currentId)) {
                                world.setBlock(coords[0], coords[1], coords[2], entry.getValue());
                                restored++;
                            }
                        } catch (Exception e) {
                            EldaniorLogger.error("DungeonRegen.restore", e);
                        }
                    }
                    EldaniorLogger.info("[DungeonRegen] " + parcel.getName() +
                            " : " + restored + " blocs restaures !");
                });
            } catch (Exception e) {
                EldaniorLogger.error("DungeonRegen.execute", e);
            }
        }, WARNING_BEFORE_SEC, TimeUnit.SECONDS);
    }

    /**
     * Initialise les snapshots et timers pour tous les donjons existants.
     * A appeler au demarrage avec une reference au World.
     */
    /**
     * Initialise les snapshots et timers pour toutes les parcelles FARM existantes.
     * A appeler au demarrage avec une reference au World.
     * Prend un snapshot de chaque ferme et demarre un timer de regen periodique (24h).
     */
    public static void initFarmTimers(World world) {
        int count = 0;
        for (ParcelData parcel : ParcelManager.getAll()) {
            if (parcel.getType() != ParcelType.FARM) continue;
            String parcelId = parcel.getId();

            // Stocker le world par parcelle (multi-mondes safe)
            cachedWorlds.put(parcelId, world);

            if (!farmSnapshots.containsKey(parcelId)) {
                takeFarmSnapshot(parcelId, parcel, world);
            }

            if (farmTimersStarted.add(parcelId)) {
                long intervalMs = FARM_REGEN_INTERVAL_MS;

                // Decaler les timers pour eviter que toutes les fermes se regenerent au meme instant
                long offsetMs = count * 30_000L; // 30s entre chaque ferme

                EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
                    try {
                        Map<String, String> snap = farmSnapshots.get(parcelId);
                        World w = cachedWorlds.get(parcelId);
                        if (snap == null || snap.isEmpty() || w == null) return;

                        EldaniorLogger.info("[FarmRegen] Regen auto " + parcel.getName() +
                                " dans " + WARNING_BEFORE_SEC + "s (" + snap.size() + " blocs)");

                        // Avertissement
                        String zoneName = parcel.getName();
                        for (Map.Entry<UUID, Vector3d> e : PlayerPositionTracker.PLAYER_POSITIONS.entrySet()) {
                            try {
                                Vector3d pos = e.getValue();
                                if (pos != null && parcel.contains(pos.x, pos.y, pos.z)) {
                                    PlayerRef pRef = Universe.get().getPlayer(e.getKey());
                                    if (pRef != null) {
                                        NotificationHelper.sendNotification(pRef,
                                                "<color:gold>La ferme " + zoneName.replace('_', ' ') +
                                                        " se regenere dans " + WARNING_BEFORE_SEC + "s !</color>",
                                                NotificationStyle.Warning);
                                    }
                                }
                            } catch (Exception ignored) {}
                        }

                        // Restaurer par batch apres le delai d'avertissement
                        EldaniorLogger.SCHEDULER.schedule(() -> {
                            try {
                                w.execute(() -> {
                                    // Collecter les blocs manquants
                                    List<Map.Entry<String, String>> toRestore = new ArrayList<>();
                                    for (Map.Entry<String, String> entry : snap.entrySet()) {
                                        try {
                                            int[] coords = parseKey(entry.getKey());
                                            if (coords == null) continue;

                                            BlockType currentType = w.getBlockType(
                                                    coords[0], coords[1], coords[2]);
                                            String currentId = (currentType != null && currentType != BlockType.EMPTY)
                                                    ? currentType.getId() : null;

                                            if (!entry.getValue().equals(currentId)) {
                                                toRestore.add(entry);
                                            }
                                        } catch (Exception ex) {
                                            // Chunk non charge
                                        }
                                    }

                                    if (toRestore.isEmpty()) {
                                        EldaniorLogger.info("[FarmRegen] " + parcel.getName() +
                                                " : aucun bloc a restaurer");
                                        return;
                                    }

                                    // Batch restore
                                    int total = toRestore.size();
                                    int batches = (total + FARM_BATCH_SIZE - 1) / FARM_BATCH_SIZE;

                                    // Premier batch immediatement
                                    int end0 = Math.min(FARM_BATCH_SIZE, total);
                                    int restored = restoreBatch(toRestore.subList(0, end0), w);
                                    EldaniorLogger.info("[FarmRegen] " + parcel.getName() +
                                            " batch 1/" + batches + " : " + restored + " blocs");

                                    // Batches suivants decales de 1s
                                    for (int b = 1; b < batches; b++) {
                                        int start = b * FARM_BATCH_SIZE;
                                        int end = Math.min(start + FARM_BATCH_SIZE, total);
                                        List<Map.Entry<String, String>> batchCopy = new ArrayList<>(
                                                toRestore.subList(start, end));
                                        int batchNum = b + 1;
                                        int finalBatches = batches;
                                        EldaniorLogger.SCHEDULER.schedule(() -> {
                                            try {
                                                w.execute(() -> {
                                                    int r = restoreBatch(batchCopy, w);
                                                    EldaniorLogger.info("[FarmRegen] " + parcel.getName() +
                                                            " batch " + batchNum + "/" + finalBatches +
                                                            " : " + r + " blocs");
                                                });
                                            } catch (Exception ex) {
                                                EldaniorLogger.error("FarmRegen.batchRestore", ex);
                                            }
                                        }, b * 1000L, TimeUnit.MILLISECONDS);
                                    }
                                });
                            } catch (Exception ex) {
                                EldaniorLogger.error("FarmRegen.execute", ex);
                            }
                        }, WARNING_BEFORE_SEC, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        EldaniorLogger.error("FarmRegen.timer", e);
                    }
                }, intervalMs + offsetMs, intervalMs, TimeUnit.MILLISECONDS);

                EldaniorLogger.info("[FarmRegen] Timer periodique demarre pour " + parcel.getName() +
                        " (offset=" + offsetMs / 1000 + "s)");
                count++;
            }
        }
        if (count > 0) {
            EldaniorLogger.info("[FarmRegen] " + count + " ferme(s) initialisee(s) avec snapshot + timer");
        }
    }

    public static void initDungeonTimers(World world) {
        // Les timers demarreront au premier break dans chaque donjon.
        // Pour forcer l'init au demarrage, on pourrait scanner toutes les parcelles DUNGEON ici.
        for (ParcelData parcel : ParcelManager.getAll()) {
            if (parcel.getType() == ParcelType.DUNGEON) {
                String parcelId = parcel.getId();

                // Stocker le world par parcelle (multi-mondes safe)
                cachedWorlds.put(parcelId, world);

                if (!dungeonSnapshots.containsKey(parcelId)) {
                    // Prendre le snapshot au demarrage
                    Map<String, String> snapshot = new ConcurrentHashMap<>();
                    try {
                        world.execute(() -> {
                            try {
                                int count = 0;
                                for (int x = parcel.getMinX(); x <= parcel.getMaxX(); x++) {
                                    for (int y = parcel.getMinY(); y <= parcel.getMaxY(); y++) {
                                        for (int z = parcel.getMinZ(); z <= parcel.getMaxZ(); z++) {
                                            BlockType bt = world.getBlockType(x, y, z);
                                            if (bt != null && bt != BlockType.EMPTY) {
                                                String id = bt.getId();
                                                if (id != null && !id.isEmpty()) {
                                                    snapshot.put(posKey(x, y, z), id);
                                                    count++;
                                                }
                                            }
                                        }
                                    }
                                }
                                dungeonSnapshots.put(parcelId, snapshot);
                                EldaniorLogger.info("[DungeonRegen] Snapshot init pour " + parcel.getName() +
                                        " : " + count + " blocs");
                            } catch (Exception e) {
                                EldaniorLogger.error("DungeonRegen.initSnapshot", e);
                            }
                        });
                    } catch (Exception e) {
                        EldaniorLogger.error("DungeonRegen.initSnapshot.execute", e);
                    }
                }

                // Demarrer le timer 24h
                if (dungeonTimersStarted.add(parcelId)) {
                    EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
                        try {
                            Map<String, String> snap = dungeonSnapshots.get(parcelId);
                            World w = cachedWorlds.get(parcelId);
                            if (snap == null || snap.isEmpty() || w == null) return;

                            EldaniorLogger.info("[DungeonRegen] Regen auto " + parcel.getName() +
                                    " dans 10s (" + snap.size() + " blocs)");

                            // Avertissement
                            String zoneName = parcel.getName();
                            for (Map.Entry<UUID, Vector3d> e : PlayerPositionTracker.PLAYER_POSITIONS.entrySet()) {
                                try {
                                    Vector3d pos = e.getValue();
                                    if (pos != null && parcel.contains(pos.x, pos.y, pos.z)) {
                                        PlayerRef pRef = Universe.get().getPlayer(e.getKey());
                                        if (pRef != null) {
                                            NotificationHelper.sendNotification(pRef,
                                                    "<color:gold>Le donjon " + zoneName.replace('_', ' ') +
                                                            " se regenere dans 10s !</color>",
                                                    NotificationStyle.Warning);
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }

                            // Restaurer apres 10s
                            EldaniorLogger.SCHEDULER.schedule(() -> {
                                try {
                                    w.execute(() -> {
                                        int restored = 0;
                                        for (Map.Entry<String, String> entry : snap.entrySet()) {
                                            try {
                                                int[] coords = parseKey(entry.getKey());
                                                if (coords == null) continue;
                                                w.setBlock(coords[0], coords[1], coords[2], entry.getValue());
                                                restored++;
                                            } catch (Exception ex) {
                                                EldaniorLogger.error("DungeonRegen.restore", ex);
                                            }
                                        }
                                        EldaniorLogger.info("[DungeonRegen] " + parcel.getName() +
                                                " : " + restored + " blocs restaures !");
                                    });
                                } catch (Exception ex) {
                                    EldaniorLogger.error("DungeonRegen.execute", ex);
                                }
                            }, WARNING_BEFORE_SEC, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            EldaniorLogger.error("DungeonRegen.timer", e);
                        }
                    }, DUNGEON_REGEN_INTERVAL_MS, DUNGEON_REGEN_INTERVAL_MS, TimeUnit.MILLISECONDS);

                    EldaniorLogger.info("[DungeonRegen] Timer 24h demarre pour " + parcel.getName());
                }
            }
        }
    }

    // ==================== UTILS ====================

    private static String posKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private static int[] parseKey(String key) {
        try {
            String[] parts = key.split(",");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};
        } catch (Exception e) { return null; }
    }

    public static void resetSnapshot(String parcelId) {
        parcelSnapshots.remove(parcelId);
        pendingSnapshotRegen.remove(parcelId);
        lastScanTime.remove(parcelId);
        farmSnapshots.remove(parcelId);
    }

    public static void cleanup() {
        pendingSimpleRegen.clear();
        parcelSnapshots.clear();
        pendingSnapshotRegen.clear();
        lastScanTime.clear();
        farmSnapshots.clear();
        farmTimersStarted.clear();
        dungeonSnapshots.clear();
        dungeonTimersStarted.clear();
        cachedWorlds.clear();
    }

    public static int getPendingCount() {
        int total = pendingSimpleRegen.size();
        for (Set<String> s : pendingSnapshotRegen.values()) total += s.size();
        return total;
    }

    public static int getSnapshotCount() {
        return parcelSnapshots.size() + farmSnapshots.size() + dungeonSnapshots.size();
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
