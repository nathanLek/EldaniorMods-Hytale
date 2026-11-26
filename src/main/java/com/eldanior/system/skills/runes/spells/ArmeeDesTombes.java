package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de l'Armee des Tombes (Tenebres, Unique) : huit squelettes (PNJ Eldanior_Summon_Squelette, 150 PV) surgissent en
 * cercle autour du lanceur et combattent 20 s en se repartissant les monstres proches.
 * Fiche de conception : scripts/runes/specs/Rune_ArmeeDesTombes.json
 */
public final class ArmeeDesTombes implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 20_000, DRIVE_EVERY_MS = 500;
    private static final int SKELETONS = 8;
    private static final double RING = 3.5, FIGHT_RANGE = 18.0;
    private static final float MAX_HEALTH = 150f;

    // ===== Assets =====
    private static final String ROLE = "Eldanior_Summon_Squelette";
    private static final String FX_GRAVE = "Nyx_Chain_Burst", FX_DUST = "Gaia_Dust_Burst", FX_GONE = "Nyx_Void";
    private static final String SFX_RISE = "SFX_Skeleton_Alerted";

    @Override public String id() { return "ArmeeDesTombes"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        World world = SpellFx.world(store);
        long now = System.currentTimeMillis();
        SummonPack pack = new SummonPack(caster, FX_GONE);
        for (int i = 0; i < SKELETONS; i++) {
            double a = Math.PI * 2 * i / SKELETONS;
            Vector3d out = new Vector3d(Math.cos(a), 0, Math.sin(a));
            Vector3d at = SpellFx.ground(world, new Vector3d(pos).add(new Vector3d(out).mul(RING)));
            pack.spawn(world, ROLE, at, SpellFx.facing(out, false), MAX_HEALTH, null, now);
            SpellFx.fx(FX_GRAVE, at, 0.9f, store, cb);
            SpellFx.fx(FX_DUST, new Vector3d(at).add(0, 0.4, 0), 0.7f, store, cb);
        }
        SpellFx.sound(SFX_RISE, pos, cb);
        SpellFx.shake(pos, 15, "Impact_Strong", store);
        return new Army(caster, pack, now);
    }

    private static final class Army implements Instance {
        final Ref<EntityStore> caster;
        final SummonPack pack;
        final long startAt;
        long lastDrive;

        Army(Ref<EntityStore> caster, SummonPack pack, long startAt) { this.caster = caster; this.pack = pack; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            Vector3d home = SpellFx.position(caster, store);
            if (home == null) return true;
            if (now - lastDrive < DRIVE_EVERY_MS) return false;
            lastDrive = now;
            return pack.tick(home, FIGHT_RANGE, now, store, cb) == 0;
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { pack.cleanup(cb); }

        @Override public void abort() { pack.abort(); }
    }
}
