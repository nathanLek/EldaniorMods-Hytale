package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune du Surregime (Temps, Unique, rune de predilection du Chrono-Seigneur) : 15 s de temps accelere. Vitesse x1,8
 * (Elda_Surregime, pose une fois), chaque coup du lanceur frappe une seconde fois a 40 % 0,3 s plus tard (markEcho,
 * 4 echos par seconde au plus), mana +3 % par seconde. (Les recharges prevues par la feuille de route sont predites par
 * le client : les reduire cote serveur desynchroniserait l'interface.)
 * Fiche de conception : scripts/runes/specs/Rune_Surregime.json
 */
public final class Surregime implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, ECHO_DELAY_MS = 300, ECHO_GAP_MS = 250, MANA_EVERY_MS = 1_000, TRAIL_EVERY_MS = 150;
    private static final float ECHO = 0.4f, MANA = 0.03f;
    private static final double TRAIL_MIN_STEP = 0.4;

    // ===== Assets =====
    private static final String OVERDRIVE = "Elda_Surregime";
    private static final String FX_TRAIL = "Chronos_Dash", FX_ECHO = "Chronos_Afterimage", FX_BURST = "Chronos_Burst";
    private static final String SFX_CAST = "SFX_Arcane_Workbench_Craft";

    @Override public String id() { return "Surregime"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, OVERDRIVE, cb);
        SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1, 0), 1.5f, store, cb);
        SpellFx.sound(SFX_CAST, pos, cb);
        return new Overdrive(caster, pos, System.currentTimeMillis());
    }

    private record Echo(Ref<EntityStore> target, float amount, long at) {}

    private static final class Overdrive implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final Queue<Echo> echoes = new ConcurrentLinkedQueue<>();
        final Vector3d lastPos;
        volatile long lastEchoQueued;
        long lastMana, lastTrail;

        Overdrive(Ref<EntityStore> caster, Vector3d pos, long startAt) { this.caster = caster; this.lastPos = new Vector3d(pos); this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            long now = System.currentTimeMillis();
            if (amount <= 0 || now - lastEchoQueued < ECHO_GAP_MS) return;
            lastEchoQueued = now;
            echoes.add(new Echo(target, amount * ECHO, now + ECHO_DELAY_MS));
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            Echo e;
            while ((e = echoes.peek()) != null && now >= e.at()) {
                echoes.poll();
                Vector3d p = SpellFx.position(e.target(), store);
                if (p == null || e.amount() < 0.5f) continue;
                RuneSpellRuntime.markEcho(e.target(), e.amount());
                SpellFx.damage(caster, e.target(), "Elemental", e.amount(), null, "Impact_Light", cb);
                SpellFx.fx(FX_ECHO, new Vector3d(p).add(0, 1, 0), 0.9f, store, cb);
            }
            if (now - startAt >= DURATION_MS) return echoes.isEmpty();
            if (now - lastMana >= MANA_EVERY_MS) { lastMana = now; SpellFx.restore(caster, DefaultEntityStatTypes.getMana(), MANA, cb); }
            if (now - lastTrail >= TRAIL_EVERY_MS) {                             // trainee doree quand le lanceur court
                lastTrail = now;
                if (pos.distance(lastPos) >= TRAIL_MIN_STEP) SpellFx.fx(FX_TRAIL, new Vector3d(lastPos).add(0, 1, 0), 0.8f, store, cb);
                lastPos.set(pos);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, OVERDRIVE, cb); } catch (Exception ex) { EldaniorLogger.error("Surregime: effet", ex); }
        }
    }
}
