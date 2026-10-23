package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Porte de Niflheim — sort divin de glace (Rune_PorteNiflheim, ex-"Porte de Niflheim").
 *
 * Une onde de givre part du lanceur et s'etend jusqu'a {@link #RADIUS} blocs et gele TOUT ce qu'elle touche :
 * sols et toits deviennent neige / glace / permafrost, les murs se couvrent de glace, les arbres sont pris dans la glace
 * (troncs de glace, feuillage enneige, stalactites sous les branches), l'herbe se givre ; le paysage se couvre de pics
 * et de stalagmites de glace, de tas de neige et d'eclats de glace. Chaque ennemi atteint est gele et immobilise
 * ({@link #FREEZE_EFFECT}, 20 s), emprisonne dans des piques de glace jusqu'a la fin du gel. Tant que le givre est
 * present, tout ennemi dans la zone subit l'engelure : 2 degats de glace par seconde, vitesse et endurance -50 %.
 *
 * Mise en scene : au lancement, une porte divine de glace (cadre sculpte + battants de la porte vanilla du chateau
 * gele) apparait dans les airs au-dessus du centre de la zone et s'ouvre sur un vortex glace ; l'onde de givre ne part
 * qu'une fois les battants ouverts ({@link #GATE_OPEN_MS}). La brume glacee
 * de Niflheim recouvre la zone, des stalactites de glace tombent du ciel sur les ennemis pendant {@link #ICICLE_PHASE_MS},
 * et au degel la glace eclate en vagues de particules.
 * Briser la glace : un ennemi emprisonne subit +50 % de degats (hors glace) ; le coup fait exploser sa prison
 * (degats de glace + eclats) et le libere.
 * {@link #RESTORE_AFTER_MS} apres le lancement, le terrain est restaure a l'identique.
 *
 * Securites :
 *  - aucune modification dans une ville, une parcelle de joueur, une zone de recolte, une arene, un donjon ou une mine
 *    (les royaumes et territoires, qui couvrent toute la carte, restent autorises) ;
 *  - les blocs a contenu ou fonctionnels (coffres, etablis, portes, panneaux, lits...) et les parties de blocs
 *    multi-cases ne sont jamais remplaces (leur contenu serait perdu) ; l'orientation d'origine est restauree ;
 *  - a la restauration, un bloc n'est remis que s'il est encore celui pose par le sort (ou vide) : une construction
 *    posee entre-temps par un joueur n'est jamais ecrasee ;
 *  - les blocs a restaurer sont sauvegardes sur disque : si le serveur s'arrete avant le degel, la restauration
 *    a lieu au redemarrage.
 */
public final class NiflheimGate {

    private NiflheimGate() {}

    // ===== Reglages =====
    static final double RADIUS = SpellFx.DIVINE_ZONE_RADIUS;
    private static final double WAVE_SPEED = 15.0;          // blocs/s : l'onde atteint 35 blocs en ~2.3 s
    private static final long RESTORE_AFTER_MS = 180_000;   // le givre disparait 3 min apres le lancement
    private static final int SCAN_UP = 18, SCAN_DOWN = 10;  // du haut des arbres / toits jusque sous la surface
    private static final double ICE_CHANCE = 0.25;          // part de glace dans le sol gele
    private static final double PERMAFROST_CHANCE = 0.15;   // part de permafrost
    // Decor pose sur la surface gelee (tirage unique par colonne, dans cet ordre)
    private static final double SPIKE_CHANCE = 0.03;        // pic de glace (colonne de glace + pointe)
    private static final double STALAGMITE_CHANCE = 0.06;   // stalagmite de glace
    private static final double SNOW_PILE_CHANCE = 0.09;    // tas de neige
    private static final double RUBBLE_CHANCE = 0.05;       // eclats de glace au sol
    private static final double FROST_GRASS_CHANCE = 0.22;  // herbe givree
    private static final double ICICLE_CHANCE = 0.25;       // stalactite sous une branche / un surplomb
    private static final String SNOW = "Soil_Snow";
    private static final String ICE = "Rock_Ice";
    private static final String PERMAFROST = "Rock_Ice_Permafrost";
    private static final String ICE_TRUNK = "Wood_Ice_Trunk";
    private static final String SNOW_LEAVES = "Plant_Leaves_Snow";
    private static final String FROST_GRASS = "Plant_Grass_Snowy_Short";
    private static final String SNOW_HALF = "Soil_Snow_Half";
    private static final String STALAGMITE_LARGE = "Rock_Ice_Stalactite_Large";
    private static final String STALAGMITE_SMALL = "Rock_Ice_Icicles";
    private static final String[] RUBBLE = {"Rubble_Ice", "Rubble_Ice_Medium"};
    /** Rotation "tete en bas" : stalactite accrochee sous un bloc. */
    private static final int UPSIDE_DOWN = RotationTuple.index(Rotation.None, Rotation.OneEighty, Rotation.None);
    private static final String EMPTY = "Empty";
    static final String FREEZE_EFFECT = "Niflheim_Gel";

    // ===== Assets =====
    private static final String FX_RING = "Niflheim_Ring";
    private static final String FX_SNOW = "Niflheim_Flurry";        // bourrasque de neige (duree finie ; Snow_Storm vanilla boucle)
    private static final String FX_FREEZE = "Niflheim_Freeze_Burst"; // eclats de glace (duree finie)
    private static final long FREEZE_MS = 20_000;
    private static final String SFX_CAST = "SFX_Golem_Frost_Slam_Impact";
    private static final String SFX_FREEZE = "SFX_Z3_Emit_Cave_Ice_Crackle";
    private static final String SFX_ICE = "SFX_Ice_Build";
    // Engelure : zone active jusqu'au degel
    private static final String FROSTBITE_EFFECT = "Niflheim_Engelure";
    private static final float FROSTBITE_DAMAGE = 2f;       // par seconde
    private static final long FROSTBITE_EVERY_MS = 1000;
    private static final long FLURRY_EVERY_MS = 1500;       // bourrasques de neige d'ambiance dans la zone
    // Porte, brume, stalactites, bris de glace
    private static final String GATE_PROJECTILE = "Projectile_Config_Niflheim_Gate";
    private static final String PORTAL_PROJECTILE = "Projectile_Config_Niflheim_Portal";
    private static final double GATE_HEIGHT = 12.0;           // la porte flotte au-dessus du centre de la zone
    private static final double PORTAL_IN_GATE = 3.3;         // centre de l'ouverture au-dessus du socle (70 unites x echelle 3 / 64 unites par bloc)
    static final long GATE_OPEN_MS = 2_200;                   // la porte apparait, s'ouvre, puis l'onde part
    private static final String FX_MIST = "Niflheim_Mist";
    private static final double MIST_SPACING = 8.0;
    private static final long MIST_EVERY_MS = 28_000;          // la tache de brume dure 30 s
    private static final String ICICLE_PROJECTILE = "Projectile_Config_Niflheim_Icicle";
    static final long ICICLE_PHASE_MS = 60_000;                // tempete d'eclats : premiere minute
    private static final long ICICLE_EVERY_MS = 1_400;
    private static final double ICICLE_HEIGHT = 18.0;
    private static final float ICICLE_DAMAGE = 150f;
    private static final double ICICLE_RADIUS = 2.2;
    private static final float SHATTER_MULTIPLIER = 1.5f;      // degats subis par un ennemi emprisonne
    private static final float SHATTER_BURST = 400f;          // explosion de la prison
    private static final double SHATTER_SPLASH = 3.0;
    private static final String PRISON_PROJECTILE = "Projectile_Config_Niflheim_Ice_Prison";
    private static final int FRONT_BURSTS = 7;              // eclats de glace sur le front de l'onde, par tick

    /** Vegetation recouverte par le givre (retiree puis replantee au degel). */
    private static final String[] FOLIAGE_KEYWORDS = {"Plant", "Flower", "Fern", "Mushroom", "Bush", "Grass_Tall", "Weed", "Reed"};

    // ===== Etat =====
    static final class Wave {
        final World world;
        final Vector3d center;
        final long startAt;
        double doneRadius;
        boolean released;
        int frozenBlocks;
        final Set<Ref<EntityStore>> frozen = new HashSet<>();
        final TerrainRestore.Restore restore;
        Wave(World world, Vector3d center, long startAt) {
            this.world = world; this.center = center; this.startAt = startAt;
            this.restore = TerrainRestore.open(world, startAt + RESTORE_AFTER_MS);
        }
    }

    private static final Map<UUID, List<Wave>> waves = new ConcurrentHashMap<>();
    /** Lance l'onde de givre autour du lanceur. Appele depuis l'interaction (thread du monde). */
    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        Vector3d pos = SpellFx.position(caster, store);
        if (uuid == null || pos == null) return;
        World world = SpellFx.world(store);
        // La porte apparait et s'ouvre (tickZones), puis l'onde de givre part une fois les battants ouverts
        long now = System.currentTimeMillis();
        Wave w = new Wave(world, pos, now + GATE_OPEN_MS);
        waves.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(w);
        zones.add(new Zone(caster, world.getName(), pos, SpellFx.lookDirection(caster, store), w.startAt, w.restore.restoreAt));
    }

    /** Effets de depart de l'onde (a l'ouverture de la porte). */
    private static void release(Wave w, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = w.center;
        SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), (float) (RADIUS / 3.0), store, cb);
        SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.6, 0), (float) (RADIUS / 6.0), store, cb);
        SpellFx.fx(FX_FREEZE, new Vector3d(pos).add(0, 1, 0), 5.0f, store, cb);
        SpellFx.fx(FX_FREEZE, new Vector3d(pos).add(0, GATE_HEIGHT + PORTAL_IN_GATE, 0), 6.0f, store, cb);
        SpellFx.fx(FX_SNOW, new Vector3d(pos).add(0, 6, 0), 4.0f, store, cb);
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            SpellFx.fx(FX_SNOW, new Vector3d(pos).add(Math.cos(a) * RADIUS * 0.6, 5, Math.sin(a) * RADIUS * 0.6), 3.0f, store, cb);
        }
        SpellFx.sound(SFX_CAST, pos, cb);
        SpellFx.sound(SFX_FREEZE, pos, cb);
        SpellFx.shake(pos, RADIUS * 2, "Impact_Strong", store);
    }

    // ===================== ONDE DE GIVRE =====================

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Wave> list = waves.get(uuid);
        if (list != null) {
            long now = System.currentTimeMillis();
            for (Wave w : list) {
                if (tickWave(caster, w, now, store, cb)) list.remove(w);
            }
            if (list.isEmpty()) waves.remove(uuid);
        }
    }

    /** Retourne true quand l'onde a atteint son rayon maximal. */
    private static boolean tickWave(Ref<EntityStore> caster, Wave w, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now < w.startAt) return false;                 // la porte s'ouvre encore
        if (!w.released) { w.released = true; release(w, store, cb); }
        double radius = Math.min(RADIUS, WAVE_SPEED * (now - w.startAt) / 1000.0);
        if (radius <= w.doneRadius) return false;

        // Sol : anneau (doneRadius, radius], applique hors du tick ECS (comme FarmRegen) via world.execute
        final double inner = w.doneRadius, outer = radius;
        final boolean last = radius >= RADIUS;
        w.world.execute(() -> {
            try {
                freezeRing(w, inner, outer);
                if (last) {
                    EldaniorLogger.info("[PorteNiflheim] " + w.frozenBlocks + " bloc(s) geles autour de ("
                            + (int) w.center.x + ", " + (int) w.center.y + ", " + (int) w.center.z + ")");
                    TerrainRestore.save();
                }
            } catch (Exception e) {
                EldaniorLogger.error("PorteNiflheim: gel du sol", e);
            }
        });
        SpellFx.sound(SFX_ICE, w.center, cb);
        // Front de l'onde : eclats de glace qui explosent tout autour
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < FRONT_BURSTS; i++) {
            double a = rng.nextDouble(Math.PI * 2);
            Vector3d at = new Vector3d(w.center).add(Math.cos(a) * radius, 0.5, Math.sin(a) * radius);
            SpellFx.fx(FX_FREEZE, at, 1.6f, store, cb);
        }
        if (rng.nextInt(3) == 0) SpellFx.sound(SFX_FREEZE, new Vector3d(w.center).add(radius, 0, 0), cb);

        // Entites : chaque ennemi atteint par l'onde est gele
        for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, w.center, radius + 1, store)) {
            if (w.frozen.contains(target)) continue;
            w.frozen.add(target);
            Vector3d p = SpellFx.position(target, store);
            if (p != null) SpellFx.fx(FX_FREEZE, new Vector3d(p).add(0, 1, 0), 1.5f, store, cb);
            SpellFx.effect(target, FREEZE_EFFECT, cb);
            SpellFx.lockInPlace(target, FREEZE_MS, store); // les monstres aussi : position figee a chaque tick
            imprison(caster, target, now + FREEZE_MS, store, cb);
        }

        w.doneRadius = radius;
        return radius >= RADIUS;
    }

    /** Gele les colonnes de l'anneau (inner, outer]. A appeler sur le thread du monde, hors tick ECS. */
    private static void freezeRing(Wave w, double inner, double outer) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int r = (int) Math.ceil(outer);
        int cx = (int) Math.floor(w.center.x), cz = (int) Math.floor(w.center.z), cy = (int) Math.floor(w.center.y);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.hypot(dx, dz);
                if (d > outer || d <= inner) continue;
                if (freezeColumn(w, cx + dx, cy, cz + dz, rng)) w.frozenBlocks++;
            }
        }
    }

    /**
     * Gele une colonne de haut en bas : chaque bloc expose a l'air (dessus ou cote) est gele — feuillage enneige,
     * troncs de glace, herbe givree, surfaces en neige / glace / permafrost, murs couverts de glace. La premiere
     * surface rencontree recoit le decor (pics, stalagmites, tas de neige, eclats). Retourne true si un bloc a change.
     */
    private static boolean freezeColumn(Wave w, int x, int baseY, int z, ThreadLocalRandom rng) {
        if (TerrainRestore.isProtectedArea(w.world.getName(), x, baseY, z)) return false;
        boolean changed = false, decorated = false;
        boolean openAbove = isEmpty(w.world, x, baseY + SCAN_UP + 1, z);
        for (int y = baseY + SCAN_UP; y >= baseY - SCAN_DOWN; y--) {
            BlockType type = WorldBlockUtil.getBlockType(w.world, x, y, z);
            if (type == null) return changed; // section non chargee
            String id = type.getId();
            if (type == BlockType.EMPTY || id == null || id.equals(EMPTY)) { openAbove = true; continue; }
            boolean wasOpen = openAbove;
            openAbove = false;
            if (TerrainRestore.isKept(w.world, id, x, y, z)) continue;
            if (isFrozen(id)) { if (wasOpen) decorated = true; continue; }
            if (id.contains("Leaves")) {                  // feuillage : enneige, stalactites dessous
                changed |= place(w, x, y, z, id, SNOW_LEAVES);
                hangIcicle(w, x, y - 1, z, rng);
                continue;
            }
            if (id.startsWith("Wood_") && id.contains("Trunk")) {   // arbre : tronc pris dans la glace
                changed |= place(w, x, y, z, id, ICE_TRUNK);
                continue;
            }
            if (contains(id, FOLIAGE_KEYWORDS)) {         // vegetation basse : givree (laisse passer l'air)
                changed |= place(w, x, y, z, id, FROST_GRASS);
                openAbove = true;
                continue;
            }
            if (wasOpen) {                                // surface (sol, toit) : neige / glace / permafrost
                double roll = rng.nextDouble();
                String ground = roll < ICE_CHANCE ? ICE : roll < ICE_CHANCE + PERMAFROST_CHANCE ? PERMAFROST : SNOW;
                changed |= place(w, x, y, z, id, ground);
                if (!decorated) { decorated = true; decorate(w, x, y + 1, z, ground, rng); }
            } else if (exposedSideways(w.world, x, y, z)) {   // mur : couvert de glace
                changed |= place(w, x, y, z, id, rng.nextDouble() < 0.7 ? ICE : PERMAFROST);
            }
            if (isEmpty(w.world, x, y - 1, z) && rng.nextDouble() < ICICLE_CHANCE * 0.4) hangIcicle(w, x, y - 1, z, rng);
        }
        return changed;
    }

    private static boolean isFrozen(String id) {
        return id.equals(SNOW) || id.equals(ICE) || id.equals(PERMAFROST) || id.equals(SNOW_LEAVES) || id.equals(ICE_TRUNK)
                || id.equals(FROST_GRASS) || id.equals(SNOW_HALF) || id.startsWith("Rock_Ice") || id.startsWith("Rubble_Ice");
    }

    /** Decor sur la surface gelee (le bloc `y` doit etre vide). */
    private static void decorate(Wave w, int x, int y, int z, String ground, ThreadLocalRandom rng) {
        if (!isEmpty(w.world, x, y, z)) return;
        double roll = rng.nextDouble();
        if ((roll -= SPIKE_CHANCE) < 0) spike(w, x, y, z, rng);
        else if ((roll -= STALAGMITE_CHANCE) < 0) place(w, x, y, z, EMPTY, rng.nextBoolean() ? STALAGMITE_LARGE : STALAGMITE_SMALL);
        else if ((roll -= SNOW_PILE_CHANCE) < 0) {
            boolean big = rng.nextDouble() < 0.4 && isEmpty(w.world, x, y + 1, z);
            place(w, x, y, z, EMPTY, big ? SNOW : SNOW_HALF);
            if (big) place(w, x, y + 1, z, EMPTY, SNOW_HALF);
        }
        else if ((roll -= RUBBLE_CHANCE) < 0) place(w, x, y, z, EMPTY, RUBBLE[rng.nextInt(RUBBLE.length)]);
        else if ((roll -= FROST_GRASS_CHANCE) < 0 && ground.equals(SNOW)) place(w, x, y, z, EMPTY, FROST_GRASS);
    }

    /** Pic de glace de 2 a 6 blocs termine par une pointe, uniquement dans l'air libre. */
    private static void spike(Wave w, int x, int y, int z, ThreadLocalRandom rng) {
        int height = 2 + rng.nextInt(5);
        for (int i = 0; i < height; i++) {
            if (!isEmpty(w.world, x, y + i, z)) return;
            place(w, x, y + i, z, EMPTY, i == height - 1 ? STALAGMITE_LARGE : rng.nextDouble() < 0.25 ? PERMAFROST : ICE);
        }
    }

    /** Stalactite de glace accrochee sous un bloc (si la case est vide). */
    private static void hangIcicle(Wave w, int x, int y, int z, ThreadLocalRandom rng) {
        if (rng.nextDouble() >= ICICLE_CHANCE || !isEmpty(w.world, x, y, z)) return;
        place(w, x, y, z, EMPTY, rng.nextDouble() < 0.3 ? STALAGMITE_LARGE : STALAGMITE_SMALL, UPSIDE_DOWN);
    }

    private static boolean exposedSideways(World world, int x, int y, int z) {
        return isEmpty(world, x + 1, y, z) || isEmpty(world, x - 1, y, z) || isEmpty(world, x, y, z + 1) || isEmpty(world, x, y, z - 1);
    }

    private static boolean place(Wave w, int x, int y, int z, String original, String frozen) {
        return place(w, x, y, z, original, frozen, 0);
    }

    /** Pose un bloc gele et memorise l'original (et son orientation) pour le degel. */
    private static boolean place(Wave w, int x, int y, int z, String original, String frozen, int rotation) {
        return TerrainRestore.place(w.world, w.restore, x, y, z, original, frozen, rotation);
    }

    private static boolean isEmpty(World world, int x, int y, int z) { return TerrainRestore.isEmpty(world, x, y, z); }

    // ===================== ENGELURE =====================

    static final class Zone {
        final Ref<EntityStore> caster;
        final String world;
        final Vector3d center, fwd;
        final long startAt, until;
        long lastPulse, lastFlurry, lastMist, lastIcicle;
        Ref<EntityStore> gate, portal;
        boolean gateSpawned;
        Zone(Ref<EntityStore> caster, String world, Vector3d center, Vector3d look, long startAt, long until) {
            this.caster = caster; this.world = world; this.center = center; this.startAt = startAt; this.until = until;
            this.fwd = new Vector3d(look.x, 0, look.z);
            if (fwd.lengthSquared() < 1e-6) fwd.set(0, 0, 1);
            fwd.normalize();
        }
    }

    /** Stalactite de glace en chute. */
    static final class Icicle {
        final Ref<EntityStore> ref, caster;
        final long spawnedAt;
        Vector3d lastPos;
        boolean spawned;
        Icicle(Ref<EntityStore> ref, Ref<EntityStore> caster, long spawnedAt) { this.ref = ref; this.caster = caster; this.spawnedAt = spawnedAt; }
    }

    private static final List<Icicle> icicles = new CopyOnWriteArrayList<>();

    private static final List<Zone> zones = new CopyOnWriteArrayList<>();

    /** Chaque seconde : engelure (degats de glace + lenteur + endurance reduite) pour les ennemis dans une zone gelee. */
    static void tickZones(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (zones.isEmpty()) return;
        long now = System.currentTimeMillis();
        String worldName = SpellFx.world(store).getName();
        for (Zone z : zones) {
            if (!worldName.equals(z.world)) continue;
            if (now >= z.until) { zones.remove(z); thaw(z, store, cb); continue; }
            boolean casterOk = z.caster.isValid() && z.caster.getStore() == store;
            // La porte apparait dans les airs au-dessus du centre, face au lanceur (spawn differe : il faut un CommandBuffer)
            if (!z.gateSpawned && casterOk) {
                z.gateSpawned = true;
                Vector3d at = new Vector3d(z.center).add(0, GATE_HEIGHT, 0);
                z.gate = spawnFixed(z.caster, GATE_PROJECTILE, at, z.fwd, cb);
                z.portal = spawnFixed(z.caster, PORTAL_PROJECTILE, new Vector3d(at).add(0, PORTAL_IN_GATE, 0), z.fwd, cb);
                SpellFx.fx(FX_FREEZE, new Vector3d(at).add(0, PORTAL_IN_GATE, 0), 5.0f, store, cb);
                SpellFx.fx(FX_SNOW, new Vector3d(at).add(0, PORTAL_IN_GATE, 0), 3.0f, store, cb);
                SpellFx.sound(SFX_CAST, at, cb);
            }
            for (Ref<EntityStore> m : new Ref[]{z.gate, z.portal}) {
                if (m == null || !m.isValid()) continue;
                Velocity gv = store.getComponent(m, Velocity.getComponentType());
                if (gv != null) gv.setZero();
            }
            // Brume glacee sur toute la zone
            if (now - z.lastMist >= MIST_EVERY_MS) {
                z.lastMist = now;
                int steps = (int) Math.ceil(RADIUS / MIST_SPACING);
                for (int i = -steps; i <= steps; i++) {
                    for (int j = -steps; j <= steps; j++) {
                        double dx = i * MIST_SPACING, dz = j * MIST_SPACING;
                        if (Math.hypot(dx, dz) > RADIUS) continue;
                        SpellFx.fx(FX_MIST, SpellFx.ground(SpellFx.world(store), new Vector3d(z.center).add(dx, 0, dz)), 1.0f, store, cb);
                    }
                }
            }
            // Tempete d'eclats : stalactites de glace sur les ennemis (premiere minute)
            if (casterOk && now - z.startAt < ICICLE_PHASE_MS && now - z.lastIcicle >= ICICLE_EVERY_MS) {
                z.lastIcicle = now;
                dropIcicle(z, store, cb, now);
            }
            if (now - z.lastFlurry >= FLURRY_EVERY_MS) {
                z.lastFlurry = now;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                double r = RADIUS * Math.sqrt(rng.nextDouble()), a = rng.nextDouble(Math.PI * 2);
                SpellFx.fx(FX_SNOW, new Vector3d(z.center).add(Math.cos(a) * r, 4, Math.sin(a) * r), 2.0f, store, cb);
            }
            if (now - z.lastPulse < FROSTBITE_EVERY_MS) continue;
            z.lastPulse = now;
            boolean casterHere = z.caster.isValid() && z.caster.getStore() == store;
            for (Ref<EntityStore> target : TargetUtil.getAllEntitiesInSphere(z.center, RADIUS + 3, store)) {
                if (!frostbiteTarget(z, casterHere, target, store)) continue;
                Vector3d p = SpellFx.position(target, store);
                if (p == null || Math.hypot(p.x - z.center.x, p.z - z.center.z) > RADIUS) continue;
                SpellFx.effect(target, FROSTBITE_EFFECT, cb);
                SpellFx.damage(casterHere ? z.caster : target, target, "Ice", FROSTBITE_DAMAGE, null, null, cb);
            }
        }
    }

    /** Vrai si le point est dans une zone actuellement gelee par la Porte de Niflheim (combo avec le Courroux de Muspelheim). */
    static boolean isFrozenAt(String world, Vector3d pos) {
        long now = System.currentTimeMillis();
        for (Zone z : zones) {
            if (now < z.until && z.world.equals(world) && Math.hypot(pos.x - z.center.x, pos.z - z.center.z) <= RADIUS) return true;
        }
        return false;
    }

    /** Lanceur present : ses ennemis. Lanceur parti : seulement les creatures (on ne sait plus qui sont ses allies). */
    private static boolean frostbiteTarget(Zone z, boolean casterHere, Ref<EntityStore> target, Store<EntityStore> store) {
        if (target == null || !target.isValid()) return false;
        if (casterHere) return SpellFx.isEnemy(z.caster, target, store);
        return store.getComponent(target, NPCEntity.getComponentType()) != null;
    }

    private static void dropIcicle(Zone z, Store<EntityStore> store, CommandBuffer<EntityStore> cb, long now) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Vector3d target = null;
        List<Ref<EntityStore>> enemies = SpellFx.enemiesAround(z.caster, z.center, RADIUS, store);
        if (!enemies.isEmpty() && rng.nextDouble() < 0.75) target = SpellFx.position(enemies.get(rng.nextInt(enemies.size())), store);
        if (target == null) {
            double r = RADIUS * Math.sqrt(rng.nextDouble()), a = rng.nextDouble(Math.PI * 2);
            target = SpellFx.ground(SpellFx.world(store), new Vector3d(z.center).add(Math.cos(a) * r, 0, Math.sin(a) * r));
        }
        Ref<EntityStore> ref = spawnFixed(z.caster, ICICLE_PROJECTILE, new Vector3d(target).add(0, ICICLE_HEIGHT, 0), new Vector3d(0, -1, 0), cb);
        if (ref != null) icicles.add(new Icicle(ref, z.caster, now));
    }

    /** Stalactites en chute : impact au sol ou sur un ennemi -> degats de glace en zone + eclats. */
    static void tickIcicles(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (icicles.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (Icicle ic : icicles) {
            if (ic.caster.isValid() && ic.caster.getStore() != store) continue;
            Vector3d pos = ic.ref.isValid() ? SpellFx.position(ic.ref, store) : null;
            if (pos == null && !ic.spawned) {
                if (now - ic.spawnedAt > 1000) icicles.remove(ic);
                continue;
            }
            ic.spawned = true;
            boolean landed = pos == null
                    || (ic.lastPos != null && now - ic.spawnedAt > 200 && pos.distanceSquared(ic.lastPos) < 0.0025)
                    || now - ic.spawnedAt > 4000
                    || (ic.caster.isValid() && !SpellFx.enemiesAround(ic.caster, pos, 1.2, store).isEmpty());
            if (pos != null) ic.lastPos = pos;
            if (!landed) continue;
            icicles.remove(ic);
            Vector3d at = ic.lastPos;
            if (at == null) continue;
            SpellFx.fx(FX_FREEZE, new Vector3d(at).add(0, 0.5, 0), 1.6f, store, cb);
            SpellFx.sound(SFX_FREEZE, at, cb);
            if (ic.caster.isValid()) {
                for (Ref<EntityStore> e : SpellFx.enemiesAround(ic.caster, at, ICICLE_RADIUS, store)) {
                    SpellFx.damage(ic.caster, e, "Ice", ICICLE_DAMAGE, null, "Impact_Light", cb);
                }
            }
            if (ic.ref.isValid()) cb.tryRemoveEntity(ic.ref, RemoveReason.REMOVE);
        }
    }

    /** Degel spectaculaire : la glace eclate en vagues sur toute la zone, la porte vole en eclats. */
    private static void thaw(Zone z, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < 24; i++) {
            double r = RADIUS * Math.sqrt(rng.nextDouble()), a = rng.nextDouble(Math.PI * 2);
            Vector3d at = SpellFx.ground(SpellFx.world(store), new Vector3d(z.center).add(Math.cos(a) * r, 0, Math.sin(a) * r));
            SpellFx.fx(FX_FREEZE, new Vector3d(at).add(0, 0.5, 0), 2.0f, store, cb);
            if (i % 4 == 0) SpellFx.sound(SFX_FREEZE, at, cb);
        }
        SpellFx.fx(FX_SNOW, new Vector3d(z.center).add(0, 5, 0), 4.0f, store, cb);
        SpellFx.fx(FX_RING, new Vector3d(z.center).add(0, 0.1, 0), (float) (RADIUS / 3.0), store, cb);
        if (z.gate != null && z.gate.isValid()) {
            Vector3d g = SpellFx.position(z.gate, store);
            if (g != null) {
                SpellFx.fx(FX_FREEZE, new Vector3d(g).add(0, 3, 0), 5.0f, store, cb);
                SpellFx.sound(SFX_CAST, g, cb);
            }
            cb.tryRemoveEntity(z.gate, RemoveReason.REMOVE);
        }
        if (z.portal != null && z.portal.isValid()) cb.tryRemoveEntity(z.portal, RemoveReason.REMOVE);
    }

    private static Ref<EntityStore> spawnFixed(Ref<EntityStore> caster, String configId, Vector3d pos, Vector3d dir, CommandBuffer<EntityStore> cb) {
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(configId);
        if (config == null || caster == null || !caster.isValid()) {
            if (config == null) EldaniorLogger.error("PorteNiflheim: projectile introuvable " + configId);
            return null;
        }
        try {
            return ProjectileModule.get().spawnProjectile(caster, cb, config, new Vector3d(pos), new Vector3d(dir));
        } catch (Exception e) {
            EldaniorLogger.error("PorteNiflheim: " + configId, e);
            return null;
        }
    }

    // ===================== PRISONS DE GLACE =====================
    // Chaque ennemi gele est herisse de piques de glace (modele porte par une entite projectile immobile)
    // jusqu'a la fin du gel ; elles volent en eclats a la fin.

    static final class Prison {
        final Ref<EntityStore> caster, target;
        final long until;
        Ref<EntityStore> model;
        long lastSpawn;
        volatile boolean shattered;     // frappee (hors glace) : la prison explose au prochain tick
        Prison(Ref<EntityStore> caster, Ref<EntityStore> target, long until) { this.caster = caster; this.target = target; this.until = until; }
    }

    private static final List<Prison> prisons = new CopyOnWriteArrayList<>();

    private static void imprison(Ref<EntityStore> caster, Ref<EntityStore> target, long until, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (Prison p : prisons) if (p.target.equals(target)) prisons.remove(p);
        Prison prison = new Prison(caster, target, until);
        spawnPrison(prison, store, cb);
        prisons.add(prison);
    }

    private static void spawnPrison(Prison p, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        p.lastSpawn = System.currentTimeMillis();
        Vector3d pos = SpellFx.position(p.target, store);
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(PRISON_PROJECTILE);
        if (pos == null || config == null || !p.caster.isValid()) return;
        try {
            p.model = ProjectileModule.get().spawnProjectile(p.caster, cb, config, pos, new Vector3d(1, 0, 0));
        } catch (Exception e) {
            EldaniorLogger.error("PorteNiflheim: prison de glace", e);
        }
    }

    /**
     * Briser la glace (appele par {@link com.eldanior.system.skills.runes.NiflheimShatterSystem} pour chaque degat) :
     * un ennemi emprisonne subit +50 % de degats (hors glace) et sa prison explosera au prochain tick.
     * Retourne le multiplicateur a appliquer.
     */
    public static float onDamage(Ref<EntityStore> target, String causeId) {
        if (prisons.isEmpty() || "Ice".equalsIgnoreCase(causeId)) return 1f;
        for (Prison p : prisons) {
            if (p.target.equals(target) && !p.shattered) {
                p.shattered = true;
                return SHATTER_MULTIPLIER;
            }
        }
        return 1f;
    }

    private static void shatter(Prison p, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(p.target, store);
        if (p.model != null && p.model.isValid()) cb.tryRemoveEntity(p.model, RemoveReason.REMOVE);
        EffectsManager.removeEffect(p.target, FREEZE_EFFECT, cb);
        SpellFx.unlock(p.target);
        if (pos == null) return;
        SpellFx.fx(FX_FREEZE, new Vector3d(pos).add(0, 1, 0), 3.0f, store, cb);
        SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_FREEZE, pos, cb);
        SpellFx.sound(SFX_CAST, pos, cb);
        if (!p.caster.isValid()) return;
        SpellFx.damage(p.caster, p.target, "Ice", SHATTER_BURST, null, "Impact_Strong", cb);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(p.caster, pos, SHATTER_SPLASH, store)) {
            if (!e.equals(p.target)) SpellFx.damage(p.caster, e, "Ice", SHATTER_BURST / 2, null, null, cb);
        }
    }

    /** Fait fondre les prisons de glace dans un rayon (combo Muspelheim) : piques retirees, gel leve, creatures liberees. */
    static void meltPrisons(Vector3d pos, double radius, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (Prison p : prisons) {
            if (p.target.getStore() != store) continue;
            Vector3d t = p.target.isValid() ? SpellFx.position(p.target, store) : null;
            if (t == null || t.distance(pos) > radius) continue;
            prisons.remove(p);
            if (p.model != null && p.model.isValid()) cb.tryRemoveEntity(p.model, RemoveReason.REMOVE);
            EffectsManager.removeEffect(p.target, FREEZE_EFFECT, cb);
            SpellFx.unlock(p.target);
            SpellFx.fx(FX_FREEZE, new Vector3d(t).add(0, 1, 0), 1.5f, store, cb);
        }
    }

    /** Garde les piques sur leur cible ; les brise a la fin du gel. Appele a chaque tick. */
    static void tickPrisons(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (prisons.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (Prison p : prisons) {
            if (p.target.getStore() != store) continue;
            if (p.shattered && p.target.isValid()) {                // Briser la glace : explosion de la prison
                prisons.remove(p);
                shatter(p, store, cb);
                continue;
            }
            boolean over = now >= p.until || !p.target.isValid();
            if (over) {
                prisons.remove(p);
                if (p.model != null && p.model.isValid()) {
                    Vector3d at = SpellFx.position(p.model, store);
                    if (at != null) {
                        SpellFx.fx(FX_FREEZE, new Vector3d(at).add(0, 1, 0), 1.8f, store, cb);
                        SpellFx.sound(SFX_FREEZE, at, cb);
                    }
                    cb.tryRemoveEntity(p.model, RemoveReason.REMOVE);
                }
                continue;
            }
            if (p.model == null || !p.model.isValid()) {           // retiree par le moteur : on la repose
                if (now - p.lastSpawn > 1000) spawnPrison(p, store, cb);
                continue;
            }
            Vector3d pos = SpellFx.position(p.target, store);
            TransformComponent t = store.getComponent(p.model, TransformComponent.getComponentType());
            if (pos != null && t != null) t.setPosition(pos);
            Velocity v = store.getComponent(p.model, Velocity.getComponentType());
            if (v != null) v.setZero();
        }
    }

    private static boolean contains(String id, String[] keywords) {
        for (String k : keywords) if (id.contains(k)) return true;
        return false;
    }

    public static void handleDisconnect(UUID uuid) {
        // L'onde en cours est abandonnee ; les blocs deja geles restent planifies pour la restauration.
        List<Wave> list = waves.remove(uuid);
        if (list != null && !list.isEmpty()) TerrainRestore.save();
    }
}
