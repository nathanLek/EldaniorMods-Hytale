package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune du Meteore (Feu, Epic, rune de predilection du Magus) : cercle d'alerte 1,2 s au point vise (25 blocs), puis un
 * meteore (modele du Courroux de Muspelheim ramene a l'echelle 5) tombe en biais et s'ecrase : 400 degats a 2 blocs,
 * 250 jusqu'a 5, projection et brulure. Cratere (roche en fusion au fond, terre brulee autour) restaure apres 60 s.
 * Fiche de conception : scripts/runes/specs/Rune_Meteore.json
 */
public final class Meteore implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long IMPACT_MS = 1_200, FALL_MS = 600, ZONE_EVERY_MS = 100, RESTORE_AFTER_MS = 60_000;
    private static final double RANGE = 25.0, INNER = 2.0, OUTER = 5.0, FALL_HEIGHT = 30.0, FALL_BACK = 14.0, PUSH = 12.0, PUSH_LIFT = 7.0;
    private static final double CRATER_RADIUS = 2.5, BURN_RADIUS = 4.5;
    private static final float INNER_DAMAGE = 400f, OUTER_DAMAGE = 250f, METEOR_SCALE = 5f;

    // ===== Assets =====
    private static final String METEOR = "Projectile_Config_Muspelheim_Meteor", METEOR_MODEL = "Muspelheim_Meteor", BURN = "Elda_Brulure";
    private static final String FX_ZONE = "Muspelheim_Impact_Zone", FX_EXPLOSION = "Explosion_Big", FX_BLAST = "Gobliterator_Furnace_Blast", FX_FIRE = "Impact_Fire";
    private static final String LAVA = "Rock_Volcanic_Cracked_Lava", BURNT = "Soil_Dirt_Burnt";
    private static final String SFX_FALL = "SFX_Gobliterator_Rocket_Barrage_Projectile", SFX_IMPACT = "SFX_Gobliterator_Rocket_Barrage_Explosion";

    @Override public String id() { return "Meteore"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d impact = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        Vector3d from = new Vector3d(impact).sub(SpellFx.forward(caster, store).mul(FALL_BACK)).add(0, FALL_HEIGHT, 0);   // arrive de derriere le lanceur
        return new Fall(impact, from, SpellFx.world(store), System.currentTimeMillis());
    }

    private static boolean solid(World world, int x, int y, int z) {
        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
        return t != null && t.getMaterial() == BlockMaterial.Solid;
    }

    private static final class Fall implements Instance {
        final Vector3d impact, from;
        final World world;
        final long startAt;
        Ref<EntityStore> meteor;
        long lastZone;
        boolean scaled;

        Fall(Vector3d impact, Vector3d from, World world, long startAt) { this.impact = impact; this.from = from; this.world = world; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            long t = now - startAt;
            if (t >= IMPACT_MS) { explode(caster, store, cb); return true; }
            if (now - lastZone >= ZONE_EVERY_MS) { lastZone = now; SpellFx.fx(FX_ZONE, new Vector3d(impact).add(0, 0.1, 0), (float) (OUTER / 3.0), store, cb); }
            if (t >= IMPACT_MS - FALL_MS) {
                Vector3d dir = new Vector3d(impact).sub(from).normalize();
                double k = Math.min(1.0, (t - (IMPACT_MS - FALL_MS)) / (double) FALL_MS);
                Vector3d pos = new Vector3d(from).lerp(impact, k);
                if (meteor == null) {
                    meteor = SpellFx.spawnModel(caster, METEOR, pos, dir, cb);
                    SpellFx.notSaved(meteor, store, cb);
                    SpellFx.sound(SFX_FALL, impact, cb);
                } else if (meteor.isValid()) {
                    if (!scaled) { scaled = true; SpellFx.setModelScale(meteor, METEOR_MODEL, METEOR_SCALE, 0.1, cb); }
                    SpellFx.hold(meteor, pos, SpellFx.facing(dir, true), store);
                }
            }
            return false;
        }

        private void explode(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.remove(meteor, cb);
            Vector3d center = new Vector3d(impact).add(0, 0.5, 0);
            SpellFx.fx(FX_EXPLOSION, new Vector3d(impact).add(0, 1, 0), 3.0f, store, cb);
            SpellFx.fx(FX_BLAST, center, 1.2f, store, cb);
            SpellFx.fx(FX_FIRE, center, 2.5f, store, cb);
            SpellFx.sound(SFX_IMPACT, impact, cb);
            SpellFx.shake(impact, 25, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, OUTER + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = SpellFx.horizontalDistance(p, impact);
                if (d > OUTER || Math.abs(p.y - impact.y) > OUTER) continue;
                Vector3d away = new Vector3d(p.x - impact.x, 0, p.z - impact.z);
                if (away.lengthSquared() < 1e-6) away.set(1, 0, 0);
                SpellFx.damage(caster, e, "Fire", d <= INNER ? INNER_DAMAGE : OUTER_DAMAGE, away.normalize().mul(PUSH).add(0, PUSH_LIFT, 0), "Impact_Strong", cb);
                SpellFx.effect(e, BURN, cb);
            }
            crater();
        }

        /** Cratere : sphere de blocs retires sous l'impact, roche en fusion au fond, terre brulee autour (tout restaure). */
        private void crater() {
            if (world == null) return;
            int cx = (int) Math.floor(impact.x), cy = (int) Math.floor(impact.y) - 1, cz = (int) Math.floor(impact.z);
            TerrainRestore.Restore batch = TerrainRestore.open(world, System.currentTimeMillis() + RESTORE_AFTER_MS);
            world.execute(() -> {
                try {
                    int r = (int) Math.ceil(BURN_RADIUS);
                    for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                        double h = Math.hypot(dx, dz);
                        int x = cx + dx, z = cz + dz;
                        if (h <= CRATER_RADIUS) {
                            for (int dy = 1; dy >= -2; dy--) {                     // cuvette : plus profonde au centre
                                int y = cy + dy;
                                if (Math.sqrt(h * h + dy * dy * 1.6) > CRATER_RADIUS) continue;
                                carve(batch, x, y, z, TerrainRestore.EMPTY);
                            }
                        } else if (h <= BURN_RADIUS) {                              // terre brulee sur la surface autour
                            for (int y = cy + 2; y >= cy - 2; y--) {
                                if (!solid(world, x, y, z)) continue;
                                if (!solid(world, x, y + 1, z)) {
                                    BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                                    if (t != null && t.getId().startsWith("Soil_")) carve(batch, x, y, z, BURNT);
                                }
                                break;
                            }
                        }
                    }
                    int bottom = cy - 1;                                            // dernier bloc creuse au centre : roche en fusion dessous
                    if (solid(world, cx, bottom - 1, cz)) carve(batch, cx, bottom - 1, cz, LAVA);
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("Meteore: cratere", e);
                }
            });
        }

        private void carve(TerrainRestore.Restore batch, int x, int y, int z, String placed) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            if (t == null || TerrainRestore.isEmpty(world, x, y, z)) return;
            String id = t.getId();
            if (TerrainRestore.isKept(world, id, x, y, z) || TerrainRestore.isProtectedArea(world.getName(), x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) return;
            TerrainRestore.place(world, batch, x, y, z, id, placed, 0);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { SpellFx.remove(meteor, cb); }

        @Override
        public void abort() { RuneSpellRuntime.removeLater(meteor); }
    }
}
