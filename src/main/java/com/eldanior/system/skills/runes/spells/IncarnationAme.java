package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune de l'Incarnation de l'Ame (Luo, Legendaire, Maitre Supreme, voie martiale, 2026-11-10) : l'ame du lanceur se detache et
 * combat a ses cotes. 1 double spirituel (2 au Dieu Martial) pendant 25 s (30 s au Dieu Martial) : vrai PNJ copie du lanceur
 * (modele, skin, nom, arme de corps a corps ; roles des clones de la Rune de Clonage), corps de Ki blanc nacre (effet
 * Incarnation_Ame rafraichi : translucide, ModelVFX Ame_Spectrale), 60 % de la vie maximale du lanceur. Contrairement aux clones, il est autonome des le depart : il
 * combat seul les monstres a AUTO_RANGE blocs de lui et revient pres du lanceur s'il s'en eloigne de plus de LEASH blocs.
 * Un coup fatal dissipe le double (aucun butin). Pas de tir ami (invocation du lanceur).
 * Fiche de conception : scripts/runes/specs/Rune_IncarnationAme.json
 */
public final class IncarnationAme implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final float HP_RATIO = 0.6f;
    private static final double AUTO_RANGE = 15.0, LEASH = 30.0, SIDE = 1.8, HOSTILE_S = 3.0;
    private static final long DURATION_MS = 25_000, DIVINE_DURATION_MS = 30_000, SPAWN_TIMEOUT_MS = 3_000, DETECT_MS = 1_000,
            SIGNATURE_MS = 8_000;
    private static final int GOD_TIER = 8;

    // ===== Assets =====
    private static final String ROLE_SWORD = "Eldanior_Clone_Lumiere", ROLE_MELEE = "Eldanior_Clone_Lumiere_Melee";
    /** Corps translucide (ModelVFX Ame_Spectrale : Opacity 0,45, lueur blanc nacre ; gen_voie_martiale.py), retour du 2026-11-10. */
    private static final String BODY = "Incarnation_Ame";
    private static final String FX_APPEAR = "Gilgamesh_Sword_Burst", FX_RING = "Eldanior_Pas_Celeste";
    private static final String SFX_APPEAR = "SFX_Portal_Neutral_Open";

    @Override public String id() { return "IncarnationAme"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        int tier = MartialKi.tier(caster, store);
        long now = System.currentTimeMillis();
        Soul s = new Soul(caster, world, now, tier >= GOD_TIER ? DIVINE_DURATION_MS : DURATION_MS);
        CloneFx.Look look = CloneFx.Look.of(caster, store);
        ItemStack weapon = CloneFx.meleeWeapon(caster, store);
        float hp = CloneFx.maxHealth(caster, store) * HP_RATIO;
        boolean sword = weapon != null && weapon.getItemId().startsWith("Weapon_Sword");
        s.sword = sword;
        Vector3d fwd = SpellFx.forward(caster, store);
        int count = tier >= GOD_TIER ? 2 : 1;
        SpellFx.fx("Ki_Colonne_T" + Math.max(7, tier), new Vector3d(pos).add(0, 0.1, 0), 1.3f, store, cb);   // l'ame quitte le corps
        SpellFx.fx("Ki_Anneau_T" + Math.max(7, tier), new Vector3d(pos).add(0, 0.05, 0), 2.2f, store, cb);
        for (int i = 0; i < count; i++) {
            Vector3d at = s.slot(pos, fwd, i);
            for (int k = 1; k <= 6; k++)                                         // trainee d'ame du lanceur vers son double
                SpellFx.fx("Ki_Orbe_T7", new Vector3d(pos).lerp(at, k / 6.0).add(0, 1.2 + Math.sin(k / 6.0 * Math.PI) * 0.8, 0), 0.7f, store, cb);
            s.doubles.add(new Spirit(CloneFx.spawn(world, sword ? ROLE_SWORD : ROLE_MELEE, caster, at, SpellFx.facing(fwd, false),
                    look, weapon, hp, BODY, false, now), i, at));
            SpellFx.fx(FX_APPEAR, new Vector3d(at).add(0, 1, 0), 1.2f, store, cb);
        }
        SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), 1.8f, store, cb);
        SpellFx.sound(SFX_APPEAR, pos, cb);
        EldaniorLogger.info("[IncarnationAme] " + count + " double(s) (" + (int) hp + " PV)");
        return s;
    }

    private static final class Spirit {
        final CloneFx.Npc npc;
        final int slot;
        Vector3d lastPos;
        Ref<EntityStore> target;
        boolean ready, gone;
        Spirit(CloneFx.Npc npc, int slot, Vector3d pos) { this.npc = npc; this.slot = slot; this.lastPos = pos; }
    }

    private static final class Soul implements Instance {
        final Ref<EntityStore> caster;
        final World world;
        final long startAt, duration;
        final List<Spirit> doubles = new ArrayList<>();
        boolean sword;
        long lastDetect, lastSignature;

        Soul(Ref<EntityStore> caster, World world, long startAt, long duration) {
            this.caster = caster; this.world = world; this.startAt = startAt; this.duration = duration; this.lastSignature = startAt;
        }

        /** Place du double i : a gauche puis a droite du lanceur. */
        Vector3d slot(Vector3d casterPos, Vector3d fwd, int i) {
            Vector3d side = new Vector3d(fwd.z, 0, -fwd.x).mul(i == 0 ? SIDE : -SIDE);
            return SpellFx.ground(world, new Vector3d(casterPos).add(side));
        }

        private Ref<EntityStore> nearestMonster(Vector3d at, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestD = Double.MAX_VALUE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, AUTO_RANGE, store)) {
                if (RuneSpellRuntime.isSummon(e) || !CloneFx.isMonster(e, store)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = p.distanceSquared(at);
                if (d < bestD) { bestD = d; best = e; }
            }
            return best;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= duration) return true;
            boolean detect = now - lastDetect >= DETECT_MS, signature = now - lastSignature >= SIGNATURE_MS;
            if (detect) lastDetect = now;
            if (signature) lastSignature = now;
            Vector3d fwd = SpellFx.forward(caster, store);
            int alive = 0;
            for (Spirit d : doubles) {
                if (d.gone) continue;
                Ref<EntityStore> ref = d.npc.ref;
                if (ref == null) {
                    if (d.npc.failed || now - d.npc.born > SPAWN_TIMEOUT_MS) d.gone = true; else alive++;
                    continue;
                }
                if (!ref.isValid() || RuneSpellRuntime.isDying(ref)) { dissipate(d, store, cb); continue; }
                alive++;
                Vector3d cp = SpellFx.position(ref, store);
                if (cp != null) d.lastPos = cp;
                d.ready = true;
                CloneFx.leash(ref, slot(pos, fwd, d.slot), store);
                if (signature) CloneFx.fillStat(ref, DefaultEntityStatTypes.getSignatureEnergy(), store);
                if (!detect) continue;
                SpellFx.effect(ref, BODY, cb);                                  // corps de Ki
                if (d.lastPos.distance(pos) > LEASH) {                          // trop loin : il revient
                    d.target = null;
                    CloneFx.mark(ref, "LockedTarget", null, store);
                    CloneFx.setState(ref, "Idle", null, store);
                    continue;
                }
                if (d.target == null || !d.target.isValid() || SpellFx.position(d.target, store) == null) {
                    d.target = nearestMonster(d.lastPos, store);
                    if (d.target != null) {
                        CloneFx.mark(ref, "LockedTarget", d.target, store);
                        CloneFx.setState(ref, "Combat", sword ? "Default" : null, store);
                    }
                }
                if (d.target != null) { CloneFx.hostile(ref, d.target, HOSTILE_S, store); CloneFx.hostile(d.target, ref, HOSTILE_S, store); }
            }
            return alive == 0;
        }

        private void dissipate(Spirit d, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            d.gone = true;
            SpellFx.fx(FX_APPEAR, new Vector3d(d.lastPos).add(0, 1, 0), 1.0f, store, cb);
            SpellFx.remove(d.npc.ref, cb);
            RuneSpellRuntime.forgetSummon(d.npc.ref);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Spirit d : doubles) {
                if (d.gone) continue;
                d.gone = true;
                Ref<EntityStore> ref = d.npc.ref;
                if (ref != null && ref.isValid()) SpellFx.fx(FX_APPEAR, new Vector3d(d.lastPos).add(0, 1, 0), 1.0f, ref.getStore(), cb);
                SpellFx.remove(ref, cb);
                RuneSpellRuntime.forgetSummon(ref);
            }
        }

        @Override
        public void abort() {
            for (Spirit d : doubles) {
                RuneSpellRuntime.removeLater(d.npc.ref);
                RuneSpellRuntime.forgetSummon(d.npc.ref);
            }
        }
    }
}
