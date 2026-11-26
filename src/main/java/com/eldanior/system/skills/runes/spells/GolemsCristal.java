package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune des Golems de Cristal (Terre, Epic, rune de predilection du Demiurge) : trois golems de cristal (PNJ
 * Eldanior_Summon_Golem_Feu / _Givre / _Foudre, 250 PV) pendant 15 s. Toutes les 1,5 s, chacun ajoute son element a sa
 * cible (a 3,5 blocs) : 25 + brulure / givre / foudroiement 0,3 s. Un golem qui tombe, ou tous a la fin, eclatent :
 * 120 de leur element a 3 blocs.
 * Fiche de conception : scripts/runes/specs/Rune_GolemsCristal.json
 */
public final class GolemsCristal implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, DRIVE_EVERY_MS = 500, ELEMENT_EVERY_MS = 1_500, STUN_MS = 300;
    private static final double SPAWN_AHEAD = 3.0, SPREAD = 2.5, FIGHT_RANGE = 15.0, TOUCH = 3.5, BLAST = 3.0;
    private static final float MAX_HEALTH = 250f, TOUCH_DAMAGE = 25f, BLAST_DAMAGE = 120f;

    // ===== Assets =====
    /** Par golem : role, cause de degats, effet, particule d'impact. */
    private static final String[][] ELEMENTS = {
            {"Eldanior_Summon_Golem_Feu", "Fire", "Elda_Brulure", "Impact_Fire"},
            {"Eldanior_Summon_Golem_Givre", "Ice", "Elda_Givre", "Ability_Ice_Impact"},
            {"Eldanior_Summon_Golem_Foudre", "Lightning", "Taranis_Foudroye", "Taranis_Bolt"}};
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_BLAST = "Explosion_Small";
    private static final String SFX_WAKE = "SFX_Golem_Earth_Wake", SFX_SHATTER = "SFX_Crystal_Break";

    @Override public String id() { return "GolemsCristal"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        Vector3d fwd = SpellFx.forward(caster, store);
        Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
        long now = System.currentTimeMillis();
        SummonPack pack = new SummonPack(caster, FX_DUST);
        for (int i = 0; i < ELEMENTS.length; i++) {                               // gauche, devant, droite
            Vector3d at = SpellFx.ground(world, new Vector3d(pos).add(new Vector3d(fwd).mul(SPAWN_AHEAD)).add(new Vector3d(side).mul((i - 1) * SPREAD)));
            pack.spawn(world, ELEMENTS[i][0], at, SpellFx.facing(fwd, false), MAX_HEALTH, null, now);
            SpellFx.fx(FX_DUST, new Vector3d(at).add(0, 0.5, 0), 1.2f, store, cb);
        }
        SpellFx.sound(SFX_WAKE, pos, cb);
        SpellFx.shake(pos, 12, "Impact_Light", store);
        return new Trio(caster, pack, now);
    }

    private static final class Trio implements Instance {
        final Ref<EntityStore> caster;
        final SummonPack pack;
        final long startAt;
        final boolean[] shattered = new boolean[ELEMENTS.length];
        long lastDrive, lastElement;

        Trio(Ref<EntityStore> caster, SummonPack pack, long startAt) { this.caster = caster; this.pack = pack; this.startAt = startAt; this.lastElement = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d home = SpellFx.position(caster, store);
            if (home == null) return true;
            if (now - startAt >= DURATION_MS) {                                   // fin : les golems restants eclatent
                for (SummonPack.Member m : pack.members) if (!m.gone && m.ref() != null) shatter(m, store, cb);
                return true;
            }
            if (now - lastDrive >= DRIVE_EVERY_MS) {
                lastDrive = now;
                int alive = pack.tick(home, FIGHT_RANGE, now, store, cb);
                for (SummonPack.Member m : pack.members) if (m.gone && m.ref() != null) shatter(m, store, cb);   // tombe au combat
                if (alive == 0) return true;
            }
            if (now - lastElement >= ELEMENT_EVERY_MS) { lastElement = now; imbue(store, cb); }
            return false;
        }

        /** Chaque golem ajoute son element a sa cible. */
        private void imbue(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (SummonPack.Member m : pack.members) {
                if (m.gone || m.target == null || m.slot >= ELEMENTS.length) continue;
                Vector3d p = SpellFx.position(m.target, store);
                if (p == null || p.distance(m.lastPos) > TOUCH) continue;
                String[] el = ELEMENTS[m.slot];
                SpellFx.damage(caster, m.target, el[1], TOUCH_DAMAGE, null, null, cb);
                if (el[1].equals("Lightning")) SpellFx.stun(m.target, el[2], STUN_MS, store, cb); else SpellFx.effect(m.target, el[2], cb);
                SpellFx.fx(el[3], new Vector3d(p).add(0, 1, 0), 0.8f, store, cb);
            }
        }

        /** Le golem vole en eclats : degats de son element autour de lui. */
        private void shatter(SummonPack.Member m, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (m.slot >= ELEMENTS.length || shattered[m.slot]) return;
            shattered[m.slot] = true;
            String[] el = ELEMENTS[m.slot];
            Vector3d at = new Vector3d(m.lastPos);
            SpellFx.fx(FX_BLAST, new Vector3d(at).add(0, 1, 0), 1.0f, store, cb);
            SpellFx.fx(el[3], new Vector3d(at).add(0, 1, 0), 1.5f, store, cb);
            SpellFx.sound(SFX_SHATTER, at, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, BLAST, store)) {
                if (RuneSpellRuntime.isSummon(e)) continue;
                SpellFx.damage(caster, e, el[1], BLAST_DAMAGE, null, "Impact_Light", cb);
                if (!el[1].equals("Lightning")) SpellFx.effect(e, el[2], cb);
            }
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { pack.cleanup(cb); }

        @Override public void abort() { pack.abort(); }
    }
}
