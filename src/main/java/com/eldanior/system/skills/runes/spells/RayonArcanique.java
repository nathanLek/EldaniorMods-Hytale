package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
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
 * Rune du Rayon Arcanique (Lumiere, Unique, rune de predilection du Magus Supreme) : rayon canalise 5 s qui suit le regard
 * (30 blocs, arrete par le premier bloc solide). Chaque ennemi traverse (1,2 bloc de l'axe) subit 30 degats toutes les
 * 0,25 s ; le lanceur est ralenti (Elda_Canalisation). Faisceau "Basic" (bleu) replace a chaque tick + particules.
 * Fiche de conception : scripts/runes/specs/Rune_RayonArcanique.json
 */
public final class RayonArcanique implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 5_000, HIT_EVERY_MS = 250, FX_EVERY_MS = 200, SOUND_EVERY_MS = 1_500;
    private static final double RANGE = 30.0, WIDTH = 1.2, STEP = 0.5, EYE = 1.5, FX_SPACING = 2.0;
    private static final float DAMAGE = 30f, BEAM_SCALE = 1.5f;

    // ===== Assets =====
    private static final String CHANNEL = "Elda_Canalisation", BEAM = "Basic";
    private static final String FX_BEAM = "Astral_Esprit", FX_END = "Bifrost_Divine";
    private static final String SFX_HUM = "SFX_Portal_Neutral";

    @Override public String id() { return "RayonArcanique"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        SpellFx.effect(caster, CHANNEL, cb);
        return new Channel(caster, SpellFx.world(store), System.currentTimeMillis());
    }

    private static double distanceToSegment(Vector3d p, Vector3d a, Vector3d b) {
        Vector3d ab = new Vector3d(b).sub(a);
        double len2 = ab.lengthSquared();
        double t = len2 < 1e-9 ? 0 : Math.max(0, Math.min(1, new Vector3d(p).sub(a).dot(ab) / len2));
        return p.distance(new Vector3d(a).add(ab.mul(t)));
    }

    private static final class Channel implements Instance {
        final Ref<EntityStore> caster;
        final World world;
        final long startAt;
        Ref<EntityStore> beam;
        long lastHit, lastFx, lastSound;

        Channel(Ref<EntityStore> caster, World world, long startAt) { this.caster = caster; this.world = world; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= DURATION_MS) return true;
            Vector3d dir = SpellFx.lookDirection(caster, store);
            Vector3d from = new Vector3d(pos).add(0, EYE, 0).add(new Vector3d(dir).mul(0.6));
            Vector3d to = trace(from, dir);

            if (beam == null) beam = SpellFx.spawnBeam(BEAM, from, to, BEAM_SCALE, DURATION_MS + 500, store, cb);
            else SpellFx.beam(beam, BEAM, from, to, BEAM_SCALE, store);
            if (now - lastFx >= FX_EVERY_MS) {
                lastFx = now;
                double len = from.distance(to);
                for (double d = 1; d < len; d += FX_SPACING) SpellFx.fx(FX_BEAM, new Vector3d(from).add(new Vector3d(dir).mul(d)), 0.5f, store, cb);
                SpellFx.fx(FX_END, to, 0.6f, store, cb);
            }
            if (now - lastSound >= SOUND_EVERY_MS) { lastSound = now; SpellFx.sound(SFX_HUM, pos, cb); }
            if (now - lastHit >= HIT_EVERY_MS) {
                lastHit = now;
                Vector3d mid = new Vector3d(from).add(to).mul(0.5);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, mid, from.distance(to) / 2 + WIDTH + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || distanceToSegment(p.add(0, 1, 0), from, to) > WIDTH) continue;
                    SpellFx.damage(caster, e, "Elemental", DAMAGE, null, null, cb);
                    SpellFx.fx(FX_END, p, 0.4f, store, cb);
                }
            }
            return false;
        }

        /** Bout du rayon : premier bloc solide (ou chunk non charge) le long du regard, sinon 30 blocs. */
        private Vector3d trace(Vector3d from, Vector3d dir) {
            Vector3d to = new Vector3d(from);
            for (double d = STEP; d <= RANGE; d += STEP) {
                Vector3d next = new Vector3d(from).add(new Vector3d(dir).mul(d));
                BlockType t = world != null ? WorldBlockUtil.getBlockType(world, (int) Math.floor(next.x), (int) Math.floor(next.y), (int) Math.floor(next.z)) : null;
                if (t == null || t.getMaterial() == BlockMaterial.Solid) break;
                to = next;
            }
            return to;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(beam, cb);
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, CHANNEL, cb); } catch (Exception e) { EldaniorLogger.error("RayonArcanique: effet", e); }
        }

        @Override
        public void abort() { RuneSpellRuntime.removeLater(beam); }
    }
}
