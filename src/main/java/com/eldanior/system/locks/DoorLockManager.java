package com.eldanior.system.locks;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.DoorBlockUtils;
import com.hypixel.hytale.server.core.modules.interaction.DoorBlockUtils.DoorState;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Referme les portes ouvertes avec une cle : une salle au tresor ne reste pas ouverte derriere le Patriarche, et une porte de boss
 * se reverrouille pour le groupe suivant. La fermeture attend que personne ne soit a portee : une porte de boss n'enferme pas un
 * groupe en plein combat (on ne rouvre pas sans cle) ; une porte de tresor ne se rabat pas sur un joueur, mais pas au-dela de
 * FAMILY_HOLD_MS, sinon un voleur la garderait ouverte en restant devant.
 * Les portes ouvertes sont sauvegardees : un arret du serveur ne laisse pas une salle ouverte pour toujours.
 */
public final class DoorLockManager {

    private DoorLockManager() {}

    private static final long FAMILY_OPEN_MS = 12_000, BOSS_OPEN_MS = 300_000;
    private static final long FAMILY_HOLD_MS = 15_000;
    private static final double FAMILY_CLEAR_RANGE = 4, BOSS_CLEAR_RANGE = 40;
    /** Attente quand quelqu'un est encore la, ou quand le chunk n'est pas charge. */
    private static final long RETRY_MS = 3_000;
    /** Monde absent (instance fermee, monde pas encore charge) : on oublie la porte apres ce delai. */
    private static final long FORGET_MS = 3_600_000;
    /** Portee de recherche d'une porte collee, en blocs. */
    private static final int PARTNER_RANGE = 6;
    private static final String DENIED_SOUND = "SFX_GENERIC_CRAFTING_FAILED";

    /** @param forceAt heure a laquelle fermer meme si quelqu'un est la */
    private record OpenDoor(String world, int x, int y, int z, long closeAt, long forceAt, boolean boss) {
        String key() { return key(world, x, y, z); }
        static String key(String world, int x, int y, int z) { return world + "|" + x + "|" + y + "|" + z; }
        OpenDoor later(long at) { return new OpenDoor(world, x, y, z, at, forceAt, boss); }
    }

