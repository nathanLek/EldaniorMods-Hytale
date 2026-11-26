package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.NpcAttitudes;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Marionnette (Tenebres, Epic) : le monstre vise passe sous le controle du lanceur pendant 10 s. Il ignore le
 * lanceur et ses allies (attitude forcee), ses coups sur le lanceur sont annules (onOwnerHurt), et il attaque le monstre
 * le plus proche, qui le prend pour cible a son tour. Un fil d'ombre (faisceau) relie la main du lanceur a la marionnette.
 * Fiche de conception : scripts/runes/specs/Rune_Marionnette.json
 */
public final class Marionnette implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, DRIVE_EVERY_MS = 500;
    private static final double RANGE = 20.0, HUNT_RANGE = 15.0, ATTITUDE_S = 1.5;
    private static final float STRING_SCALE = 0.3f;

    // ===== Assets =====
    private static final String CONTROLLED = "Elda_Marionnette", STRING = "Nyx_Chain";
    private static final String FX_GRAB = "Nyx_Void";
    private static final String SFX_GRAB = "SFX_Crawler_Void_Alerted";

    @Override public String id() { return "Marionnette"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> puppet = SpellFx.targetEntity(caster, store, RANGE);
        if (puppet == null || RuneSpellRuntime.isSummon(puppet) || !CloneFx.isMonster(puppet, store)) return null;
        Vector3d p = SpellFx.position(puppet, store), pos = SpellFx.position(caster, store);
        if (p == null || pos == null) return null;
        SpellFx.effect(puppet, CONTROLLED, cb);
        SpellFx.fx(FX_GRAB, p, 1.0f, store, cb);
        SpellFx.sound(SFX_GRAB, p, cb);
        Ref<EntityStore> string = SpellFx.spawnBeam(STRING, hand(caster, store), new Vector3d(p).add(0, 1.6, 0), STRING_SCALE, DURATION_MS + 1_000, store, cb);
        return new Puppet(puppet, string, System.currentTimeMillis());
    }

    private static Vector3d hand(Ref<EntityStore> caster, Store<EntityStore> store) {
        Vector3d pos = SpellFx.position(caster, store);
        return pos == null ? null : pos.add(0, 1.4, 0).add(SpellFx.forward(caster, store).mul(0.5));
    }

    private static final class Puppet implements Instance {
        final Ref<EntityStore> puppet, string;
        final long startAt;
        long lastDrive;

        Puppet(Ref<EntityStore> puppet, Ref<EntityStore> string, long startAt) { this.puppet = puppet; this.string = string; this.startAt = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return puppet.equals(attacker) ? 0 : amount;                                 // la marionnette ne blesse pas son maitre
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS || !puppet.isValid() || store.getComponent(puppet, DeathComponent.getComponentType()) != null) return true;
            Vector3d p = SpellFx.position(puppet, store), h = hand(caster, store);
            if (p == null || h == null) return true;
            SpellFx.beam(string, STRING, h, new Vector3d(p).add(0, 1.6, 0), STRING_SCALE, store);
            if (now - lastDrive < DRIVE_EVERY_MS) return false;
            lastDrive = now;
            NpcAttitudes.force(puppet, caster, Attitude.IGNORE, ATTITUDE_S, store);
            for (Ref<EntityStore> a : SpellFx.alliesAround(caster, p, HUNT_RANGE, store)) NpcAttitudes.force(puppet, a, Attitude.IGNORE, ATTITUDE_S, store);
            Ref<EntityStore> prey = null;
            double best = HUNT_RANGE * HUNT_RANGE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, HUNT_RANGE, store)) {
                if (e.equals(puppet) || RuneSpellRuntime.isSummon(e) || !CloneFx.isMonster(e, store)) continue;
                Vector3d q = SpellFx.position(e, store);
                if (q == null) continue;
                double d = q.distanceSquared(p);
                if (d < best) { best = d; prey = e; }
            }
            if (prey == null) { CloneFx.mark(puppet, "LockedTarget", null, store); return false; }
            CloneFx.hostile(puppet, prey, ATTITUDE_S, store);
            CloneFx.hostile(prey, puppet, ATTITUDE_S, store);
            CloneFx.mark(puppet, "LockedTarget", prey, store);
            CloneFx.mark(prey, "LockedTarget", puppet, store);
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(string, cb);
            if (!puppet.isValid()) return;
            try { EffectsManager.removeEffect(puppet, CONTROLLED, cb); } catch (Exception e) { EldaniorLogger.error("Marionnette: effet", e); }
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(string);
        }
    }
}
