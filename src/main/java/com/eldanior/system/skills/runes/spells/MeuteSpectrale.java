package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Meute Spectrale (Tenebres, Rare) : trois loups spectraux (PNJ Eldanior_Summon_Loup, 150 PV, teinte
 * Elda_Spectral) pendant 15 s. Ils chassent les monstres a moins de 15 blocs du lanceur, chacun le sien si possible,
 * puis reviennent a ses pieds.
 * Fiche de conception : scripts/runes/specs/Rune_MeuteSpectrale.json
 */
public final class MeuteSpectrale implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, DRIVE_EVERY_MS = 500;
    private static final int WOLVES = 3;
    private static final double SPAWN_RADIUS = 2.5, HUNT_RANGE = 15.0;
    private static final float MAX_HEALTH = 150f;

    // ===== Assets =====
    private static final String ROLE = "Eldanior_Summon_Loup", SPECTRAL = "Elda_Spectral";
    private static final String FX_SMOKE = "Nyx_Void";
    private static final String SFX_HOWL = "SFX_Wolf_Alerted";

    @Override public String id() { return "MeuteSpectrale"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        World world = SpellFx.world(store);
        Vector3d fwd = SpellFx.forward(caster, store);
        long now = System.currentTimeMillis();
        SummonPack pack = new SummonPack(caster, FX_SMOKE);
        double base = Math.atan2(fwd.z, fwd.x);
        for (int i = 0; i < WOLVES; i++) {
            double a = base + (i - 1) * 0.9;                                            // en eventail devant le lanceur
            Vector3d at = SpellFx.ground(world, new Vector3d(pos).add(Math.cos(a) * SPAWN_RADIUS, 0, Math.sin(a) * SPAWN_RADIUS));
            pack.spawn(world, ROLE, at, SpellFx.facing(fwd, false), MAX_HEALTH, SPECTRAL, now);
            SpellFx.fx(FX_SMOKE, new Vector3d(at).add(0, 0.8, 0), 0.6f, store, cb);
        }
        SpellFx.sound(SFX_HOWL, pos, cb);
        return new Pack(caster, pack, now);
    }

    private static final class Pack implements Instance {
        final Ref<EntityStore> caster;
        final SummonPack pack;
        final long startAt;
        long lastDrive;

        Pack(Ref<EntityStore> caster, SummonPack pack, long startAt) { this.caster = caster; this.pack = pack; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            Vector3d home = SpellFx.position(caster, store);
            if (home == null) return true;
            if (now - lastDrive < DRIVE_EVERY_MS) return false;
            lastDrive = now;
            return pack.tick(home, HUNT_RANGE, now, store, cb) == 0;
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { pack.cleanup(cb); }

        @Override public void abort() { pack.abort(); }
    }
}
