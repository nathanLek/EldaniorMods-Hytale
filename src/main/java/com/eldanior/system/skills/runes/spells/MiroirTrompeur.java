package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune du Miroir Trompeur (Lumiere, Rare) : deux reflets du lanceur (copies PNJ, 60 PV) s'enfuient a gauche et a droite
 * pendant 6 s et attirent les monstres proches ; le lanceur devient invisible 3 s.
 * Fiche de conception : scripts/runes/specs/Rune_MiroirTrompeur.json
 */
public final class MiroirTrompeur implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, INVISIBLE_MS = 3_000, LURE_EVERY_MS = 500;
    private static final double SPAWN_SIDE = 1.8, RUN_SIDE = 9.0, RUN_AHEAD = 3.0, LURE_RANGE = 15.0, LURE_S = 2.0;
    private static final float DECOY_HEALTH = 60f;

    // ===== Assets =====
    private static final String ROLE = "Eldanior_Clone_Lumiere_Melee", INVISIBLE = "Elda_InvisTotal";
    private static final String FX_MIRROR = "Bifrost_Divine";
    private static final String SFX_MIRROR = "SFX_Portal_Neutral_Open";

    @Override public String id() { return "MiroirTrompeur"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        long now = System.currentTimeMillis();
        Vector3d fwd = SpellFx.forward(caster, store);
        Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
        if (side.lengthSquared() < 1e-6) side.set(1, 0, 0);
        side.normalize();
        SummonPack pack = new SummonPack(caster, FX_MIRROR);
        CloneFx.Look look = CloneFx.Look.of(caster, store);
        List<Vector3d> runTo = new ArrayList<>();
        for (int sign = -1; sign <= 1; sign += 2) {
            Vector3d at = SpellFx.ground(world, new Vector3d(pos).add(new Vector3d(side).mul(sign * SPAWN_SIDE)));
            pack.spawn(world, ROLE, at, SpellFx.facing(new Vector3d(side).mul(sign), false), look, CloneFx.heldItem(caster, store), DECOY_HEALTH, null, now);
            runTo.add(new Vector3d(pos).add(new Vector3d(side).mul(sign * RUN_SIDE)).add(new Vector3d(fwd).mul(RUN_AHEAD)));
            SpellFx.fx(FX_MIRROR, new Vector3d(at).add(0, 1, 0), 0.8f, store, cb);
        }
        EffectsManager.applyCustomEffect(caster, INVISIBLE, INVISIBLE_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
        SpellFx.sound(SFX_MIRROR, pos, cb);
        return new Decoys(caster, pack, runTo, now);
    }

    private static final class Decoys implements Instance {
        final Ref<EntityStore> caster;
        final SummonPack pack;
        final List<Vector3d> runTo;
        final long startAt;
        long lastLure;

        Decoys(Ref<EntityStore> caster, SummonPack pack, List<Vector3d> runTo, long startAt) {
            this.caster = caster; this.pack = pack; this.runTo = runTo; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastLure < LURE_EVERY_MS) return false;
            lastLure = now;
            List<Ref<EntityStore>> decoys = new ArrayList<>();
            for (SummonPack.Member m : pack.members) {
                if (m.gone) continue;
                Ref<EntityStore> ref = m.ref();
                if (ref == null) continue;                                              // pas encore cree
                if (!ref.isValid() || RuneSpellRuntime.isDying(ref)) { pack.dissipate(m, store, cb); continue; }
                Vector3d p = SpellFx.position(ref, store);
                if (p != null) m.lastPos = p;
                CloneFx.leash(ref, runTo.get(m.slot), store);                           // il court vers son point de fuite
                decoys.add(ref);
            }
            if (decoys.isEmpty()) return pack.members.stream().allMatch(m -> m.gone);
            Vector3d home = SpellFx.position(caster, store);
            if (home == null) return true;
            int i = 0;
            for (Ref<EntityStore> e : pack.monstersAround(home, LURE_RANGE, store)) {
                Ref<EntityStore> decoy = decoys.get(i++ % decoys.size());
                CloneFx.hostile(e, decoy, LURE_S, store);
                CloneFx.mark(e, "LockedTarget", decoy, store);
            }
            return false;
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { pack.cleanup(cb); }

        @Override public void abort() { pack.abort(); }
    }
}
