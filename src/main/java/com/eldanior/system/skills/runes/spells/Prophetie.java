package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de la Prophetie (Temps, Legendaire, rune de predilection du Prophete) : 15 s pour le lanceur et ses allies a
 * 15 blocs. Elda_Prophetie (+30 % de degats infliges) ; protection RuneSpellRuntime.ward : les 3 premiers coups portes par
 * un attaquant sont annules, et l'attaquant est frappe par le destin (80 + Chronos_Fige 0,5 s). Les coups sans attaquant
 * (chute) ne consomment pas d'esquive.
 * Fiche de conception : scripts/runes/specs/Rune_Prophetie.json
 */
public final class Prophetie implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, GLOW_EVERY_MS = 1_000, STUN_MS = 500;
    private static final double RADIUS = 15.0;
    private static final int DODGES = 3;
    private static final float RETRIBUTION = 80f;

    // ===== Assets =====
    private static final String PROPHECY = "Elda_Prophetie", STUN = "Chronos_Fige";
    private static final String FX_GLOW = "Chronos_Gold", FX_DODGE = "Chronos_Afterimage", FX_STRIKE = "Chronos_Burst";
    private static final String SFX_CAST = "SFX_Divine_Respawn", SFX_DODGE = "SFX_Portal_Neutral";

    @Override public String id() { return "Prophetie"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Prophecy prophecy = new Prophecy(System.currentTimeMillis());
        for (Ref<EntityStore> a : SpellFx.alliesAround(caster, pos, RADIUS, store)) {
            Omen omen = new Omen(prophecy);
            prophecy.seers.put(a, omen);
            RuneSpellRuntime.ward(a, omen);
            EffectsManager.applyCustomEffect(a, PROPHECY, DURATION_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
            Vector3d p = SpellFx.position(a, store);
            if (p != null) SpellFx.fx(FX_GLOW, new Vector3d(p).add(0, 1.8, 0), 1.2f, store, cb);
        }
        SpellFx.sound(SFX_CAST, pos, cb);
        return prophecy;
    }

    private record Dodge(Ref<EntityStore> seer, Ref<EntityStore> attacker) {}

    /** L'avenir d'un allie : 3 coups qu'il voit venir. */
    private static final class Omen implements RuneSpellRuntime.Ward {
        final Prophecy owner;
        volatile int dodges = DODGES;

        Omen(Prophecy owner) { this.owner = owner; }

        @Override
        public float onHurt(Ref<EntityStore> target, Ref<EntityStore> attacker, float amount) {
            if (attacker == null || attacker.equals(target) || dodges <= 0) return amount;
            dodges--;
            owner.dodged.add(new Dodge(target, attacker));                         // riposte au tick du lanceur
            return 0;
        }
    }

    private static final class Prophecy implements Instance {
        final long startAt;
        final Map<Ref<EntityStore>, Omen> seers = new HashMap<>();
        final Queue<Dodge> dodged = new ConcurrentLinkedQueue<>();
        long lastGlow;

        Prophecy(long startAt) { this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Dodge d;
            while ((d = dodged.poll()) != null) retribution(caster, d, store, cb);
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastGlow >= GLOW_EVERY_MS) {
                lastGlow = now;
                for (Map.Entry<Ref<EntityStore>, Omen> en : seers.entrySet()) {
                    if (en.getValue().dodges <= 0) continue;
                    Vector3d p = SpellFx.position(en.getKey(), store);
                    if (p != null) SpellFx.fx(FX_GLOW, new Vector3d(p).add(0, 2.2, 0), 0.6f, store, cb);
                }
            }
            return false;
        }

        /** Le coup est evite ; le destin frappe celui qui l'a porte. */
        private void retribution(Ref<EntityStore> caster, Dodge d, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d p = SpellFx.position(d.seer(), store);
            if (p != null) { SpellFx.fx(FX_DODGE, new Vector3d(p).add(0, 1, 0), 1.0f, store, cb); SpellFx.sound(SFX_DODGE, p, cb); }
            Vector3d q = SpellFx.position(d.attacker(), store);
            if (q == null || !SpellFx.isEnemy(caster, d.attacker(), store)) return;
            SpellFx.fx(FX_STRIKE, new Vector3d(q).add(0, 1, 0), 1.0f, store, cb);
            SpellFx.damage(caster, d.attacker(), "Elemental", RETRIBUTION, null, "Impact_Light", cb);
            SpellFx.stun(d.attacker(), STUN, STUN_MS, store, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { seers.forEach(RuneSpellRuntime::unward); }

        @Override
        public void abort() { seers.forEach(RuneSpellRuntime::unward); }
    }
}
