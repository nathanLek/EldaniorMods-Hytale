package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rune du Faconnage (Terre, Unique, rune de predilection du Demiurge) : domaine de 12 blocs au point vise
 * (25 blocs) pendant 12 s. Toutes les 0,5 s, un pic de roche jaillit sous un ennemi (pas un vise dans les 2 dernieres
 * secondes) : 90 + projection ; 0,2 s plus tard (l'ennemi a quitte la case), une colonne de basalte de 3 y est posee et
 * reste (24 pics max). Allies dans le domaine : Elda_Faconnage (-25 % physique/projectiles). A la fin les pics
 * s'effondrent (60 a 1,5 bloc) et le sol est restaure.
 * Fiche de conception : scripts/runes/specs/Rune_Faconnage.json
 */
public final class Faconnage implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, SPIKE_EVERY_MS = 500, RAISE_DELAY_MS = 200, RETARGET_MS = 2_000, ALLY_EVERY_MS = 500, ALLY_TINT_MS = 2_000;
    private static final double RANGE = 25.0, RADIUS = 12.0, HEIGHT = 5.0, LIFT = 12.0, CRUMBLE = 1.5;
    private static final int MAX_SPIKES = 24, SPIKE_HEIGHT = 3;
    private static final float SPIKE = 90f, COLLAPSE = 60f;

    // ===== Assets =====
    private static final String ROCK = "Rock_Basalt", GUARD = "Elda_Faconnage";
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_SPIKE = "SFX_Golem_Earth_Stomp_Impact", SFX_COLLAPSE = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "Faconnage"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        long now = System.currentTimeMillis();
        SpellFx.fx(FX_DUST, center, 3.0f, store, cb);
        SpellFx.shake(center, RADIUS + 8, "Impact_Light", store);
        return new Domain(world, center, TerrainRestore.open(world, now + DURATION_MS + 2_000), now);
    }

    /** Un pic : sa base (premiere case libre) et l'heure ou ses blocs seront poses. */
    private record Spike(int x, int y, int z, long raiseAt) {}

    private static final class Domain implements Instance {
        final World world;
        final Vector3d center;
        final TerrainRestore.Restore batch;
        final long startAt;
        final List<Spike> spikes = new ArrayList<>();
        final List<Spike> pending = new ArrayList<>();
        final Map<Ref<EntityStore>, Long> lastTargeted = new HashMap<>();
        final Map<Ref<EntityStore>, Long> guardedUntil = new HashMap<>();
        long lastSpike, lastAllies;

        Domain(World world, Vector3d center, TerrainRestore.Restore batch, long startAt) { this.world = world; this.center = center; this.batch = batch; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (now - startAt >= DURATION_MS) { collapse(caster, store, cb); return true; }
            pending.removeIf(s -> { if (now < s.raiseAt()) return false; raise(s); return true; });
            if (spikes.size() < MAX_SPIKES && now - lastSpike >= SPIKE_EVERY_MS) { lastSpike = now; strike(caster, now, store, cb); }
            if (now - lastAllies >= ALLY_EVERY_MS) { lastAllies = now; guardAllies(caster, now, store, cb); }
            return false;
        }

        private boolean inside(Vector3d p) { return SpellFx.horizontalDistance(p, center) <= RADIUS && Math.abs(p.y - center.y) <= HEIGHT; }

        /** Un pic jaillit sous un ennemi du domaine. */
        private void strike(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<Ref<EntityStore>> enemies = SpellFx.enemiesAround(caster, center, RADIUS + HEIGHT, store);
            Collections.shuffle(enemies);
            for (Ref<EntityStore> e : enemies) {
                Long last = lastTargeted.get(e);
                Vector3d p = SpellFx.position(e, store);
                if (p == null || !inside(p) || RuneSpellRuntime.isSummon(e) || (last != null && now - last < RETARGET_MS)) continue;
                lastTargeted.put(e, now);
                int x = (int) Math.floor(p.x), z = (int) Math.floor(p.z), y = floor(x, (int) Math.floor(p.y), z);
                SpellFx.damage(caster, e, "Earth", SPIKE, new Vector3d(0, LIFT, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_DUST, new Vector3d(x + 0.5, y, z + 0.5), 1.0f, store, cb);
                SpellFx.sound(SFX_SPIKE, p, cb);
                Spike s = new Spike(x, y, z, now + RAISE_DELAY_MS);
                spikes.add(s);
                pending.add(s);
                return;
            }
        }

        /** Pose la colonne du pic (cases vides seulement : un ennemi reste dans sa case = case sautee). */
        private void raise(Spike s) {
            world.execute(() -> {
                try {
                    String w = world.getName();
                    for (int k = 0; k < SPIKE_HEIGHT; k++) {
                        int y = s.y() + k;
                        if (!TerrainRestore.isEmpty(world, s.x(), y, s.z()) || TerrainRestore.isProtectedArea(w, s.x(), y, s.z())
                                || WorldBlockUtil.getFluidIndex(world, s.x(), y, s.z()) > 0) break;
                        TerrainRestore.place(world, batch, s.x(), y, s.z(), TerrainRestore.EMPTY, ROCK, 0);
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("Faconnage: pic", e);
                }
            });
        }

        /** Premiere case libre au-dessus du sol local (de 2 au-dessus a 4 en dessous). */
        private int floor(int x, int y0, int z) {
            for (int y = y0 + 2; y >= y0 - 4; y--) {
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
                if (t != null && t.getMaterial() == BlockMaterial.Solid && above != null && above.getMaterial() != BlockMaterial.Solid) return y + 1;
            }
            return y0;
        }

        /** Le sol protege les allies qui s'y tiennent. */
        private void guardAllies(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> a : SpellFx.alliesAround(caster, center, RADIUS + HEIGHT, store)) {
                Vector3d p = SpellFx.position(a, store);
                if (p == null || !inside(p)) continue;
                Long until = guardedUntil.get(a);
                if (until == null || until - now < ALLY_EVERY_MS * 2) {          // renouvele avant expiration : pas de clignotement
                    EffectsManager.applyCustomEffect(a, GUARD, ALLY_TINT_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
                    guardedUntil.put(a, now + ALLY_TINT_MS);
                }
            }
        }

        /** Fin : les pics s'effondrent. */
        private void collapse(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("Faconnage: fin", e); } });
            for (Spike s : spikes) {
                Vector3d at = new Vector3d(s.x() + 0.5, s.y(), s.z() + 0.5);
                SpellFx.fx(FX_CRUMBS, new Vector3d(at).add(0, 1, 0), 1.2f, store, cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, CRUMBLE + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p != null && SpellFx.horizontalDistance(p, at) <= CRUMBLE) SpellFx.damage(caster, e, "Bludgeoning", COLLAPSE, null, null, cb);
                }
            }
            SpellFx.sound(SFX_COLLAPSE, center, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
