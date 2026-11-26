package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.List;

/**
 * Rune de l'Esprit de la Foret (Terre, Epic, rune de predilection du Gardien de la Nature) : invoque un esprit-arbre
 * (PNJ Eldanior_Summon_Esprit_Foret, modele Hedera recolore, 800 PV) pendant 25 s. Il frappe le monstre le plus proche ;
 * toutes les 3 s, des racines jaillissent sous les 2 ennemis les plus proches du lanceur (12 blocs) : 80 degats,
 * Elda_Racines + lockInPlace 1 s. Sans ennemi, il suit le lanceur ; vaincu, le sort prend fin.
 * Fiche de conception : scripts/runes/specs/Rune_EspritForet.json
 */
public final class EspritForet implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 25_000, ROOT_EVERY_MS = 3_000, ROOT_MS = 1_000, DRIVE_EVERY_MS = 500;
    private static final double SPAWN_AHEAD = 3.0, FIGHT_RANGE = 15.0, GUARD_RADIUS = 12.0;
    private static final int ROOTED_PER_PULSE = 2;
    private static final float MAX_HEALTH = 800f, ROOT_DAMAGE = 80f;

    // ===== Assets =====
    private static final String ROLE = "Eldanior_Summon_Esprit_Foret", ROOTS = "Elda_Racines";
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_LEAVES = "Block_Break_Leaves", FX_DIRT = "Block_Break_Dirt";
    private static final String SFX_WAKE = "SFX_Spirit_Root_Spawn", SFX_CREAK = "SFX_Emit_Tree_Creak", SFX_ROOT = "SFX_Bramble_Break";

    @Override public String id() { return "EspritForet"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        Vector3d fwd = SpellFx.forward(caster, store);
        Vector3d at = SpellFx.ground(world, new Vector3d(pos).add(new Vector3d(fwd).mul(SPAWN_AHEAD)));
        long now = System.currentTimeMillis();
        SummonPack pack = new SummonPack(caster, FX_LEAVES);
        pack.spawn(world, ROLE, at, SpellFx.facing(fwd, false), MAX_HEALTH, null, now);
        SpellFx.fx(FX_DUST, new Vector3d(at).add(0, 0.5, 0), 1.4f, store, cb);
        SpellFx.fx(FX_LEAVES, new Vector3d(at).add(0, 2, 0), 2.0f, store, cb);
        SpellFx.sound(SFX_WAKE, at, cb);
        SpellFx.sound(SFX_CREAK, at, cb);
        SpellFx.shake(at, 12, "Impact_Light", store);
        return new Guardian(caster, pack, now);
    }

    private static final class Guardian implements Instance {
        final Ref<EntityStore> caster;
        final SummonPack pack;
        final long startAt;
        long lastRoot, lastDrive;

        Guardian(Ref<EntityStore> caster, SummonPack pack, long startAt) {
            this.caster = caster; this.pack = pack; this.startAt = startAt; this.lastRoot = startAt - ROOT_EVERY_MS / 2;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            Vector3d home = SpellFx.position(caster, store);
            if (home == null) return true;
            if (now - lastDrive >= DRIVE_EVERY_MS) {
                lastDrive = now;
                if (pack.tick(home, FIGHT_RANGE, now, store, cb) == 0) return true;
            }
            if (now - lastRoot >= ROOT_EVERY_MS && spiritPresent()) { lastRoot = now; rootAssailants(home, store, cb); }
            return false;
        }

        private boolean spiritPresent() {
            for (SummonPack.Member m : pack.members) {
                Ref<EntityStore> ref = m.ref();
                if (!m.gone && ref != null && ref.isValid()) return true;
            }
            return false;
        }

        /** Les racines du gardien saisissent les ennemis les plus proches du lanceur. */
        private void rootAssailants(Vector3d home, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<Ref<EntityStore>> near = SpellFx.enemiesAround(caster, home, GUARD_RADIUS, store);
            near.removeIf(RuneSpellRuntime::isSummon);
            near.sort((a, b) -> Double.compare(dist(a, home, store), dist(b, home, store)));
            int n = 0;
            for (Ref<EntityStore> e : near) {
                if (n >= ROOTED_PER_PULSE) break;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                n++;
                SpellFx.lockInPlace(e, ROOT_MS, store);
                try { EffectsManager.applyCustomEffect(e, ROOTS, ROOT_MS / 1000f, OverlapBehavior.OVERWRITE, cb); }
                catch (Exception ex) { EldaniorLogger.error("EspritForet: effet", ex); }
                SpellFx.damage(caster, e, "Earth", ROOT_DAMAGE, null, "Impact_Light", cb);
                SpellFx.fx(FX_DIRT, p, 1.2f, store, cb);
                SpellFx.fx(FX_DUST, p, 0.8f, store, cb);
                SpellFx.sound(SFX_ROOT, p, cb);
            }
        }

        private static double dist(Ref<EntityStore> e, Vector3d from, Store<EntityStore> store) {
            Vector3d p = SpellFx.position(e, store);
            return p == null ? Double.MAX_VALUE : p.distance(from);
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { pack.cleanup(cb); }

        @Override public void abort() { pack.abort(); }
    }
}
