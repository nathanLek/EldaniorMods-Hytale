package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Rune de la Releve des Morts (Tenebres, Rare) : pendant 15 s, chaque monstre qui meurt a moins de 20 blocs du lanceur
 * se releve en squelette allie (PNJ Eldanior_Summon_Squelette, 120 PV) pour 15 s ; 4 squelettes au plus. La mort est
 * reperee par le composant DeathComponent des monstres surveilles.
 * Fiche de conception : scripts/runes/specs/Rune_ReleveDesMorts.json
 */
public final class ReleveDesMorts implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long WINDOW_MS = 15_000, SKELETON_MS = 15_000, SCAN_EVERY_MS = 250, DRIVE_EVERY_MS = 500;
    private static final double WATCH_RANGE = 20.0, FIGHT_RANGE = 15.0;
    private static final int MAX_SKELETONS = 4;
    private static final float MAX_HEALTH = 120f;

    // ===== Assets =====
    private static final String ROLE = "Eldanior_Summon_Squelette";
    private static final String FX_RISE = "Nyx_Chain_Burst", FX_MIST = "Nyx_Mist", FX_GONE = "Nyx_Void";
    private static final String SFX_RISE = "SFX_Skeleton_Alerted";

    @Override public String id() { return "ReleveDesMorts"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_MIST, pos, 1.0f, store, cb);
        SpellFx.sound(SFX_RISE, pos, cb);
        return new Necromancy(caster, SpellFx.world(store), System.currentTimeMillis());
    }

    private static final class Necromancy implements Instance {
        final Ref<EntityStore> caster;
        final World world;
        final long startAt;
        final SummonPack pack;
        final Map<Ref<EntityStore>, Vector3d> watched = new HashMap<>();   // monstres proches et leur derniere position
        final Set<Ref<EntityStore>> raised = new HashSet<>();
        long lastScan, lastDrive;

        Necromancy(Ref<EntityStore> caster, World world, long startAt) {
            this.caster = caster; this.world = world; this.startAt = startAt;
            this.pack = new SummonPack(caster, FX_GONE);
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d home = SpellFx.position(caster, store);
            if (home == null) return true;
            boolean open = now - startAt < WINDOW_MS;
            if (open && now - lastScan >= SCAN_EVERY_MS) { lastScan = now; watch(home, now, store, cb); }
            for (SummonPack.Member m : pack.members)                                   // chaque squelette a sa propre duree
                if (!m.gone && now - m.npc.born >= SKELETON_MS) pack.dissipate(m, store, cb);
            int alive = 0;
            if (now - lastDrive >= DRIVE_EVERY_MS) { lastDrive = now; alive = pack.tick(home, FIGHT_RANGE, now, store, cb); }
            else for (SummonPack.Member m : pack.members) if (!m.gone) alive++;
            return !open && alive == 0;
        }

        /** Releve les monstres surveilles qui viennent de mourir, puis met a jour la liste des monstres proches. */
        private void watch(Vector3d home, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            watched.entrySet().removeIf(en -> {
                Ref<EntityStore> e = en.getKey();
                if (!e.isValid()) return true;
                if (store.getComponent(e, DeathComponent.getComponentType()) == null) return false;
                if (raised.size() < MAX_SKELETONS && raised.add(e)) rise(en.getValue(), now, store, cb);
                return true;
            });
            for (Ref<EntityStore> e : pack.monstersAround(home, WATCH_RANGE, store)) {
                if (raised.contains(e) || store.getComponent(e, DeathComponent.getComponentType()) != null) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p != null) watched.put(e, p);
            }
        }

        private void rise(Vector3d at, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            pack.spawn(world, ROLE, at, SpellFx.facing(new Vector3d(0, 0, 1), false), MAX_HEALTH, null, now);
            SpellFx.fx(FX_RISE, at, 1.0f, store, cb);
            SpellFx.sound(SFX_RISE, at, cb);
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { pack.cleanup(cb); }

        @Override public void abort() { pack.abort(); }
    }
}
