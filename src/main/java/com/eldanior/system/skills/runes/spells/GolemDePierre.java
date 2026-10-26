package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune du Golem de Pierre (Terre, Rare) : invoque un golem (PNJ Eldanior_Summon_Golem, 600 PV) pendant 20 s. Il
 * provoque les monstres a moins de 8 blocs (ils le prennent pour cible) et frappe le plus proche ; sinon il suit le lanceur.
 * Fiche de conception : scripts/runes/specs/Rune_GolemDePierre.json
 */
public final class GolemDePierre implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 20_000, TAUNT_EVERY_MS = 1_000, DRIVE_EVERY_MS = 500;
    private static final double SPAWN_AHEAD = 3.0, FIGHT_RANGE = 15.0, TAUNT_RADIUS = 8.0, TAUNT_S = 3.0;
    private static final float MAX_HEALTH = 600f;

    // ===== Assets =====
    private static final String ROLE = "Eldanior_Summon_Golem";
    private static final String FX_DUST = "Gaia_Dust_Burst";
    private static final String SFX_WAKE = "SFX_Golem_Frost_Wake";

    @Override public String id() { return "GolemDePierre"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        World world = SpellFx.world(store);
        Vector3d fwd = SpellFx.forward(caster, store);
        Vector3d at = SpellFx.ground(world, new Vector3d(pos).add(new Vector3d(fwd).mul(SPAWN_AHEAD)));
        long now = System.currentTimeMillis();
        SummonPack pack = new SummonPack(caster, FX_DUST);
        pack.spawn(world, ROLE, at, SpellFx.facing(fwd, false), MAX_HEALTH, null, now);
        SpellFx.fx(FX_DUST, new Vector3d(at).add(0, 0.5, 0), 1.4f, store, cb);
        SpellFx.sound(SFX_WAKE, at, cb);
        SpellFx.shake(at, 12, "Impact_Light", store);
        return new Golem(caster, pack, now);
    }

    private static final class Golem implements Instance {
        final Ref<EntityStore> caster;
        final SummonPack pack;
        final long startAt;
        long lastTaunt, lastDrive;

        Golem(Ref<EntityStore> caster, SummonPack pack, long startAt) { this.caster = caster; this.pack = pack; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            Vector3d home = SpellFx.position(caster, store);
            if (home == null) return true;
            if (now - lastDrive >= DRIVE_EVERY_MS) {
                lastDrive = now;
                if (pack.tick(home, FIGHT_RANGE, now, store, cb) == 0) return true;
            }
            if (now - lastTaunt >= TAUNT_EVERY_MS) { lastTaunt = now; taunt(store); }
            return false;
        }

        /** Les monstres proches du golem le prennent pour cible. */
        private void taunt(Store<EntityStore> store) {
            for (SummonPack.Member m : pack.members) {
                Ref<EntityStore> golem = m.ref();
                if (m.gone || golem == null || !golem.isValid()) continue;
                for (Ref<EntityStore> e : pack.monstersAround(m.lastPos, TAUNT_RADIUS, store)) {
                    CloneFx.hostile(e, golem, TAUNT_S, store);
                    CloneFx.mark(e, "LockedTarget", golem, store);
                }
            }
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { pack.cleanup(cb); }

        @Override public void abort() { pack.abort(); }
    }
}
