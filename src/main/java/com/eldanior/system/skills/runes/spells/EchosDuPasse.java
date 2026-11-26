package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Rune des Echos du Passe (Temps, Unique, rune de predilection du Tisseur Temporel) : pendant 12 s, trois remanences du
 * lanceur (porteur Projectile_Config_Chronos_Ghost habille de son modele et de son skin) le suivent avec 1, 2 et 3 s de
 * retard en rejouant ses poses ; chaque coup qu'il porte est rejoue par chacune au moment ou elle le revit (35 %,
 * markEcho). Les echos restent 3 s de plus pour finir de rejouer.
 * Fiche de conception : scripts/runes/specs/Rune_EchosDuPasse.json
 */
public final class EchosDuPasse implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long RECORD_MS = 12_000;
    private static final long[] DELAYS_MS = {1_000, 2_000, 3_000};
    private static final float ECHO = 0.35f;
    private static final int MAX_HITS = 30;
    private static final double MAX_REACH = 30.0;

    // ===== Assets =====
    private static final String GHOST_CARRIER = "Projectile_Config_Chronos_Ghost", GHOST_EFFECT = "Elda_Echo";
    private static final String FX_DASH = "Chronos_Dash", FX_HIT = "Chronos_Afterimage", FX_BURST = "Chronos_Burst";
    private static final String SFX_OPEN = "SFX_Portal_Neutral_Open";

    @Override public String id() { return "EchosDuPasse"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        long now = System.currentTimeMillis();
        ModelComponent mc = store.getComponent(caster, ModelComponent.getComponentType());
        PlayerSkinComponent skin = store.getComponent(caster, PlayerSkinComponent.getComponentType());
        Weave w = new Weave(caster, mc != null ? mc.getModel() : null, skin != null ? skin.getPlayerSkin() : null, now);
        Sample first = sample(caster, pos, now, store);
        w.samples.add(first);
        for (long d : DELAYS_MS) {
            Ref<EntityStore> ref = SpellFx.spawnModel(caster, GHOST_CARRIER, pos, new Vector3d(1, 0, 0), cb);
            SpellFx.notSaved(ref, store, cb);
            w.ghosts.add(new Ghost(ref, d, now));
        }
        SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1, 0), 1.6f, store, cb);
        SpellFx.sound(SFX_OPEN, pos, cb);
        return w;
    }

    record Sample(long time, Vector3d pos, Rotation3f body, Rotation3f head, MovementStates states) {}
    private record Hit(Ref<EntityStore> target, float amount, long time) {}

    private static Sample sample(Ref<EntityStore> caster, Vector3d pos, long now, Store<EntityStore> store) {
        TransformComponent t = store.getComponent(caster, TransformComponent.getComponentType());
        HeadRotation head = store.getComponent(caster, HeadRotation.getComponentType());
        MovementStatesComponent ms = store.getComponent(caster, MovementStatesComponent.getComponentType());
        return new Sample(now, new Vector3d(pos), t != null ? new Rotation3f(t.getRotation()) : new Rotation3f(),
                head != null ? new Rotation3f(head.getRotation()) : null,
                ms != null && ms.getMovementStates() != null ? new MovementStates(ms.getMovementStates()) : null);
    }

    /** Remanence : porteur habille du lanceur, qui revit ses poses avec `delay` ms de retard. */
    private static final class Ghost {
        final Ref<EntityStore> ref;
        final long delay, born;
        boolean dressed;
        int replayed;                                                       // nombre de coups deja rejoues
        Ghost(Ref<EntityStore> ref, long delay, long born) { this.ref = ref; this.delay = delay; this.born = born; }
    }

    private static final class Weave implements Instance {
        final Ref<EntityStore> caster;
        final Model model;
        final PlayerSkin skin;
        final long startAt;
        final Deque<Sample> samples = new ArrayDeque<>();
        final List<Ghost> ghosts = new ArrayList<>();
        final List<Hit> hits = new CopyOnWriteArrayList<>();

        Weave(Ref<EntityStore> caster, Model model, PlayerSkin skin, long startAt) { this.caster = caster; this.model = model; this.skin = skin; this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            long now = System.currentTimeMillis();
            if (amount > 0 && now - startAt < RECORD_MS && hits.size() < MAX_HITS) hits.add(new Hit(target, amount, now));
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            long maxDelay = DELAYS_MS[DELAYS_MS.length - 1];
            if (now - startAt < RECORD_MS) samples.addLast(sample(caster, pos, now, store));
            while (samples.size() > 1 && now - samples.peekFirst().time() > maxDelay + 500) samples.pollFirst();
            if (now - startAt >= RECORD_MS + maxDelay) return true;

            for (Ghost g : ghosts) {
                if (g.ref == null || !g.ref.isValid()) continue;
                if (!g.dressed) dress(g, cb);
                Sample p = at(now - g.delay);
                if (p != null) pose(g, p, store);
                replay(g, now, p, store, cb);
            }
            return false;
        }

        /** Pose enregistree la plus recente a l'instant `time` (ou la plus ancienne disponible). */
        private Sample at(long time) {
            Sample best = samples.peekFirst();
            for (Sample s : samples) { if (s.time() > time) break; best = s; }
            return best;
        }

        private void replay(Ghost g, long now, Sample p, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            while (g.replayed < hits.size() && hits.get(g.replayed).time() + g.delay <= now) {
                Hit h = hits.get(g.replayed++);
                Vector3d tp = SpellFx.position(h.target(), store);
                float echo = h.amount() * ECHO;
                if (tp == null || p == null || echo < 0.5f || tp.distance(p.pos()) > MAX_REACH) continue;
                RuneSpellRuntime.markEcho(h.target(), echo);
                SpellFx.damage(caster, h.target(), "Elemental", echo, null, "Impact_Light", cb);
                Vector3d from = new Vector3d(p.pos()).add(0, 1.2, 0), to = new Vector3d(tp).add(0, 1, 0);
                for (int i = 1; i <= 3; i++) SpellFx.fx(FX_DASH, new Vector3d(from).lerp(to, i / 4.0), 0.6f, store, cb);
                SpellFx.fx(FX_HIT, to, 0.9f, store, cb);
            }
        }

        private void dress(Ghost g, CommandBuffer<EntityStore> cb) {
            g.dressed = true;
            if (model != null) cb.putComponent(g.ref, ModelComponent.getComponentType(), new ModelComponent(model));
            if (skin != null) cb.putComponent(g.ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin));
            cb.putComponent(g.ref, MovementStatesComponent.getComponentType(), new MovementStatesComponent());
            SpellFx.effect(g.ref, GHOST_EFFECT, cb);
        }

        private static void pose(Ghost g, Sample p, Store<EntityStore> store) {
            SpellFx.hold(g.ref, p.pos(), p.body(), store);
            if (p.head() != null) {
                HeadRotation head = store.getComponent(g.ref, HeadRotation.getComponentType());
                if (head != null) head.setRotation(p.head());
            }
            if (p.states() != null) {
                MovementStatesComponent ms = store.getComponent(g.ref, MovementStatesComponent.getComponentType());
                if (ms != null && !p.states().equals(ms.getMovementStates())) ms.setMovementStates(new MovementStates(p.states()));
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Ghost g : ghosts) SpellFx.remove(g.ref, cb);
        }

        @Override
        public void abort() {
            for (Ghost g : ghosts) RuneSpellRuntime.removeLater(g.ref);
        }
    }
}