    private static final Map<String, OpenDoor> OPEN = new ConcurrentHashMap<>();
    private static Path file;

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("portes_ouvertes.txt");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                tick();
            } catch (Exception e) {
                EldaniorLogger.error("[Portes] boucle", e);
            }
        }, 5, 1, TimeUnit.SECONDS);
    }

    // ===== Etat d'une porte =====

    /** Bloc de base d'une porte visee sur l'une de ses cases de remplissage. */
    static Vector3i anchor(World world, Vector3i at) {
        int filler = WorldBlockUtil.getFiller(world, at.x(), at.y(), at.z());
        if (filler <= 0) return new Vector3i(at.x(), at.y(), at.z());
        return new Vector3i(at.x() - FillerBlockUtil.unpackX(filler), at.y() - FillerBlockUtil.unpackY(filler), at.z() - FillerBlockUtil.unpackZ(filler));
    }

    static boolean isOpen(BlockType type) {
        return DoorState.fromBlockState(type.getStateForBlock(type)) != DoorState.CLOSED;
    }

    /** La cle a deja servi pour cette ouverture : un second appui, ou une porte restee bloquee, n'en consomme pas une autre. */
    static boolean isUnlocked(World world, Vector3i door) {
        return OPEN.containsKey(OpenDoor.key(world.getName(), door.x(), door.y(), door.z()));
    }

    /** A appeler quand une cle ouvre la porte : programme sa fermeture. */
    static void unlocked(World world, Vector3i door, boolean boss) {
        // 2026-11-19 : porte du boss d'un donjon -> refermee seule apres 15 s, meme si des joueurs sont la (le boss attend ce moment)
        boolean gate = boss && !com.eldanior.system.boards.DungeonBosses.gatedBy(world.getName(), door).isEmpty();
        long closeAt = System.currentTimeMillis() + (gate ? com.eldanior.system.boards.DungeonBosses.GATE_MS : boss ? BOSS_OPEN_MS : FAMILY_OPEN_MS);
        OpenDoor open = new OpenDoor(world.getName(), door.x(), door.y(), door.z(), closeAt, gate ? closeAt : forceAt(closeAt, boss), boss);
        OPEN.put(open.key(), open);
        save();
    }

    private static long forceAt(long closeAt, boolean boss) {
        return boss ? Long.MAX_VALUE : closeAt + FAMILY_HOLD_MS;
    }

    // ===== Cles =====

    static boolean hasKey(Player player, String keyItem) {
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        for (short slot = 0; slot < bag.getCapacity(); slot++) {
            ItemStack stack = bag.getItemStack(slot);
            if (stack != null && !stack.isEmpty() && keyItem.equals(stack.getItemId())) return true;
        }
        return false;
    }

    static boolean takeKey(Player player, String keyItem) {
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        for (short slot = 0; slot < bag.getCapacity(); slot++) {
            ItemStack stack = bag.getItemStack(slot);
            if (stack != null && !stack.isEmpty() && keyItem.equals(stack.getItemId())) return bag.removeItemStackFromSlot(slot, 1).succeeded();
        }
        return false;
    }

    static void denied(PlayerRef pr, String message) {
        NotificationHelper.sendWarning(pr, message);
        int sound = SoundEvent.getAssetMap().getIndex(DENIED_SOUND);
        if (sound != 0) SoundUtil.playSoundEvent2dToPlayer(pr, sound, SoundCategory.SFX);
    }

    // ===== Fermeture =====

    private static void tick() {
        long now = System.currentTimeMillis();
        for (OpenDoor door : OPEN.values()) {
            if (now < door.closeAt) continue;
            World world = Universe.get().getWorld(door.world);
            if (world != null) world.execute(() -> closeWhenClear(world, door));
            else if (now - door.closeAt > FORGET_MS) forget(door);
        }
    }

    private static void closeWhenClear(World world, OpenDoor door) {
        if (OPEN.get(door.key()) != door) return;
        BlockType type = WorldBlockUtil.getBlockType(world, door.x, door.y, door.z);
        if (type == null) {                                                         // chunk non charge
            OPEN.put(door.key(), door.later(System.currentTimeMillis() + RETRY_MS));
            return;
        }
        DoorLock lock = DoorLock.of(type);
        if (lock == null || !isOpen(type)) {                                        // cassee, ou deja refermee a la main
            forget(door);
            return;
        }
        if (System.currentTimeMillis() < door.forceAt && playerNear(world.getName(), door, door.boss ? BOSS_CLEAR_RANGE : FAMILY_CLEAR_RANGE)) {
            OPEN.put(door.key(), door.later(System.currentTimeMillis() + RETRY_MS));
            return;
        }
        close(world, door.x, door.y, door.z, type);
        closePartner(world, door, lock);
        forget(door);
    }

    private static void close(World world, int x, int y, int z, BlockType type) {
        ChunkStore chunks = world.getChunkStore();
        String state = DoorBlockUtils.getInteractionState(DoorState.fromBlockState(type.getStateForBlock(type)), DoorState.CLOSED);
        BlockOperations.setBlockInteractionState(chunks, chunks.getChunkSectionReferenceAtBlock(x, y, z), x, y, z, type, state, false);
    }

    /** Deux portes collees s'ouvrent ensemble (DoorInteraction) : la voisine ne passe pas par notre evenement. */
    private static void closePartner(World world, OpenDoor door, DoorLock lock) {
        Set<Vector3i> done = new HashSet<>(List.of(new Vector3i(door.x, door.y, door.z)));
        int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] side : sides) {
            for (int step = 1; step <= PARTNER_RANGE; step++) {
                Vector3i cell = new Vector3i(door.x + side[0] * step, door.y, door.z + side[1] * step);
                Vector3i base = anchor(world, cell);
                BlockType type = WorldBlockUtil.getBlockType(world, base.x(), base.y(), base.z());
                if (type != null && lock.equals(DoorLock.of(type)) && isOpen(type) && done.add(base)) close(world, base.x(), base.y(), base.z(), type);
            }
        }
    }

    private static boolean playerNear(String world, OpenDoor door, double range) {
        Vector3d at = new Vector3d(door.x + 0.5, door.y + 0.5, door.z + 0.5);
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> ref = pr.getReference();
            World w = ref != null && ref.isValid() ? ref.getStore().getExternalData().getWorld() : null;
            Vector3d pos = w != null && w.getName().equals(world) ? PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid()) : null;
            if (pos != null && pos.distanceSquared(at) <= range * range) return true;
        }
        return false;
    }

    private static void forget(OpenDoor door) {
        OPEN.remove(door.key(), door);
        save();
    }

    // ===== Sauvegarde =====

    private static void save() {
        if (file == null) return;
        List<String> lines = new ArrayList<>();
        for (OpenDoor d : OPEN.values()) lines.add(d.world + "|" + d.x + "|" + d.y + "|" + d.z + "|" + d.boss);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            EldaniorLogger.error("[Portes] sauvegarde", e);
        }
    }

    /** Une porte ouverte a l'arret se referme des que possible apres le redemarrage. */
    private static void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] p = line.split("\\|");
                if (p.length != 5) continue;
                boolean boss = Boolean.parseBoolean(p[4]);
                long now = System.currentTimeMillis();
                OpenDoor door = new OpenDoor(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), now, forceAt(now, boss), boss);
                OPEN.put(door.key(), door);
            }
        } catch (IOException | NumberFormatException e) {
            EldaniorLogger.error("[Portes] chargement", e);
        }
    }
}
