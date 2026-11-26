package com.eldanior.system.territory.systems;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mur du royaume, comme la barriere du donjon Deepworks : blocs vanilla "Barrier" (solides, invisibles) sur toute
 * la hauteur, juste a l'exterieur du royaume, + "VFX_Eldanior_Kingdom_Barrier" (copie violette de
 * VFX_Dungeon_Goblin_Energy_Barrier : panneau d'energie de 15x15, visible de pres) sur le bord interieur.
 *
 * Un royaume de 10000x10000 = 40 000 colonnes : le mur se construit au fur et a mesure, sur les chunks charges
 * autour des joueurs qui approchent de la frontiere (BUILD_RANGE), avec un budget de colonnes par seconde.
 * La teleportation de ParcelRangeSystem reste le filet de securite.
 *
 * Active / desactive par royaume (/es royaume barriere on|off), sauvegarde dans kingdom_barriers.properties.
 * Desactiver retire le mur de la meme facon, progressivement autour des joueurs.
 */
public class KingdomBarrierSystem extends EntityTickingSystem<EntityStore> {

    public static final String BARRIER = "Barrier";
    public static final String VFX = "VFX_Eldanior_Kingdom_Barrier";
    /** Ancienne version jaune (premier patch) : retiree au passage. */
    private static final String LEGACY_VFX = "VFX_Dungeon_Goblin_Energy_Barrier";
    private static final String EMPTY = "Empty";

    private static final int WORLD_MIN_Y = 0;
    private static final int WORLD_MAX_Y = 319;
    /** Distance a la frontiere a partir de laquelle on construit, et demi-longueur du troncon construit. */
    private static final int BUILD_RANGE = 64;
    /** Colonnes traitees par joueur et par seconde. */
    private static final int COLUMNS_PER_TICK = 24;
    /** Particules : un panneau de 15x15 toutes les VFX_SPACING colonnes, centre a ces hauteurs au-dessus du sol. */
    private static final int VFX_SPACING = 12;
    private static final int[] VFX_HEIGHTS = {7};
    /** Rotation du bloc de particules (0..3, NESW) pour que le panneau suive le mur : cotes nord/sud, cotes ouest/est. */
    private static final int VFX_ROT_ALONG_X = 0;
    private static final int VFX_ROT_ALONG_Z = 1;

    private static final Map<String, Boolean> enabled = new ConcurrentHashMap<>();
    /** Colonnes deja traitees (construites ou retirees selon le mode), par royaume ; videes a chaque bascule. */
    private static final Map<String, Set<Long>> done = new ConcurrentHashMap<>();
    private static Path file;

    private float timer = 0;
    private boolean shouldUpdate = false;

    // ==================== ETAT ====================

    public static void init(Path dataDir) {
        file = dataDir.resolve("kingdom_barriers.properties");
        if (!Files.exists(file)) return;
        Properties props = new Properties();
        try (Reader r = Files.newBufferedReader(file)) {
            props.load(r);
            for (String id : props.stringPropertyNames()) enabled.put(id, Boolean.parseBoolean(props.getProperty(id)));
        } catch (IOException e) {
            EldaniorLogger.error("[Barriere] lecture " + file, e);
        }
    }

