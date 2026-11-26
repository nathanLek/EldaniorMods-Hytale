package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de la Galerie des Glaces (Lumiere, Epic) : dix reflets du lanceur (copies PNJ a 1 PV, figees) encerclent
 * l'ennemi vise pendant 10 s. Chaque coup du lanceur sur lui est repete par chaque reflet debout (15 %, coup "echo").
 * A la fin, les reflets convergent et eclatent (degats par reflet + eblouissement).
 * Fiche de conception : scripts/runes/specs/Rune_GalerieDesGlaces.json
 */
public final class GalerieDesGlaces implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, DAZZLE_MS = 1_500;
    private static final int REFLECTIONS = 10, ARCS_PER_HIT = 3;
    private static final double RANGE = 20.0, RING = 3.5, BURST_RADIUS = 3.0;
    private static final float ECHO = 0.15f, BURST_DAMAGE = 40f, REFLECTION_HEALTH = 1f;

    // ===== Assets =====
    private static final String ROLE = "Eldanior_Clone_Lumiere_Melee", DAZZLE = "Elda_Eblouissement";
    private static final String FX_MIRROR = "Bifrost_Divine", FX_SHARDS = "Block_Break_Ice";
    private static final String SFX_APPEAR = "SFX_Portal_Neutral_Open", SFX_SHATTER = "SFX_Glass_Break";

    @Override public String id() { return "GalerieDesGlaces"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);
        Vector3d tp = SpellFx.position(target, store);
        World world = SpellFx.world(store);
        if (tp == null || world == null) return null;
        long now = System.currentTimeMillis();
        SummonPack pack = new SummonPack(caster, FX_MIRROR);
        CloneFx.Look look = CloneFx.Look.of(caster, store);
        for (int i = 0; i < REFLECTIONS; i++) {
            double a = Math.PI * 2 * i / REFLECTIONS;
            Vector3d at = SpellFx.ground(world, new Vector3d(tp).add(Math.cos(a) * RING, 0, Math.sin(a) * RING));
            Vector3d inward = new Vector3d(tp).sub(at);
            pack.spawn(world, ROLE, at, SpellFx.facing(inward, false), look, CloneFx.heldItem(caster, store), REFLECTION_HEALTH, null, now);
            SpellFx.fx(FX_MIRROR, new Vector3d(at).add(0, 1, 0), 0.6f, store, cb);
        }
        SpellFx.sound(SFX_APPEAR, tp, cb);
        return new Gallery(target, tp, pack, now);
    }

    private static final class Gallery implements Instance {
        final Ref<EntityStore> target;
        final SummonPack pack;
        final long startAt;
        final Queue<Float> hits = new ConcurrentLinkedQueue<>();       // coups du lanceur sur la cible (traitement des degats)
        Vector3d targetPos;

        Gallery(Ref<EntityStore> target, Vector3d targetPos, SummonPack pack, long startAt) {
            this.target = target; this.targetPos = targetPos; this.pack = pack; this.startAt = startAt;
        }

        @Override
        public void onOwnerHit(Ref<EntityStore> t, float amount) {
            if (t.equals(target) && hits.size() < 16) hits.add(amount);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean over = now - startAt >= DURATION_MS || !target.isValid() || store.getComponent(target, DeathComponent.getComponentType()) != null;
            Vector3d tp = SpellFx.position(target, store);
            if (tp != null) targetPos = tp;
            int standing = 0;
            for (SummonPack.Member m : pack.members) {                                 // reflets figes face a la cible
                if (m.gone) continue;
                Ref<EntityStore> ref = m.ref();
                if (ref == null) continue;
                if (!ref.isValid() || RuneSpellRuntime.isDying(ref)) { pack.dissipate(m, store, cb); SpellFx.sound(SFX_SHATTER, m.lastPos, cb); continue; }
                standing++;
                SpellFx.lockInPlace(ref, 600, store);
                CloneFx.faceTowards(ref, targetPos, store);
            }
            if (over) { burst(caster, standing, store, cb); return true; }
            for (Float amount = hits.poll(); amount != null; amount = hits.poll()) echo(caster, amount, store, cb);
            return false;
        }

        /** Chaque reflet debout repete le coup (15 %), en coup "echo" pour ne pas etre recopie. */
        private void echo(Ref<EntityStore> caster, float amount, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            float each = amount * ECHO;
            if (each < 0.5f) return;
            int arcs = 0;
            Vector3d chest = new Vector3d(targetPos).add(0, 1, 0);
            for (SummonPack.Member m : pack.members) {
                if (m.gone || m.ref() == null || !m.ref().isValid()) continue;
                RuneSpellRuntime.markEcho(target, each);
                SpellFx.damage(caster, target, "Elemental", each, null, null, cb);
                if (arcs++ < ARCS_PER_HIT) SpellFx.arc(FX_MIRROR, new Vector3d(m.lastPos).add(0, 1.2, 0), chest, 0.2f, store, cb);
            }
        }

        /** Les reflets convergent et eclatent sur la cible. */
        private void burst(Ref<EntityStore> caster, int standing, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (standing <= 0) return;
            Vector3d chest = new Vector3d(targetPos).add(0, 1, 0);
            for (SummonPack.Member m : pack.members)
                if (!m.gone && m.ref() != null && m.ref().isValid()) SpellFx.arc(FX_MIRROR, new Vector3d(m.lastPos).add(0, 1.2, 0), chest, 0.3f, store, cb);
            SpellFx.fx(FX_MIRROR, chest, 2.0f, store, cb);
            SpellFx.fx(FX_SHARDS, chest, 1.5f, store, cb);
            SpellFx.sound(SFX_SHATTER, targetPos, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, targetPos, BURST_RADIUS, store))
                SpellFx.damage(caster, e, "Elemental", BURST_DAMAGE * standing, null, "Impact_Light", cb);
            if (target.isValid()) SpellFx.stun(target, DAZZLE, DAZZLE_MS, store, cb);
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { pack.cleanup(cb); }

        @Override public void abort() { pack.abort(); }
    }
}
