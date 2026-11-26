package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Signature du Bokken du Dojo (2026-11-11, demande de l'utilisateur : « autant de versions ameliorees que de classes ;
 * Apprenti : coup de katana charge de haut en bas, slash vertical avec animation, a ameliorer pour chaque classe »).
 * Fente du Ciel (karatake-wari) : posture chargee (IaiCharging), puis taille verticale. Lancee par Ability1 a energie de
 * signature pleine (Luo_Signature, LuoIaigiri renvoie ici quand le katana tenu est le Bokken). Le Bokken ne tue pas
 * (LuoPassives.trainingCap) : les coups laissent au moins 1 PV.
 * 2026-11-12 : retiree du Bokken lui-meme (maintien = Percee, competence = rafale) ; reste la signature des autres katanas.
 *
 *   1 Apprenti      Fente Verticale        charge 0,7 s, pas en avant, taille verticale (arc de 3,5 blocs), projette au sol
 *   2 Disciple      Fente Fracassante      + onde au sol en ligne (6 blocs), poussiere
 *   3 Expert        Fente du Pas Leger     ruee de 6 blocs avant la taille, onde de 8 blocs
 *   4 Maitre        Fente de Ki            + lame de Ki verticale (16 blocs)
 *   5 Grand Maitre  Fente Plongeante       bond et salto, taille en retombant : impact en cercle (4 blocs) + lame
 *   6 Saint         Fente des Trois Cieux  + trois lames verticales en eventail, colonne de Ki doree a l'impact
 *   7 Maitre Supreme Fente de l'Ame        lame geante, le sol se fend (16 blocs), l'ame repasse la taille 0,4 s apres
 *   8 Dieu Martial  Fente du Ciel et de la Terre : une lame geante tombe du ciel sur le point vise (rayon 6), sol fendu
 *                                          sur 24 blocs, tout est projete
 */
public final class BokkenSignature {

    private BokkenSignature() {}

    static final String[] NAMES = {"Fente Verticale", "Fente Fracassante", "Fente du Pas Léger", "Fente de Ki", "Fente Plongeante",
            "Fente des Trois Cieux", "Fente de l'Âme", "Fente du Ciel et de la Terre"};
    /** Degats de la taille par palier, charge en ms, longueur de l'onde au sol (0 : aucune). */
    static final float[] DAMAGE = {30, 40, 50, 60, 75, 90, 110, 140};
    static final long[] CHARGE_MS = {700, 650, 600, 600, 550, 550, 500, 500};
    static final double[] WAVE = {0, 6, 8, 8, 10, 12, 16, 24};
    static final double REACH = 3.5, HALF_ANGLE = Math.toRadians(50);
    private static final String SET = "Tenryumaru_Degainee";

    /** Palier de la voie martiale du porteur (1 hors de la voie). */
    static int tier(Ref<EntityStore> ref, Store<EntityStore> store) {
        Integer npc = MartialKi.NPC_TIER.get(ref);
        if (npc != null) return Math.clamp(npc, 1, 8);
        return Math.max(1, VoieMartiale.tier(MartialKi.data(ref, store)));
    }

