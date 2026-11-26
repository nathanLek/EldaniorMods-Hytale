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

import java.util.ArrayList;
import java.util.List;

/**
 * Rune du Pilier Ascendant (Terre, Epic, rune de predilection du Grand Thaumaturge) : une colonne de basalte 3x3 pousse
 * sous le lanceur (6 couches, une toutes les 150 ms) et l'eleve avec les allies a 1,5 bloc (teleportes d'un bloc avant
 * chaque couche, posee au tick suivant). Elle tient 5 s puis s'effondre : blocs restaures, 200 degats a 5 blocs de la base
 * et etourdissement 1 s. Seules les cases vides recoivent un bloc ; zone protegee : rien.
 * Fiche de conception : scripts/runes/specs/Rune_PilierAscendant.json
 */
public final class PilierAscendant implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long LAYER_MS = 150, HOLD_MS = 5_000, STUN_MS = 1_000, FALL_GUARD_MS = 4_000, SAFETY_RESTORE_MS = 10_000;
    private static final int LAYERS = 6;
    private static final double PASSENGER_RADIUS = 1.5, BLAST_RADIUS = 5.0;
    private static final float DAMAGE = 200f;

    // ===== Assets =====
    private static final String PILLAR = "Rock_Basalt", STUN = "Gaia_Etourdi";
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_RISE = "SFX_Golem_Earth_Wake", SFX_COLLAPSE = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "PilierAscendant"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        int bx = (int) Math.floor(pos.x), by = (int) Math.floor(pos.y), bz = (int) Math.floor(pos.z);
        if (TerrainRestore.isProtectedArea(world.getName(), bx, by, bz)) return null;
        List<Ref<EntityStore>> riders = new ArrayList<>(SpellFx.alliesAround(caster, pos, PASSENGER_RADIUS, store));   // lanceur compris
        for (Ref<EntityStore> r : riders) SpellFx.protectFromFall(r, HOLD_MS + LAYERS * LAYER_MS + FALL_GUARD_MS, store);
        SpellFx.fx(FX_DUST, pos, 1.5f, store, cb);
        SpellFx.sound(SFX_RISE, pos, cb);
        long now = System.currentTimeMillis();
        return new Pillar(world, bx, by, bz, riders, TerrainRestore.open(world, now + SAFETY_RESTORE_MS), now);
    }

    private static final class Pillar implements Instance {
        final World world;
        final int bx, by, bz;
        final List<Ref<EntityStore>> riders;
        final TerrainRestore.Restore batch;
        final long startAt;
        int raised = 0, pendingLayer = -1;                                       // couches deja montees / a poser au tick suivant
        boolean blocked;
        long topAt, lastLayer;

        Pillar(World world, int bx, int by, int bz, List<Ref<EntityStore>> riders, TerrainRestore.Restore batch, long startAt) {
            this.world = world; this.bx = bx; this.by = by; this.bz = bz; this.riders = riders; this.batch = batch; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) { collapse(caster, store, cb, false); return true; }
            if (pendingLayer >= 0) { placeLayer(pendingLayer); pendingLayer = -1; }   // les passagers ont deja ete leves
            if (topAt == 0) {
                if (now - lastLayer >= LAYER_MS) {
                    lastLayer = now;
                    if (raised >= LAYERS || blocked || !headroom(raised)) topAt = now;
                    else lift(store, cb);
                }
                return false;
            }
            if (now - topAt >= HOLD_MS) { collapse(caster, store, cb, true); return true; }
            return false;
        }

        /** Monte les passagers d'un bloc, la couche sera posee sous eux au tick suivant. */
        private void lift(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> r : riders) {
                Vector3d p = SpellFx.position(r, store);
                if (p != null) SpellFx.teleport(r, new Vector3d(p.x, by + raised + 1.02, p.z), store, cb);
            }
            SpellFx.fx(FX_DUST, new Vector3d(bx + 0.5, by + raised, bz + 0.5), 1.2f, store, cb);
            pendingLayer = raised++;
        }

        /** Il faut 2 cases libres au-dessus de la couche suivante (pas de plafond). */
        private boolean headroom(int layer) {
            for (int dy = 1; dy <= 2; dy++) {
                BlockType t = WorldBlockUtil.getBlockType(world, bx, by + layer + dy, bz);
                if (t == null || t.getMaterial() == BlockMaterial.Solid) { blocked = true; return false; }
            }
            return true;
        }

        private void placeLayer(int layer) {
            int y = by + layer;
            world.execute(() -> {
                try {
                    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                        int x = bx + dx, z = bz + dz;
                        if (TerrainRestore.isEmpty(world, x, y, z) && !TerrainRestore.isProtectedArea(world.getName(), x, y, z))
                            TerrainRestore.place(world, batch, x, y, z, TerrainRestore.EMPTY, PILLAR, 0);
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("PilierAscendant: couche", e);
                }
            });
        }

        /** Effondrement : la colonne disparait (restauree), souffle de gravats autour de la base. */
        private void collapse(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb, boolean blast) {
            world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("PilierAscendant: effondrement", e); } });
            Vector3d base = new Vector3d(bx + 0.5, by, bz + 0.5);
            for (int k = 0; k < raised; k += 2) SpellFx.fx(FX_CRUMBS, new Vector3d(base).add(0, k + 0.5, 0), 1.6f, store, cb);
            SpellFx.fx(FX_DUST, base, 3.0f, store, cb);
            SpellFx.sound(SFX_COLLAPSE, base, cb);
            if (!blast) return;
            SpellFx.shake(base, 20, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, base, BLAST_RADIUS + 2, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, base) > BLAST_RADIUS || p.y < by - 2 || p.y > by + LAYERS + 2) continue;
                SpellFx.damage(caster, e, "Bludgeoning", DAMAGE, new Vector3d(0, 3, 0), "Impact_Strong", cb);
                SpellFx.stun(e, STUN, STUN_MS, store, cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }   // interruption : pas de pilier qui reste

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
