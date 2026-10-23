package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune du Puits Gravitationnel (Spatial, Unique) : zone de gravite extreme au point vise.
 *
 * Le centre est sous les pieds de l'ennemi vise (sinon le bloc vise). Le sol s'effondre en un cratere de 7 blocs de
 * diametre et 2 de profondeur (pente en demi-dalles, fond de basalte compresse, bourrelet et fissures autour), les plantes
 * de la zone sont ecrasees, et une colonne de traits noir-violet tombe sur un cylindre de {@link #ZONE_RADIUS} blocs de
 * rayon et 50 de haut. Toute cible dans la zone est clouee au sol jusqu'a la fin ({@link #DURATION_MS}) et subit
 * {@link #DAMAGE_PER_SECOND} degats par seconde.
 * Fiche de conception : scripts/runes/specs/Rune_PuitsGravitationnel.json
 */
public final class PuitsGravitationnel implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 40.0, FALLBACK = 15.0;
    private static final double ZONE_RADIUS = 3.5, COLUMN_HEIGHT = 50.0;            // 7 blocs de diametre : la zone = le cratere
    private static final long DURATION_MS = 10_000;
    private static final long TICK_DAMAGE_EVERY_MS = 1_000, PIN_DELAY_MS = 350, SCAN_EVERY_MS = 200;
    private static final float DAMAGE_PER_SECOND = 2f;
    private static final double SLAM_SPEED = 22.0;                 // chute imposee avant d'etre cloue
    // ===== Cratere =====
    private static final double CRATER_RADIUS = 3.5, CRATER_DEPTH = 2.0, RIM_INNER = 4.4, RIM_OUTER = 5.3;
    private static final int CRACKS = 6, CRACK_MIN = 4, CRACK_MAX = 9;
    private static final double CRACK_DEPTH_START = 2.0, CRACK_DEPTH_END = 0.5, BRANCH_CHANCE = 0.12;
    private static final long RESTORE_AFTER_MS = 120_000;
    // ===== Ambiance =====
    private static final long COLUMN_EVERY_MS = 500, HUM_EVERY_MS = 2_000, FISSURE_EVERY_MS = 350;
    private static final int FISSURE_FX_PER_PASS = 10;

    // ===== Assets =====
    private static final String COLUMN_PROJECTILE = "Projectile_Config_Gravite_Colonne";
    private static final String FX_FISSURE = "Gravite_Fissure", FX_COLUMN = "Gravite_Colonne";
    private static final String CRUSH_EFFECT = "Gravite_Ecrasement";
    private static final String SFX_IMPACT = "SFX_Golem_Frost_Slam_Impact", SFX_HUM = "SFX_Emit_Dungeon_Goblin_Void_Strike", SFX_END = "SFX_Void_Eviction";
    private static final String FLOOR = "Rock_Basalt", FLOOR_HALF = "Rock_Basalt_Half", SLOPE = "Rock_Basalt_Cobble", SLOPE_HALF = "Rock_Basalt_Cobble_Half";
    private static final String RIM_HALF = "Rock_Basalt_Cobble_Half", RUBBLE = "Rock_Stone_Cobble";

    @Override public String id() { return "PuitsGravitationnel"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        World world = SpellFx.world(store);
        Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);               // sous les pieds de l'ennemi vise
        Vector3d tp = target != null ? SpellFx.position(target, store) : null;
        Vector3d at = tp != null ? SpellFx.ground(world, tp) : SpellFx.targetPoint(caster, store, RANGE, FALLBACK);
        Zone z = new Zone(caster, at, System.currentTimeMillis(), world);
        z.cracks.addAll(planCracks(at));
        crater(world, z.terrain, at, z.cracks);                                                 // la gravite est invisible : seul le sol la trahit
        z.column = SpellFx.spawnModel(caster, COLUMN_PROJECTILE, at, new Vector3d(1, 0, 0), cb);
        SpellFx.fx(FX_COLUMN, at, 1.0f, store, cb);
        SpellFx.sound(SFX_IMPACT, at, cb);
        SpellFx.shake(at, 20, "Impact_Light", store);
        return z;
    }

    /** Trace des fissures autour d'un cratere centre en at (cases {x, z, profondeur}). */
    static List<double[]> planCracks(Vector3d at) {
        java.util.Random rng = new java.util.Random(ThreadLocalRandom.current().nextLong());
        List<double[]> out = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (int k = 0; k < CRACKS; k++)
            GraviteSol.crackPath(at, Math.PI * 2 * k / CRACKS + rng.nextDouble() * 0.7, RIM_INNER - 0.6, CRACK_MIN + rng.nextDouble() * (CRACK_MAX - CRACK_MIN),
                    CRACK_DEPTH_START, CRACK_DEPTH_END, BRANCH_CHANCE, out, seen, rng, true);
        return out;
    }

    // ============================================================ Terrain ============================================================

    /**
     * Cratere en cuvette : profondeur 2*(1-(d/R)^2), quantifiee au demi-bloc (demi-dalle quand il tombe entre deux blocs),
     * fond de basalte au centre et cailloutis sur la pente ; bourrelet en demi-dalles et gravats autour ; fissures rayonnantes ;
     * plantes de la zone ecrasees. Jamais en zone protegee, sous l'eau, ni sur les blocs conserves.
     */
    static void crater(World world, TerrainRestore.Restore batch, Vector3d at, List<double[]> cracks) {
        int cx = (int) Math.floor(at.x), cz = (int) Math.floor(at.z), baseY = (int) Math.floor(at.y) - 1;
        long seed = ThreadLocalRandom.current().nextLong();
        world.execute(() -> {
            try {
                java.util.Random rng = new java.util.Random(seed);
                int r = (int) Math.ceil(RIM_OUTER);
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        double d = Math.hypot(cx + dx + 0.5 - at.x, cz + dz + 0.5 - at.z);
                        if (d > RIM_OUTER) continue;
                        column(world, batch, cx + dx, baseY, cz + dz, d, rng);
                    }
                }
                for (double[] c : cracks) {                                                     // fissures creusees
                    int x = (int) c[0], z = (int) c[1];
                    int top = GraviteSol.surface(world, x, baseY, z);
                    if (top == Integer.MIN_VALUE || TerrainRestore.isProtectedArea(world.getName(), x, top, z) || WorldBlockUtil.getFluidIndex(world, x, top + 1, z) > 0) continue;
                    GraviteSol.dig(world, batch, x, top, z, c[2], FLOOR, FLOOR_HALF);
                }
                int zr = (int) Math.ceil(RIM_OUTER);                                           // plantes ecrasees par la gravite
                for (int dx = -zr; dx <= zr; dx++) {
                    for (int dz = -zr; dz <= zr; dz++) {
                        if (dx * dx + dz * dz > RIM_OUTER * RIM_OUTER) continue;
                        int x = cx + dx, z = cz + dz;
                        for (int y = baseY + 4; y >= baseY - 3; y--) {
                            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                            if (t == null || TerrainRestore.isEmpty(world, x, y, z)) continue;
                            if (t.getId().startsWith("Plant_") && !TerrainRestore.isProtectedArea(world.getName(), x, y, z) && !TerrainRestore.isKept(world, t.getId(), x, y, z))
                                TerrainRestore.place(world, batch, x, y, z, t.getId(), TerrainRestore.EMPTY, 0);
                        }
                    }
                }
            } catch (Exception e) {
                EldaniorLogger.error("PuitsGravitationnel: cratere", e);
            }
        });
    }

    /** Une colonne du cratere (d = distance au centre) : tout est creuse dans le sol, rien ne depasse. */
    private static void column(World world, TerrainRestore.Restore batch, int x, int baseY, int z, double d, java.util.Random rng) {
        if (TerrainRestore.isProtectedArea(world.getName(), x, baseY, z)) return;
        int top = GraviteSol.surface(world, x, baseY, z);
        if (top == Integer.MIN_VALUE || WorldBlockUtil.getFluidIndex(world, x, top + 1, z) > 0) return;
        if (d > CRATER_RADIUS) {                                                                 // levre affaissee d'un demi-bloc, puis gravats a fleur
            if (d <= RIM_INNER) GraviteSol.dig(world, batch, x, top, z, 0.5, SLOPE, RIM_HALF);
            else if (rng.nextDouble() < 0.35 && GraviteSol.editable(world, x, top, z))
                TerrainRestore.place(world, batch, x, top, z, WorldBlockUtil.getBlockType(world, x, top, z).getId(), RUBBLE, 0);
            return;
        }
        double depth = Math.round(CRATER_DEPTH * (1 - (d / CRATER_RADIUS) * (d / CRATER_RADIUS)) * 2) / 2.0;
        boolean center = d < CRATER_RADIUS * 0.55;
        GraviteSol.dig(world, batch, x, top, z, Math.max(0.5, depth), center ? FLOOR : SLOPE, center ? FLOOR_HALF : SLOPE_HALF);
    }

    // ============================================================ Zone ============================================================

    private static final class Zone implements Instance {
        final Ref<EntityStore> caster;
        final Vector3d at;
        final long startAt;
        final TerrainRestore.Restore terrain;
        Ref<EntityStore> column;
        final List<double[]> cracks = new ArrayList<>();                 // cases des fissures {x, z, profondeur}
        final Map<Ref<EntityStore>, Long> caughtAt = new HashMap<>();   // capture (chute imposee) -> cloue apres PIN_DELAY_MS
        final Set<Ref<EntityStore>> pinned = new HashSet<>();
        long lastScan, lastDamage, lastColumn, lastHum, lastFissure;

        Zone(Ref<EntityStore> caster, Vector3d at, long startAt, World world) {
            this.caster = caster; this.at = at; this.startAt = startAt; this.lastDamage = startAt; this.lastColumn = startAt;
            this.terrain = TerrainRestore.open(world, startAt + RESTORE_AFTER_MS);
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) {
                SpellFx.sound(SFX_END, at, cb);
                return true;
            }
            SpellFx.hold(column, at, null, store);
            if (now - lastColumn >= COLUMN_EVERY_MS) { lastColumn = now; SpellFx.fx(FX_COLUMN, at, 1.0f, store, cb); }
            if (now - lastHum >= HUM_EVERY_MS) { lastHum = now; SpellFx.sound(SFX_HUM, at, cb); }
            if (now - lastFissure >= FISSURE_EVERY_MS && !cracks.isEmpty()) {                     // l'energie suinte des fissures
                lastFissure = now;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                for (int i = 0; i < FISSURE_FX_PER_PASS; i++) {
                    double[] c = cracks.get(rng.nextInt(cracks.size()));
                    SpellFx.fx(FX_FISSURE, new Vector3d(c[0] + 0.5, at.y + 0.3 - c[2], c[1] + 0.5), 1.0f, store, cb);
                }
            }
            if (now - lastScan >= SCAN_EVERY_MS) { lastScan = now; capture(now, store, cb); }
            pin(now, store, cb);
            if (now - lastDamage >= TICK_DAMAGE_EVERY_MS) {
                lastDamage = now;
                for (Ref<EntityStore> e : pinned) if (e.isValid()) SpellFx.damage(caster, e, "Crush", DAMAGE_PER_SECOND, null, null, cb);
            }
            return false;
        }

        /** Toute cible dans le cylindre (15 blocs de rayon, 50 de haut) est plaquee au sol. */
        private void capture(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, ZONE_RADIUS + 1, store)) {
                if (caughtAt.containsKey(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, at) > ZONE_RADIUS || p.y > at.y + COLUMN_HEIGHT || p.y < at.y - CRATER_DEPTH - 3) continue;
                caughtAt.put(e, now);
                SpellFx.push(e, new Vector3d(0, -SLAM_SPEED, 0), false, cb);
            }
        }

        /** Apres la chute, la cible est clouee (joueurs : effet ; monstres : verrou de position) jusqu'a la fin de la zone. */
        private void pin(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            caughtAt.entrySet().removeIf(en -> {
                Ref<EntityStore> e = en.getKey();
                if (!e.isValid()) { pinned.remove(e); return true; }
                if (pinned.contains(e)) return false;
                if (now - en.getValue() < PIN_DELAY_MS) { SpellFx.push(e, new Vector3d(0, -SLAM_SPEED, 0), false, cb); return false; }
                pinned.add(e);
                long left = Math.max(0, DURATION_MS - (now - startAt));                         // borne : rien ne survit a la zone
                EffectsManager.applyCustomEffect(e, CRUSH_EFFECT, left / 1000f, OverlapBehavior.OVERWRITE, cb);
                SpellFx.lockInPlace(e, left, store);
                Vector3d p = SpellFx.position(e, store);
                return false;
            });
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(column, cb);
            for (Ref<EntityStore> e : pinned) {
                if (!e.isValid()) continue;
                SpellFx.unlock(e);
                try { EffectsManager.removeEffect(e, CRUSH_EFFECT, cb); } catch (Exception ex) { EldaniorLogger.error("PuitsGravitationnel: effet", ex); }
            }
            pinned.clear();
            caughtAt.clear();
            TerrainRestore.save();
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(column);
            for (Ref<EntityStore> e : pinned) SpellFx.unlock(e);                                // l'effet expire seul (duree = temps restant)
            TerrainRestore.save();
        }
    }
}
