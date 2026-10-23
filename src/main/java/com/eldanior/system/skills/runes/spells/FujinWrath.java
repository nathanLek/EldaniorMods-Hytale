package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Courroux de Fujin — sort divin de vent (Rune_CourrouxFujin) : le lanceur devient l'Oeil du Cyclone.
 *
 * Trois anneaux centres sur le lanceur (ils le suivent) :
 *  - l'oeil : {@link #EYE_RADIUS} blocs de rayon (7 de diametre), calme, sans danger pour le lanceur ;
 *  - le mur : de {@link #EYE_RADIUS} a {@link #WALL_OUTER} blocs, colonne de vent de 100 blocs (modele Fujin_Cyclone : trails
 *    sur des anneaux qui tournent, jupe de poussiere, enclume nuageuse) ;
 *  - la zone : jusqu'a {@link #ZONE_RADIUS} blocs (celle de Niflheim).
 * Le cyclone aspire toute la zone : les ennemis glissent vers le mur puis tournoient dedans jusqu'a la fin, ou ils sont
 * projetes au loin ; le sol, les plantes et les arbres sont arraches et filent en spirale vers le mur avant de monter dans
 * la colonne. Le mur lance au hasard des lames de vent (vrais projectiles Wind Strike agrandis), de preference sur les ennemis.
 * Budget de performance fixe : 1 modele, au plus {@link #DEBRIS_MAX} debris, {@link #BLADES_MAX} lames, {@link #CAUGHT_MAX} happes.
 */
public final class FujinWrath implements RuneSpell {

    // ===== Deroulement =====
    private static final long DURATION_MS = 25_000, DISPERSE_MS = 2_000, RESTORE_AFTER_MS = 180_000;
    // ===== Anneaux =====
    static final double EYE_RADIUS = 3.5, WALL_OUTER = EYE_RADIUS + 5.0, ZONE_RADIUS = SpellFx.DIVINE_ZONE_RADIUS;
    private static final double WALL_MID = (EYE_RADIUS + WALL_OUTER) / 2;
    // ===== Aspiration des entites =====
    private static final long SCAN_EVERY_MS = 150, SPIN_HIT_EVERY_MS = 600;
    private static final int CAUGHT_MAX = 24;
    private static final double PULL_NEAR = 13.0, PULL_FAR = 5.0, PULL_SWIRL = 4.0, EYE_PUSH = 10.0;
    private static final double SPIN_SPEED = 14.0, LIFT_SPEED = 6.0, HOVER_MIN = 4.0, HOVER_MAX = 26.0;
    private static final double FINAL_PUSH = 38.0, FINAL_LIFT = 16.0;
    private static final float SPIN_DAMAGE = 20f, FINAL_DAMAGE = 300f;
    // ===== Aspiration de l'environnement (vrais blocs) =====
    private static final int DEBRIS_MAX = 160, TEAR_PER_PASS = 8, TEAR_TOTAL_MAX = 900;
    private static final long TEAR_EVERY_MS = 200, TREE_EVERY_MS = 900;
    private static final int TREES_MAX = 16, TREE_BLOCKS_MAX = 180, TREE_REACH = 6, TREE_SAMPLES = 12;
    // Arbres entiers arraches : ils volent d'un bloc vers le mur, montent dans la colonne en tournoyant puis sont expulses en haut
    private static final int FLYING_TREES_MAX = 3, TREE_PIECES_MAX = 70;
    private static final double TREE_INBOUND_SPEED = 6.0, TREE_LIFT = 2.5, TREE_CLIMB = 9.0, TREE_TUMBLE = 0.7, TREE_SPIN = 1.6;
    // Mode test : le cyclone est pose au point vise (fixe). false -> il suit le lanceur, qui plane dans l'oeil.
    private static final boolean ANCHOR_ON_CURSOR = true;
    private static final double CAST_RANGE = 40.0, CAST_FALLBACK = 15.0;
    // Le lanceur plane dans l'oeil, a 5 blocs du sol (seulement si le cyclone suit le lanceur)
    private static final double HOVER_HEIGHT = 5.0;
    private static final long HOVER_EVERY_MS = 100;
    private static final double INBOUND_SPEED = 9.0, INBOUND_SPIN = 1.4, DEBRIS_SPIN = 1.3 * Math.PI * 2, DEBRIS_CLIMB = 6.0, DEBRIS_TOP = 80.0, FLING_SPEED = 26.0;
    // ===== Lames de vent (projectiles) =====
    private static final int BLADES_MAX = 10;
    private static final long BLADE_MIN_MS = 300, BLADE_MAX_MS = 850, BLADE_LIFE_MS = 2_200;
    private static final double BLADE_AIM_CHANCE = 0.75, BLADE_HIT = 2.6, BLADE_CUT_RADIUS = 1.4, BLADE_CUT_AHEAD = 2.0;
    private static final int BLADE_CUT_MAX = 60;
    private static final float BLADE_DAMAGE = 220f;
    /** Une entite creee par le code n'existe qu'a la fin du tick : ne pas la considerer comme perdue avant ce delai. */
    private static final long SPAWN_GRACE_MS = 1_000;
    // ===== Ambiance =====
    private static final long DUST_EVERY_MS = 300, WIND_SOUND_EVERY_MS = 1_800, SUCTION_EVERY_MS = 180, GUST_EVERY_MS = 280;
    private static final long LIGHTNING_MIN_MS = 1_200, LIGHTNING_MAX_MS = 3_200;
    // Les nuages au-dessus de la zone font partie du modele Fujin_Cyclone (trails : plafond tournant de 12 a 34 blocs du centre)
    private static final double THUNDER_HEARING = 70.0;
    // Orage : pluie battante sur la zone, eclairs qui frappent le sol autour du cyclone
    private static final long RAIN_EVERY_MS = 1_200, BOLT_MIN_MS = 1_500, BOLT_MAX_MS = 3_500;
    private static final double RAIN_SPACING = 12.0;

    // ===== Assets =====
    private static final String CYCLONE_PROJECTILE = "Projectile_Config_Fujin_Cyclone", BLADE_PROJECTILE = "Projectile_Config_Fujin_Blade";
    private static final String CAUGHT_EFFECT = "Fujin_Happe";
    private static final String FX_GUST = "Fujin_Gust", FX_DUST = "Gaia_Dust_Burst", FX_SHOCKWAVE = "Chaos_Shockwave", FX_LEAVES = "Block_Break_Leaves";
    private static final String FX_SUCTION = "Fujin_Aspiration", FX_FLASH = "Taranis_Sky_Flash";
    private static final String FX_RAIN = "Taranis_Rain", FX_BOLT = "Taranis_Bolt", FX_SPARK = "Taranis_Electric_Burst";
    private static final String SFX_WIND = "SFX_Emit_Wind_Gusts", SFX_SLASH = "SFX_Ability_Wind_Strike_Slash", SFX_BURST = "SFX_Golem_Frost_Slam_Impact";
    private static final String SFX_THUNDER = "SFX_Global_Weather_Thunder", SFX_GRASS = "SFX_Emit_Wind_Grass";

    @Override public String id() { return "FujinWrath"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Cyclone c = new Cyclone(caster, System.currentTimeMillis(), SpellFx.world(store));
        c.center = ANCHOR_ON_CURSOR ? SpellFx.targetPoint(caster, store, CAST_RANGE, CAST_FALLBACK) : SpellFx.ground(c.world, pos);
        c.wall = SpellFx.spawnModel(caster, CYCLONE_PROJECTILE, c.center, new Vector3d(1, 0, 0), cb);
        if (!ANCHOR_ON_CURSOR) SpellFx.protectFromFall(caster, DURATION_MS + DISPERSE_MS + 5_000, store);
        SpellFx.fx(FX_SHOCKWAVE, new Vector3d(c.center).add(0, 0.3, 0), 3.0f, store, cb);
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * i / 4;
            SpellFx.fx(FX_GUST, new Vector3d(c.center).add(Math.cos(a) * WALL_MID, 1.5, Math.sin(a) * WALL_MID), 2.5f, store, cb);
        }
        SpellFx.sound(SFX_WIND, c.center, cb);
        SpellFx.shake(c.center, 50, "Impact_Strong", store);
        return c;
    }

    // ============================================================ Etat ============================================================

    /** Bloc arrache : file d'abord en spirale vers le mur (inbound), puis monte en orbite dans la colonne. */
    private static final class Debris {
        final Ref<EntityStore> ref;
        final long born;
        double angle, radius, height;
        boolean inbound;
        Debris(Ref<EntityStore> ref, long born, double angle, double radius, double height, boolean inbound) {
            this.ref = ref; this.born = born; this.angle = angle; this.radius = radius; this.height = height; this.inbound = inbound;
        }
    }

    private record Fling(Ref<EntityStore> ref, Vector3d from, Vector3d velocity, long at) {}

    /** Arbre arrache d'un bloc : ses blocs gardent leur place autour du pied, qui vole, tournoie et bascule. */
    private static final class FlyingTree {
        final List<Ref<EntityStore>> refs = new ArrayList<>();
        final List<Vector3d> offsets = new ArrayList<>();                     // position de chaque bloc par rapport au pied
        final long born;
        double angle, radius, height, spin, tilt;
        boolean inbound = true;
        FlyingTree(long born) { this.born = born; }
    }

    private static final class Blade {
        final Ref<EntityStore> ref;
        final long at;
        final Vector3d dir;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        int cut;
        boolean felled;
        Blade(Ref<EntityStore> ref, long at, Vector3d dir) { this.ref = ref; this.at = at; this.dir = dir; }
    }

    private static final class Cyclone implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final World world;
        final TerrainRestore.Restore terrain;
        Ref<EntityStore> wall;
        Vector3d center;
        final List<Debris> debris = new ArrayList<>();
        final List<FlyingTree> flyingTrees = new ArrayList<>();
        final List<Fling> flings = new ArrayList<>();
        final List<Blade> blades = new ArrayList<>();
        final Map<Ref<EntityStore>, Double> caught = new HashMap<>();             // happe -> hauteur de vol visee
        final Map<Ref<EntityStore>, Long> lastSpinHit = new HashMap<>();
        final Set<Long> uprooted = new HashSet<>();
        long lastTick, lastScan, lastTear, lastTree, nextBlade, lastDust, lastSound, disperseAt, lastSuction, lastGust, nextLightning, lastHover, lastRain, nextBolt;
        int torn, trees;
        boolean dispersing;

        Cyclone(Ref<EntityStore> caster, long startAt, World world) {
            this.caster = caster; this.startAt = startAt; this.world = world; this.lastTick = startAt; this.nextBlade = startAt + 600; this.nextLightning = startAt + 2_000; this.nextBolt = startAt + 2_500;
            this.terrain = TerrainRestore.open(world, startAt + RESTORE_AFTER_MS);
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            double dt = Math.min(0.2, (now - lastTick) / 1000.0);
            lastTick = now;
            if (!ANCHOR_ON_CURSOR) {                                                            // le cyclone suit le lanceur, qui plane dans l'oeil
                if (now - lastHover >= HOVER_EVERY_MS) {
                    lastHover = now;
                    center = SpellFx.ground(world, pos);
                    if (!dispersing) hover(pos, store, cb);
                } else {
                    center = new Vector3d(pos.x, center.y, pos.z);
                }
            }
            if (dispersing) {                                                                   // le cyclone s'est disperse : debris projetes
                tickFlings(now, store, cb);
                tickBlades(now, store, cb);
                return now - disperseAt >= DISPERSE_MS;
            }
            if (now - startAt >= DURATION_MS) {
                finale(store, cb);
                SpellFx.remove(wall, cb);
                dispersing = true;
                disperseAt = now;
                return false;
            }
            SpellFx.hold(wall, center, null, store);

            if (now - lastScan >= SCAN_EVERY_MS) { lastScan = now; scan(store, cb); }
            spinCaught(now, store, cb);
            if (now - lastTear >= TEAR_EVERY_MS) { lastTear = now; tear(store, cb); }
            if (now - lastTree >= TREE_EVERY_MS) { lastTree = now; uprootTree(store, cb); }
            moveDebris(dt, now, store);
            moveTrees(dt, now, store);
            tickFlings(now, store, cb);
            if (now >= nextBlade) launchBlade(now, store, cb);
            tickBlades(now, store, cb);
            ambiance(now, store, cb);
            return false;
        }

        /** Le lanceur plane a HOVER_HEIGHT blocs au-dessus du sol (poussees verticales ajoutees, il garde le controle horizontal). */
        private void hover(Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double dy = center.y + HOVER_HEIGHT - pos.y;
            var ms = store.getComponent(caster, com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent.getComponentType());
            var st = ms != null ? ms.getMovementStates() : null;
            boolean falling = st != null && st.falling;
            if (dy > 0.5) SpellFx.push(caster, new Vector3d(0, Math.min(7.0, 3.0 + dy), 0), true, cb);   // monte
            else if (dy > -0.8 && falling) SpellFx.push(caster, new Vector3d(0, 1.9, 0), true, cb);     // compense la gravite
        }

        // ----- Entites : aspirees vers le mur, puis happees -----
        private void scan(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, ZONE_RADIUS + 2, store)) {
                if (caught.containsKey(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - center.x, 0, p.z - center.z);
                double r = out.length();
                if (r < 1e-3) out.set(1, 0, 0); else out.div(r);
                Vector3d tangent = new Vector3d(-out.z, 0, out.x);
                if (r < EYE_RADIUS) {                                                           // l'oeil rejette les intrus
                    SpellFx.push(e, new Vector3d(out).mul(EYE_PUSH).add(0, 2, 0), false, cb);
                } else if (r <= WALL_OUTER + 0.5) {                                             // le mur happe
                    if (caught.size() >= CAUGHT_MAX) continue;
                    caught.put(e, rng.nextDouble(HOVER_MIN, HOVER_MAX));
                    SpellFx.effect(e, CAUGHT_EFFECT, cb);
                    SpellFx.fx(FX_GUST, new Vector3d(p).add(0, 1, 0), 1.5f, store, cb);
                } else if (r <= ZONE_RADIUS) {                                                  // la zone aspire, en spirale
                    double k = 1 - (r - WALL_OUTER) / (ZONE_RADIUS - WALL_OUTER);
                    double pull = PULL_FAR + (PULL_NEAR - PULL_FAR) * k;
                    SpellFx.push(e, new Vector3d(out).mul(-pull).add(tangent.mul(PULL_SWIRL)).add(0, 0.8, 0), false, cb);
                }
            }
        }

        /** Les happes tournent dans le mur a la hauteur qui leur est assignee, jusqu'a la fin. */
        private void spinCaught(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            caught.entrySet().removeIf(en -> {
                Ref<EntityStore> e = en.getKey();
                Vector3d p = SpellFx.position(e, store);
                if (p == null) { lastSpinHit.remove(e); return true; }
                Vector3d out = new Vector3d(p.x - center.x, 0, p.z - center.z);
                double r = out.length();
                if (r < 1e-3) out.set(1, 0, 0); else out.div(r);
                Vector3d tangent = new Vector3d(-out.z, 0, out.x);
                double dy = en.getValue() - (p.y - center.y);
                SpellFx.push(e, tangent.mul(SPIN_SPEED).add(new Vector3d(out).mul((WALL_MID - r) * 3)).add(0, Math.max(-LIFT_SPEED, Math.min(LIFT_SPEED, dy * 2)), 0), false, cb);
                Long hit = lastSpinHit.get(e);
                if (hit == null || now - hit >= SPIN_HIT_EVERY_MS) {
                    lastSpinHit.put(e, now);
                    SpellFx.damage(caster, e, "Wind", SPIN_DAMAGE, null, null, cb);
                }
                return false;
            });
        }

        // ----- Lames de vent : vrais projectiles lances au hasard depuis le mur -----
        private void launchBlade(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            nextBlade = now + rng.nextLong(BLADE_MIN_MS, BLADE_MAX_MS);
            if (blades.size() >= BLADES_MAX) return;
            Vector3d aim = null;
            if (rng.nextDouble() < BLADE_AIM_CHANCE) {
                List<Ref<EntityStore>> outside = new ArrayList<>();
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, ZONE_RADIUS, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p != null && !caught.containsKey(e) && SpellFx.horizontalDistance(p, center) > WALL_OUTER + 1) outside.add(e);
                }
                if (!outside.isEmpty()) aim = SpellFx.position(outside.get(rng.nextInt(outside.size())), store);
            }
            double a = aim != null ? Math.atan2(aim.z - center.z, aim.x - center.x) + rng.nextDouble(-0.2, 0.2) : rng.nextDouble(Math.PI * 2);
            Vector3d from = new Vector3d(center.x + Math.cos(a) * WALL_OUTER, center.y + rng.nextDouble(2, 16), center.z + Math.sin(a) * WALL_OUTER);
            Vector3d dir = aim != null ? new Vector3d(aim).add(0, 1, 0).sub(from).normalize()
                    : new Vector3d(Math.cos(a), rng.nextDouble(-0.35, 0.05), Math.sin(a)).normalize();
            Ref<EntityStore> ref = SpellFx.spawnModel(caster, BLADE_PROJECTILE, from, dir, cb);
            if (ref == null) return;
            blades.add(new Blade(ref, now, dir));
            SpellFx.fx(FX_GUST, from, 2.0f, store, cb);
            SpellFx.sound(SFX_SLASH, from, cb);
        }

        private void tickBlades(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<int[]> cells = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            blades.removeIf(b -> {
                long age = now - b.at;
                if (age >= BLADE_LIFE_MS) { SpellFx.remove(b.ref, cb); return true; }
                Vector3d p = SpellFx.position(b.ref, store);
                if (p == null) return age >= SPAWN_GRACE_MS;                                    // pas encore cree, ou detruit
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, BLADE_HIT, store)) {
                    if (!b.hit.add(e)) continue;
                    Vector3d ep = SpellFx.position(e, store);
                    Vector3d push = ep == null ? new Vector3d(0, 5, 0) : new Vector3d(ep.x - center.x, 0, ep.z - center.z).normalize().mul(12).add(0, 5, 0);
                    SpellFx.damage(caster, e, "Wind", BLADE_DAMAGE, push, "Impact_Strong", cb);
                    SpellFx.fx(FX_GUST, new Vector3d(p), 1.8f, store, cb);
                }
                cut(b, p, cells, ids, store, cb);
                return false;
            });
            removeBlocks(cells, ids);
        }

        /** La lame tranche ce qu'elle traverse (un peu en avant d'elle, pour ne pas s'y briser) ; un arbre touche est deracine. */
        private void cut(Blade b, Vector3d p, List<int[]> cells, List<String> ids, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (b.cut >= BLADE_CUT_MAX) return;
            Vector3d c = new Vector3d(p).add(new Vector3d(b.dir).mul(BLADE_CUT_AHEAD));
            int r = (int) Math.ceil(BLADE_CUT_RADIUS);
            for (int dx = -r; dx <= r && b.cut < BLADE_CUT_MAX; dx++) {
                for (int dy = -r; dy <= r && b.cut < BLADE_CUT_MAX; dy++) {
                    for (int dz = -r; dz <= r && b.cut < BLADE_CUT_MAX; dz++) {
                        if (dx * dx + dy * dy + dz * dz > BLADE_CUT_RADIUS * BLADE_CUT_RADIUS) continue;
                        int x = (int) Math.floor(c.x) + dx, y = (int) Math.floor(c.y) + dy, z = (int) Math.floor(c.z) + dz;
                        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
                        if (type == null || TerrainRestore.isEmpty(world, x, y, z)) continue;
                        String id = type.getId();
                        if (TerrainRestore.isProtectedArea(world.getName(), x, y, z) || TerrainRestore.isKept(world, id, x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) continue;
                        if (id.startsWith("Wood_")) {
                            long key = ((long) x << 32) ^ (z & 0xffffffffL);
                            if (!b.felled && trees < TREES_MAX && flyingTrees.size() < FLYING_TREES_MAX && uprooted.add(key)) { b.felled = true; trees++; fellTree(x, y, z, store, cb); }
                            continue;
                        }
                        cells.add(new int[]{x, y, z});
                        ids.add(id);
                        b.cut++;
                        if (b.cut % 3 == 0 && debris.size() < DEBRIS_MAX) addDebris(id, x, y, z, store, cb);      // une partie part dans le cyclone
                    }
                }
            }
        }

        // ----- Environnement : sol et plantes arraches dans toute la zone -----
        private void tear(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (debris.size() >= DEBRIS_MAX || torn >= TEAR_TOTAL_MAX) return;
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            int baseY = (int) Math.floor(center.y);
            List<int[]> cells = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            for (int k = 0; k < TEAR_PER_PASS && debris.size() < DEBRIS_MAX && torn < TEAR_TOTAL_MAX; k++) {
                double a = rng.nextDouble(Math.PI * 2), r = EYE_RADIUS + 0.5 + Math.sqrt(rng.nextDouble()) * (ZONE_RADIUS - EYE_RADIUS - 0.5);
                int x = (int) Math.floor(center.x + Math.cos(a) * r), z = (int) Math.floor(center.z + Math.sin(a) * r);
                if (TerrainRestore.isProtectedArea(world.getName(), x, baseY, z)) continue;
                int y = top(x, baseY, z);
                if (y == Integer.MIN_VALUE) continue;
                BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
                if (type == null) continue;
                String id = type.getId();
                if (id.startsWith("Wood_") || TerrainRestore.isKept(world, id, x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) continue;
                if (!addDebris(id, x, y, z, store, cb)) continue;
                cells.add(new int[]{x, y, z});
                ids.add(id);
                torn++;
            }
            removeBlocks(cells, ids);
        }

        /** Un arbre de la zone est deracine : ses blocs disparaissent, une partie file vers le cyclone. */
        private void uprootTree(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (trees >= TREES_MAX || flyingTrees.size() >= FLYING_TREES_MAX) return;
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            int baseY = (int) Math.floor(center.y);
            for (int s = 0; s < TREE_SAMPLES; s++) {
                double a = rng.nextDouble(Math.PI * 2), r = rng.nextDouble(WALL_OUTER, ZONE_RADIUS);
                int x = (int) Math.floor(center.x + Math.cos(a) * r), z = (int) Math.floor(center.z + Math.sin(a) * r);
                long key = ((long) x << 32) ^ (z & 0xffffffffL);
                if (uprooted.contains(key) || TerrainRestore.isProtectedArea(world.getName(), x, baseY, z)) continue;
                int trunkY = Integer.MIN_VALUE;
                for (int y = baseY + 8; y >= baseY - 4 && trunkY == Integer.MIN_VALUE; y--) {
                    BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                    if (t == null) break;
                    if (t.getId().startsWith("Wood_")) trunkY = y;
                }
                if (trunkY == Integer.MIN_VALUE) continue;
                uprooted.add(key);
                fellTree(x, trunkY, z, store, cb);
                trees++;
                return;
            }
        }

        private void fellTree(int x0, int y0, int z0, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<int[]> cells = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            Set<Long> seen = new HashSet<>();
            ArrayDeque<int[]> queue = new ArrayDeque<>();
            int baseY = y0;
            for (int y = y0 - 1; y >= y0 - 4; y--) {                                            // pied du tronc
                BlockType t = WorldBlockUtil.getBlockType(world, x0, y, z0);
                if (t == null || !t.getId().startsWith("Wood_")) break;
                baseY = y;
            }
            queue.add(new int[]{x0, baseY, z0});
            while (!queue.isEmpty() && cells.size() < TREE_BLOCKS_MAX) {                        // tronc + feuillage relies, borne
                int[] c = queue.poll();
                long k = (((long) c[0] & 0x1FFFFF) << 42) | (((long) c[1] & 0x1FFFFF) << 21) | ((long) c[2] & 0x1FFFFF);
                if (!seen.add(k) || Math.abs(c[0] - x0) > TREE_REACH || Math.abs(c[2] - z0) > TREE_REACH || c[1] < baseY) continue;
                BlockType type = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                if (type == null) continue;
                String id = type.getId();
                if (!(id.startsWith("Wood_") || id.contains("Leaves")) || TerrainRestore.isProtectedArea(world.getName(), c[0], c[1], c[2])) continue;
                cells.add(c);
                ids.add(id);
                for (int[] o : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) queue.add(new int[]{c[0] + o[0], c[1] + o[1], c[2] + o[2]});
            }
            if (cells.size() < 3) return;
            Vector3d pivot = new Vector3d(x0 + 0.5, baseY, z0 + 0.5);
            FlyingTree tree = new FlyingTree(lastTick);
            int leaves = 0;
            for (int i = 0; i < cells.size(); i++) if (!ids.get(i).startsWith("Wood_")) leaves++;
            int woods = cells.size() - leaves, leafBudget = Math.max(0, TREE_PIECES_MAX - woods), leafStride = Math.max(1, (int) Math.ceil(leaves / (double) Math.max(1, leafBudget)));
            int leafIndex = 0;
            for (int i = 0; i < cells.size() && tree.refs.size() < TREE_PIECES_MAX; i++) {     // tout le bois, et assez de feuilles pour la silhouette
                boolean wood = ids.get(i).startsWith("Wood_");
                if (!wood && (leafIndex++ % leafStride) != 0) continue;
                int[] c = cells.get(i);
                Vector3d at = new Vector3d(c[0] + 0.5, c[1] + 0.5, c[2] + 0.5);
                Ref<EntityStore> ref = SpellFx.spawnBlock(ids.get(i), at, store, cb);
                if (ref == null) continue;
                tree.refs.add(ref);
                tree.offsets.add(new Vector3d(at).sub(pivot));
            }
            double dx = pivot.x - center.x, dz = pivot.z - center.z;
            tree.angle = Math.atan2(dz, dx);
            tree.radius = Math.hypot(dx, dz);
            tree.height = pivot.y - center.y;
            flyingTrees.add(tree);
            removeBlocks(cells, ids);
            SpellFx.fx(FX_LEAVES, new Vector3d(pivot).add(0, 3, 0), 3.5f, store, cb);
            SpellFx.fx(FX_DUST, new Vector3d(pivot).add(0, 0.5, 0), 2.0f, store, cb);
            SpellFx.sound(SFX_SLASH, pivot, cb);
        }

        /** Arbres volants : spirale vers le mur en se soulevant et en basculant, puis montee dans la colonne, puis expulsion. */
        private void moveTrees(double dt, long now, Store<EntityStore> store) {
            flyingTrees.removeIf(t -> {
                if (t.inbound) {
                    t.radius = Math.max(WALL_MID, t.radius - TREE_INBOUND_SPEED * (1 + 1.2 * (1 - t.radius / ZONE_RADIUS)) * dt);
                    t.angle += INBOUND_SPIN * dt * (6 / Math.max(6, t.radius) + 0.25);
                    t.height += TREE_LIFT * dt;
                    t.tilt = Math.min(Math.PI / 2, t.tilt + TREE_TUMBLE * dt);
                    if (t.radius <= WALL_OUTER) t.inbound = false;
                } else {
                    t.angle += DEBRIS_SPIN * dt * 0.55;
                    t.height += TREE_CLIMB * dt;
                    t.radius += (WALL_MID + Math.max(0, t.height - 40) * 0.08 - t.radius) * Math.min(1, dt * 2);
                }
                t.spin += TREE_SPIN * dt;
                Vector3d pivot = new Vector3d(center.x + Math.cos(t.angle) * t.radius, center.y + t.height, center.z + Math.sin(t.angle) * t.radius);
                boolean expel = t.height >= DEBRIS_TOP;
                double ct = Math.cos(t.tilt), st = Math.sin(t.tilt), cs = Math.cos(t.spin), ss = Math.sin(t.spin);
                for (int i = 0; i < t.refs.size(); i++) {
                    Ref<EntityStore> ref = t.refs.get(i);
                    Vector3d o = t.offsets.get(i);
                    double y1 = o.y * ct - o.z * st, z1 = o.y * st + o.z * ct;                    // bascule
                    double x2 = o.x * cs + z1 * ss, z2 = -o.x * ss + z1 * cs;                    // rotation sur lui-meme
                    Vector3d at = new Vector3d(pivot).add(x2, y1, z2);
                    if (expel) {
                        ThreadLocalRandom rng = ThreadLocalRandom.current();
                        flings.add(new Fling(ref, at, new Vector3d(Math.cos(t.angle) + rng.nextDouble(-0.3, 0.3), 0.35, Math.sin(t.angle) + rng.nextDouble(-0.3, 0.3)).mul(FLING_SPEED), now));
                    } else if (ref.isValid()) {
                        SpellFx.hold(ref, at, null, store);
                    }
                }
                return expel;
            });
        }

        private boolean addDebris(String id, int x, int y, int z, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> ref = SpellFx.spawnBlock(id, new Vector3d(x + 0.5, y + 0.5, z + 0.5), store, cb);
            if (ref == null) return false;
            double dx = x + 0.5 - center.x, dz = z + 0.5 - center.z, r = Math.hypot(dx, dz);
            debris.add(new Debris(ref, lastTick, Math.atan2(dz, dx), r, y + 0.5 - center.y, r > WALL_OUTER));
            return true;
        }

        private void removeBlocks(List<int[]> cells, List<String> ids) {
            if (cells.isEmpty()) return;
            world.execute(() -> {
                try {
                    for (int i = 0; i < cells.size(); i++) {
                        int[] c = cells.get(i);
                        BlockType type = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                        if (type != null && ids.get(i).equals(type.getId())) TerrainRestore.place(world, terrain, c[0], c[1], c[2], ids.get(i), TerrainRestore.EMPTY, 0);
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("FujinWrath: terrain", e);
                }
            });
        }

        /** Premier bloc non vide en descendant (8 au-dessus a 6 en dessous), ou MIN_VALUE. */
        private int top(int x, int baseY, int z) {
            for (int y = baseY + 8; y >= baseY - 6; y--) {
                if (WorldBlockUtil.getBlockType(world, x, y, z) == null) return Integer.MIN_VALUE;
                if (!TerrainRestore.isEmpty(world, x, y, z)) return y;
            }
            return Integer.MIN_VALUE;
        }

        private void moveDebris(double dt, long now, Store<EntityStore> store) {
            debris.removeIf(d -> {
                if (!d.ref.isValid()) return now - d.born >= SPAWN_GRACE_MS;                     // pas encore cree (fin de tick)
                if (d.inbound) {                                                                // spirale vers le mur, de plus en plus vite
                    double speed = INBOUND_SPEED * (1 + 1.5 * (1 - d.radius / ZONE_RADIUS));
                    d.radius = Math.max(WALL_MID, d.radius - speed * dt);
                    d.angle += INBOUND_SPIN * dt * (6 / Math.max(6, d.radius) + 0.3);
                    d.height += (2.0 + Math.sin(d.angle * 3)) * dt;
                    if (d.radius <= WALL_OUTER) d.inbound = false;
                } else {
                    d.angle += DEBRIS_SPIN * dt * (0.8 + 2.5 / (d.radius + 1));
                    d.height += DEBRIS_CLIMB * dt;
                    d.radius += (WALL_MID + Math.sin(d.angle * 0.7) * 1.8 + Math.max(0, d.height - 40) * 0.08 - d.radius) * Math.min(1, dt * 2);
                }
                Vector3d at = new Vector3d(center.x + Math.cos(d.angle) * d.radius, center.y + d.height, center.z + Math.sin(d.angle) * d.radius);
                if (d.height >= DEBRIS_TOP) {                                                   // recrache au sommet
                    flings.add(new Fling(d.ref, at, new Vector3d(Math.cos(d.angle), 0.25, Math.sin(d.angle)).mul(FLING_SPEED), now));
                    return true;
                }
                SpellFx.hold(d.ref, at, null, store);
                return false;
            });
        }

        private void tickFlings(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            flings.removeIf(f -> {
                double t = (now - f.at()) / 1000.0;
                if (t >= 2.0) { SpellFx.remove(f.ref(), cb); return true; }
                if (!f.ref().isValid()) return t >= SPAWN_GRACE_MS / 1000.0;
                SpellFx.hold(f.ref(), new Vector3d(f.from()).add(new Vector3d(f.velocity()).mul(t)).add(0, -9.0 * t * t, 0), null, store);
                return false;
            });
        }

        private void ambiance(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            if (now - lastDust >= DUST_EVERY_MS) {                                              // terre soulevee au pied du mur
                lastDust = now;
                for (int i = 0; i < 3; i++) {
                    double a = rng.nextDouble(Math.PI * 2), r = rng.nextDouble(WALL_OUTER - 1, WALL_OUTER + 3);
                    SpellFx.fx(FX_DUST, new Vector3d(center).add(Math.cos(a) * r, 0.5, Math.sin(a) * r), 2.4f, store, cb);
                }
                if (rng.nextInt(3) == 0) {
                    double a = rng.nextDouble(Math.PI * 2);
                    SpellFx.fx(FX_LEAVES, new Vector3d(center).add(Math.cos(a) * WALL_MID, rng.nextDouble(3, 25), Math.sin(a) * WALL_MID), 3.0f, store, cb);
                }
            }
            if (now - lastSuction >= SUCTION_EVERY_MS) {                                        // l'air de toute la zone file vers le cyclone
                lastSuction = now;
                for (int i = 0; i < 3; i++) {
                    double a = rng.nextDouble(Math.PI * 2), r = rng.nextDouble(WALL_OUTER + 4, ZONE_RADIUS);
                    Vector3d at = new Vector3d(center).add(Math.cos(a) * r, 0, Math.sin(a) * r);
                    at = SpellFx.ground(world, at);
                    float yaw = (float) Math.toDegrees(Math.atan2(-Math.cos(a), -Math.sin(a)));
                    SpellFx.fx(FX_SUCTION, at, yaw, 0f, 1.0f, store, cb);
                }
            }
            if (now - lastGust >= GUST_EVERY_MS) {                                              // rafales qui eclatent dans le mur
                lastGust = now;
                double a = rng.nextDouble(Math.PI * 2), h = rng.nextDouble(2, 70);
                double r = WALL_MID + Math.max(0, h - 40) * 0.08;
                SpellFx.fx(FX_GUST, new Vector3d(center).add(Math.cos(a) * r, h, Math.sin(a) * r), 3.5f, store, cb);
            }
            if (now - lastRain >= RAIN_EVERY_MS) {                                              // pluie battante sur la zone (hors oeil)
                lastRain = now;
                int n = (int) Math.ceil(ZONE_RADIUS / RAIN_SPACING);
                for (int i = -n; i <= n; i++) for (int j = -n; j <= n; j++) {
                    double dx = i * RAIN_SPACING, dz = j * RAIN_SPACING, d = Math.hypot(dx, dz);
                    if (d <= ZONE_RADIUS && d > WALL_OUTER) SpellFx.fx(FX_RAIN, new Vector3d(center).add(dx, 0, dz), 1.0f, store, cb);
                }
            }
            if (now >= nextBolt) {                                                              // un eclair frappe le sol autour du cyclone
                nextBolt = now + rng.nextLong(BOLT_MIN_MS, BOLT_MAX_MS);
                double a = rng.nextDouble(Math.PI * 2), r = rng.nextDouble(WALL_OUTER + 4, ZONE_RADIUS);
                Vector3d at = SpellFx.ground(world, new Vector3d(center).add(Math.cos(a) * r, 0, Math.sin(a) * r));
                SpellFx.fx(FX_BOLT, at, 2.0f, store, cb);
                SpellFx.fx(FX_SPARK, new Vector3d(at).add(0, 1, 0), 2.0f, store, cb);
                SpellFx.fx(FX_DUST, new Vector3d(at).add(0, 0.5, 0), 1.6f, store, cb);
                SpellFx.sound2dNear(SFX_THUNDER, at, THUNDER_HEARING, store);
            }
            if (now >= nextLightning) {                                                         // eclairs dans l'enclume orageuse
                nextLightning = now + rng.nextLong(LIGHTNING_MIN_MS, LIGHTNING_MAX_MS);
                double a = rng.nextDouble(Math.PI * 2), r = rng.nextDouble(8, 18);
                SpellFx.fx(FX_FLASH, new Vector3d(center).add(Math.cos(a) * r, rng.nextDouble(96, 106), Math.sin(a) * r), 2.5f, store, cb);
                SpellFx.sound2dNear(SFX_THUNDER, center, THUNDER_HEARING, store);
            }
            if (now - lastSound >= WIND_SOUND_EVERY_MS) {
                lastSound = now;
                SpellFx.sound(SFX_WIND, center, cb);
                SpellFx.sound(SFX_GRASS, center, cb);
            }
        }

        /** Fin : le cyclone se disperse ; les happes et les debris sont projetes au loin. */
        private void finale(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            long now = System.currentTimeMillis();
            for (Ref<EntityStore> e : caught.keySet()) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - center.x, 0, p.z - center.z);
                if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                SpellFx.damage(caster, e, "Wind", FINAL_DAMAGE, out.normalize().mul(FINAL_PUSH).add(0, FINAL_LIFT, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_GUST, new Vector3d(p).add(0, 1, 0), 2.5f, store, cb);
            }
            caught.clear();
            for (Debris d : debris) {
                Vector3d at = new Vector3d(center.x + Math.cos(d.angle) * d.radius, center.y + d.height, center.z + Math.sin(d.angle) * d.radius);
                flings.add(new Fling(d.ref, at, new Vector3d(Math.cos(d.angle), 0.4, Math.sin(d.angle)).mul(FLING_SPEED * 1.3), now));
            }
            debris.clear();
            for (FlyingTree t : flyingTrees) {
                Vector3d pivot = new Vector3d(center.x + Math.cos(t.angle) * t.radius, center.y + t.height, center.z + Math.sin(t.angle) * t.radius);
                for (int i = 0; i < t.refs.size(); i++)
                    flings.add(new Fling(t.refs.get(i), new Vector3d(pivot).add(t.offsets.get(i)), new Vector3d(Math.cos(t.angle), 0.4, Math.sin(t.angle)).mul(FLING_SPEED), now));
            }
            flyingTrees.clear();
            SpellFx.fx(FX_SHOCKWAVE, new Vector3d(center).add(0, 0.3, 0), 6.0f, store, cb);
            for (int h = 4; h <= 60; h += 14) SpellFx.fx(FX_GUST, new Vector3d(center).add(0, h, 0), 6.0f, store, cb);
            SpellFx.sound(SFX_BURST, center, cb);
            SpellFx.sound(SFX_SLASH, center, cb);
            SpellFx.shake(center, 60, "Impact_Strong", store);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(wall, cb);
            for (Debris d : debris) SpellFx.remove(d.ref, cb);
            for (FlyingTree t : flyingTrees) for (Ref<EntityStore> r : t.refs) SpellFx.remove(r, cb);
            flyingTrees.clear();
            for (Fling f : flings) SpellFx.remove(f.ref(), cb);           // debris projetes a la fin : retires (pas de survie hors du sort)
            for (Blade b : blades) SpellFx.remove(b.ref, cb);
            debris.clear();
            flings.clear();
            blades.clear();
            TerrainRestore.save();
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(wall);
            for (Debris d : debris) RuneSpellRuntime.removeLater(d.ref);
            for (FlyingTree t : flyingTrees) for (Ref<EntityStore> r : t.refs) RuneSpellRuntime.removeLater(r);
            for (Fling f : flings) RuneSpellRuntime.removeLater(f.ref());
            for (Blade b : blades) RuneSpellRuntime.removeLater(b.ref);
            TerrainRestore.save();
        }
    }
}