    public static RuneSpell.Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        int t = tier(caster, store);
        long now = System.currentTimeMillis();
        AnimationUtils.playAnimation(caster, AnimationSlot.Action, SET, "IaiCharging", true, store);
        SpellFx.fx("Ki_Anneau_T" + t, new Vector3d(pos).add(0, 0.05, 0), 1.2f + t * 0.1f, store, cb);
        SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", pos, cb);
        SpellFx.protectFromFall(caster, 4_000, store);
        MartialKi.notify(caster, store, "<color:gold>" + NAMES[t - 1] + "</color> <color:gray>— " + VoieMartiale.className(t) + "</color>",
                NotificationStyle.Default);
        return new Cut(caster, t, now, Percee.power(Percee.held(caster, store)));
    }

    static final class Cut implements RuneSpell.Instance {
        final Ref<EntityStore> caster;
        final int tier;
        final long startAt;
        int phase;                    // 0 charge, 1 (pour 5+) bond, 2 taille faite, 3 echo de l'ame fait
        long nextAt;
        Vector3d strikeDir;
        final List<RuneSpell.Instance> blades = new ArrayList<>();
        /** Puissance du katana tenu (2026-11-11 : la Fente du Ciel passe a tous les katanas ; Bokken x1). */
        final float power;

        Cut(Ref<EntityStore> caster, int tier, long now, float power) {
            this.caster = caster; this.tier = tier; this.startAt = now; this.nextAt = now + CHARGE_MS[tier - 1]; this.power = power;
        }

        float base() { return DAMAGE[tier - 1] * power; }

        @Override
        public boolean tick(Ref<EntityStore> c, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            for (var it = blades.iterator(); it.hasNext(); ) {
                RuneSpell.Instance bl = it.next();
                if (bl.tick(c, now, s, b)) { bl.cleanup(b); it.remove(); }
            }
            if (now < nextAt) return false;
            Vector3d pos = SpellFx.position(c, s);
            if (pos == null) return true;
            switch (phase) {
                case 0 -> {
                    strikeDir = flat(SpellFx.forward(c, s));
                    if (tier >= 5) {                                              // bond : salto avant, la taille tombe en retombant
                        AnimationUtils.playAnimation(c, AnimationSlot.Action, SET, "LuoSalto", true, s);
                        SpellFx.push(c, new Vector3d(strikeDir).mul(10).add(0, 9, 0), false, b);
                        SpellFx.fx("Eldanior_Pas_Celeste", new Vector3d(pos).add(0, 0.1, 0), 1.0f, s, b);
                        SpellFx.sound("SFX_Light_Melee_T2_Lunge", pos, b);
                        phase = 1;
                        nextAt = now + 450;
                        return false;
                    }
                    if (tier >= 3) {                                              // pas leger : ruee avant la taille
                        SpellFx.push(c, new Vector3d(strikeDir).mul(22).add(0, 0.6, 0), false, b);
                        SpellFx.fx("Gaia_Dust_Burst", pos, 0.6f, s, b);
                        phase = 1;
                        nextAt = now + 220;
                        return false;
                    }
                    strike(c, pos, s, b);
                    return phase >= 2 && blades.isEmpty();
                }
                case 1 -> {
                    strike(c, pos, s, b);
                    return false;
                }
                case 2 -> {
                    if (tier >= 7) {                                              // l'ame repasse la taille
                        SpellFx.fx("Ki_Colonne_T7", new Vector3d(pos).add(0, 0.1, 0), 1.0f, s, b);
                        cleave(c, pos, strikeDir, base() * 0.6f, s, b);
                        launch(c, pos, strikeDir, 1.8f, s, b);
                    }
                    phase = 3;
                    return blades.isEmpty();
                }
                default -> { return blades.isEmpty(); }
            }
        }

        /** La taille verticale, puis ses effets selon le palier. */
        private void strike(Ref<EntityStore> c, Vector3d pos, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            Vector3d f = strikeDir != null ? strikeDir : flat(SpellFx.forward(c, s));
            if (tier < 5) {
                AnimationUtils.playAnimation(c, AnimationSlot.Action, SET, tier >= 3 ? "LuoCoup4" : "SwingDownStrong", true, s);
                SpellFx.push(c, new Vector3d(f).mul(5), false, b);
            }
            float dmg = base();
            SpellFx.sound(tier >= 6 ? "SFX_Longsword_Special_Impact" : "SFX_Sword_T2_Swing_Down", pos, b);
            Vector3d front = new Vector3d(pos).add(new Vector3d(f).mul(1.6));
            SpellFx.fx("Impact_Sword_Charged", new Vector3d(front).add(0, 1.0, 0), 1.0f + tier * 0.12f, s, b);
            SpellFx.fx("Gaia_Dust_Burst", front, 0.6f + tier * 0.1f, s, b);
            cleave(c, pos, f, dmg, s, b);
            if (tier >= 5) {                                                      // impact en cercle a la retombee
                for (Ref<EntityStore> e : SpellFx.enemiesAround(c, pos, 4.0, s))
                    SpellFx.damage(c, e, "Physical", dmg * 0.5f, new Vector3d(0, 6, 0), "Impact_Strong", b);
                SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(pos).add(0, 0.05, 0), 2.2f, s, b);
            }
            double wave = WAVE[tier - 1];
            if (wave > 0) wave(c, pos, f, wave, dmg * 0.6f, s, b);
            if (tier >= 4) launch(c, pos, f, tier >= 7 ? 2.4f : 1.2f, s, b);
            if (tier >= 6) {                                                      // trois cieux : deux lames de plus, colonne doree
                launch(c, pos, rotateY(f, 15), 1.0f, s, b);
                launch(c, pos, rotateY(f, -15), 1.0f, s, b);
                SpellFx.fx("Ki_Colonne_T6", new Vector3d(front).add(0, 0.1, 0), 1.3f, s, b);
            }
            if (tier >= 7) MartialTerrain.fissure(SpellFx.world(s), pos, f, tier >= 8 ? 24 : 16, s, b);
            if (tier >= 8) sky(c, pos, s, b);
            phase = 2;
            nextAt = System.currentTimeMillis() + 400;
        }

        /** Arc de la taille devant le porteur ; les ennemis touches sont plaques au sol (ou projetes en l'air des le Saint). */
        private void cleave(Ref<EntityStore> c, Vector3d pos, Vector3d f, float dmg, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(c, pos, REACH + tier * 0.25, s)) {
                Vector3d p = SpellFx.position(e, s);
                if (p == null) continue;
                Vector3d to = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
                if (to.lengthSquared() > 1e-4 && to.normalize().angle(f) > HALF_ANGLE) continue;
                SpellFx.fx("Impact_Sword_Charged", new Vector3d(p).add(0, 1.1, 0), 1.0f, s, b);
                SpellFx.damage(c, e, "Physical", dmg, new Vector3d(f).mul(3).add(0, tier >= 6 ? 6 : -2, 0), "Impact_Strong", b);
            }
        }

        /** Onde au sol : une ligne de poussiere devant le porteur, qui frappe les ennemis sur son passage. */
        private void wave(Ref<EntityStore> c, Vector3d pos, Vector3d f, double length, float dmg, Store<EntityStore> s,
                          CommandBuffer<EntityStore> b) {
            Set<Ref<EntityStore>> hit = new HashSet<>();
            for (double d = 2; d <= length; d += 2) {
                Vector3d at = new Vector3d(pos).add(new Vector3d(f).mul(d));
                SpellFx.fx("Gaia_Dust_Burst", at, 0.5f, s, b);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(c, new Vector3d(at).add(0, 0.8, 0), 1.6, s))
                    if (hit.add(e)) SpellFx.damage(c, e, "Physical", dmg, new Vector3d(f).mul(2).add(0, 4, 0), "Impact_Light", b);
            }
        }

        /** Lame de Ki verticale dans la direction f (taille scale). */
        private void launch(Ref<EntityStore> c, Vector3d pos, Vector3d f, float scale, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            RuneSpell.Instance bl = Tenryumaru.kiBlade(c, pos, new Vector3d(f), MartialKi.bladeKey(tier, "V"), 38, 16 * Math.max(1f, scale * 0.8f),
                    1.5 * scale, base() * 0.8f, scale, null, s, b);
            if (bl != null) blades.add(bl);
        }

        /** Dieu Martial : une lame geante tombe du ciel sur le point vise. */
        private void sky(Ref<EntityStore> c, Vector3d pos, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            Vector3d target = SpellFx.targetPoint(c, s, 24, 10);
            if (target == null) target = new Vector3d(pos).add(new Vector3d(strikeDir).mul(10));
            SpellFx.fx("Ki_Colonne_T8", new Vector3d(target).add(0, 0.1, 0), 2.5f, s, b);
            SpellFx.fx("Ki_Anneau_T8", new Vector3d(target).add(0, 0.05, 0), 3.0f, s, b);
            SpellFx.sound("SFX_Longsword_Special_Impact", target, b);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(c, target, 6.0, s))
                SpellFx.damage(c, e, "Physical", 200f * power, new Vector3d(0, 10, 0), "Impact_Strong", b);
            MartialTerrain.fissure(SpellFx.world(s), target, rotateY(strikeDir, 90), 12, s, b);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (RuneSpell.Instance bl : blades) { try { bl.cleanup(cb); } catch (Exception ignored) { } }
            blades.clear();
        }
    }

    private static Vector3d flat(Vector3d v) {
        Vector3d f = new Vector3d(v.x, 0, v.z);
        return f.lengthSquared() < 1e-6 ? new Vector3d(0, 0, 1) : f.normalize();
    }

    private static Vector3d rotateY(Vector3d v, double deg) {
        double a = Math.toRadians(deg), cos = Math.cos(a), sin = Math.sin(a);
        return new Vector3d(v.x * cos - v.z * sin, 0, v.x * sin + v.z * cos);
    }
}
