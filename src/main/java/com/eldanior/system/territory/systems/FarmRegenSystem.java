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
 * - FARM / MINE : regen simple par bloc (le bloc casse par le joueur est restaure apres le delai)
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

    // ==================== DUNGEON : regen auto 24h ====================

    private static final Map<String, Map<String, String>> dungeonSnapshots = new ConcurrentHashMap<>();
    private static final Set<String> dungeonTimersStarted = ConcurrentHashMap.newKeySet();
    private static final long DUNGEON_REGEN_INTERVAL_MS = 24L * 60 * 60 * 1000; // 24h
    private static volatile World cachedWorld = null;

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

        // Stocker la reference au monde pour les timers donjon
        cachedWorld = world;

        if (type == ParcelType.FARM || type == ParcelType.MINE) {
            handleSimpleRegen(target, worldName, parcel, world);
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

        World world = cachedWorld;
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
    public static void initDungeonTimers(World world) {
        cachedWorld = world;
        // Les timers demarreront au premier break dans chaque donjon.
        // Pour forcer l'init au demarrage, on pourrait scanner toutes les parcelles DUNGEON ici.
        for (ParcelData parcel : ParcelManager.getAll()) {
            if (parcel.getType() == ParcelType.DUNGEON) {
                String parcelId = parcel.getId();
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
                            if (snap == null || snap.isEmpty() || cachedWorld == null) return;

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
                                    cachedWorld.execute(() -> {
                                        int restored = 0;
                                        for (Map.Entry<String, String> entry : snap.entrySet()) {
                                            try {
                                                int[] coords = parseKey(entry.getKey());
                                                if (coords == null) continue;
                                                cachedWorld.setBlock(coords[0], coords[1], coords[2], entry.getValue());
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
    }

    public static void cleanup() {
        pendingSimpleRegen.clear();
        parcelSnapshots.clear();
        pendingSnapshotRegen.clear();
        lastScanTime.clear();
        dungeonSnapshots.clear();
        dungeonTimersStarted.clear();
    }

    public static int getPendingCount() {
        int total = pendingSimpleRegen.size();
        for (Set<String> s : pendingSnapshotRegen.values()) total += s.size();
        return total;
    }

    public static int getSnapshotCount() {
        return parcelSnapshots.size();
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