    private static void save() {
        if (file == null) return;
        Properties props = new Properties();
        enabled.forEach((id, on) -> props.setProperty(id, String.valueOf(on)));
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file)) { props.store(w, "Mur des royaumes (id -> true/false)"); }
        } catch (IOException e) {
            EldaniorLogger.error("[Barriere] ecriture " + file, e);
        }
    }

    public static boolean isEnabled(String kingdomId) {
        return Boolean.TRUE.equals(enabled.get(kingdomId));
    }

    public static void setEnabled(String kingdomId, boolean on) {
        enabled.put(kingdomId, on);
        done.remove(kingdomId);
        save();
    }

    /** Royaume contenant cette colonne (x, z), sans tenir compte de la hauteur. */
    public static ParcelData kingdomAt(String world, int x, int z) {
        for (ParcelData k : ParcelManager.getByType(ParcelType.KINGDOM)) {
            if (world.equals(k.getWorld()) && x >= k.getMinX() && x <= k.getMaxX() && z >= k.getMinZ() && z <= k.getMaxZ()) return k;
        }
        return null;
    }

    // ==================== TICK ====================

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        if (index == 0) {
            timer += dt;
            shouldUpdate = timer >= 1.0f;
            if (shouldUpdate) timer = 0;
        }
        if (!shouldUpdate || enabled.isEmpty()) return;

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;
        Player player = store.getComponent(ref, Player.getComponentType());
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (player == null || transform == null) return;
        World world = player.getWorld();
        if (world == null) return;

        try {
            Vector3d pos = transform.getPosition();
            int px = (int) Math.floor(pos.x), pz = (int) Math.floor(pos.z);
            int[] budget = {COLUMNS_PER_TICK};
            List<ParcelData> kingdoms = ParcelManager.getByType(ParcelType.KINGDOM);
            for (ParcelData k : kingdoms) {
                if (!enabled.containsKey(k.getId()) || !world.getName().equals(k.getWorld())) continue;
                processKingdom(world, k, px, pz, budget);
                if (budget[0] <= 0) return;
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Barriere] tick", e);
        }
    }

    private static void processKingdom(World world, ParcelData k, int px, int pz, int[] budget) {
        int minX = k.getMinX(), maxX = k.getMaxX(), minZ = k.getMinZ(), maxZ = k.getMaxZ();
        // Joueur trop loin de toute frontiere
        if (px < minX - BUILD_RANGE || px > maxX + BUILD_RANGE || pz < minZ - BUILD_RANGE || pz > maxZ + BUILD_RANGE) return;

        boolean build = isEnabled(k.getId());
        Set<Long> cols = done.computeIfAbsent(k.getId(), id -> ConcurrentHashMap.newKeySet());

        // Ouest / Est : colonnes x fixes, z de minZ-1 a maxZ+1 (coins inclus) ; particules en x = minX / maxX
        if (Math.abs(px - (minX - 1)) <= BUILD_RANGE)
            side(world, k, cols, build, minX - 1, true, pz, minZ - 1, maxZ + 1, minX, budget);
        if (Math.abs(px - (maxX + 1)) <= BUILD_RANGE)
            side(world, k, cols, build, maxX + 1, true, pz, minZ - 1, maxZ + 1, maxX, budget);
        // Nord / Sud : colonnes z fixes, x de minX a maxX ; particules en z = minZ / maxZ
        if (Math.abs(pz - (minZ - 1)) <= BUILD_RANGE)
            side(world, k, cols, build, minZ - 1, false, px, minX, maxX, minZ, budget);
        if (Math.abs(pz - (maxZ + 1)) <= BUILD_RANGE)
            side(world, k, cols, build, maxZ + 1, false, px, minX, maxX, maxZ, budget);
    }

    /**
     * Un cote du mur. fixed = x (alongZ) ou z de la ligne du mur, inner = meme coordonnee pour la ligne de particules,
     * along = coordonnee du joueur le long du cote, [from, to] = etendue du cote.
     */
    private static void side(World world, ParcelData k, Set<Long> cols, boolean build, int fixed, boolean alongZ,
                             int along, int from, int to, int inner, int[] budget) {
        int start = Math.max(from, along - BUILD_RANGE), end = Math.min(to, along + BUILD_RANGE);
        for (int a = start; a <= end && budget[0] > 0; a++) {
            int x = alongZ ? fixed : a, z = alongZ ? a : fixed;
            long key = ((long) x << 32) | (z & 0xffffffffL);
            if (cols.contains(key)) continue;
            // Royaume voisin colle a celui-ci : pas de mur entre les deux
            ParcelData other = kingdomAt(k.getWorld(), x, z);
            if (other != null && other != k) { cols.add(key); continue; }

            budget[0]--;
            boolean corner = a < from + 1 || a > to - 1;
            boolean vfx = !corner && Math.floorMod(a, VFX_SPACING) == 0;
            int ix = alongZ ? inner : x, iz = alongZ ? z : inner;
            int rot = alongZ ? VFX_ROT_ALONG_Z : VFX_ROT_ALONG_X;
            boolean ok = build ? buildColumn(world, x, z, ix, iz, vfx, rot)
                               : clearColumn(world, x, z, ix, iz);
            if (ok) cols.add(key);
        }
    }

    // ==================== BLOCS ====================

    /** Remplit de Barrier toutes les cases non solides de la colonne. false si le chunk n'est pas charge. */
    private static boolean buildColumn(World world, int x, int z, int ix, int iz, boolean vfx, int rot) {
        if (WorldBlockUtil.getBlockType(world, x, WORLD_MIN_Y, z) == null) return false;
        for (int y = WORLD_MIN_Y; y <= WORLD_MAX_Y; y++) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            if (t == null) break; // au-dessus de la hauteur du monde
            if (t.getMaterial() == BlockMaterial.Solid || BARRIER.equals(t.getId())) continue;
            if (WorldBlockUtil.getFiller(world, x, y, z) != 0) continue; // partie d'un bloc multi-cases
            WorldBlockUtil.setBlock(world, x, y, z, BARRIER);
        }
        clearVfx(world, ix, iz, LEGACY_VFX);
        if (vfx) placeVfx(world, ix, iz, rot);
        return true;
    }

    private static void placeVfx(World world, int x, int z, int rot) {
        int ground = surface(world, x, z);
        if (ground == Integer.MIN_VALUE) return;
        for (int h : VFX_HEIGHTS) {
            int y = ground + h;
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            if (t != null && EMPTY.equals(t.getId())) WorldBlockUtil.setBlock(world, x, y, z, VFX, rot);
        }
    }

    /** Plus haut bloc solide de la colonne, Integer.MIN_VALUE si aucun / non charge. */
    private static int surface(World world, int x, int z) {
        for (int y = WORLD_MAX_Y; y >= WORLD_MIN_Y; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            if (t != null && t.getMaterial() == BlockMaterial.Solid && !BARRIER.equals(t.getId())) return y;
        }
        return Integer.MIN_VALUE;
    }

    /** Retire Barrier (ligne du mur) et particules (ligne interieure). false si le chunk n'est pas charge. */
    private static boolean clearColumn(World world, int x, int z, int ix, int iz) {
        if (WorldBlockUtil.getBlockType(world, x, WORLD_MIN_Y, z) == null) return false;
        for (int y = WORLD_MIN_Y; y <= WORLD_MAX_Y; y++) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            if (t == null) break;
            if (BARRIER.equals(t.getId())) WorldBlockUtil.setBlock(world, x, y, z, EMPTY);
        }
        clearVfx(world, ix, iz, VFX);
        clearVfx(world, ix, iz, LEGACY_VFX);
        return true;
    }

    private static void clearVfx(World world, int x, int z, String id) {
        for (int y = WORLD_MIN_Y; y <= WORLD_MAX_Y; y++) {
            BlockType v = WorldBlockUtil.getBlockType(world, x, y, z);
            if (v == null) break;
            if (id.equals(v.getId())) WorldBlockUtil.setBlock(world, x, y, z, EMPTY);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
