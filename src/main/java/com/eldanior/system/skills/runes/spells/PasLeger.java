package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rune du Pas leger (Luo, Rare, Expert Martial, voie martiale ; refaite le 2026-11-10 a la demande de l'utilisateur) : une
 * ruee legere, animation de dash de l'arc (Bow/DashForward), qui s'ameliore a chaque classe ; plus de teleportation.
 *   3 Expert          8 blocs au sol ;
 *   4 Maitre          10 blocs, intouchable pendant le pas (pas fantome) ;
 *   5 Grand Maitre    12 blocs dans la direction du regard (monte, descend, aussi en l'air), intouchable ;
 *   6 Saint           15 blocs dans le regard, intouchable ;
 *   7 Maitre Supreme  18 blocs, coupe (50) les ennemis traverses, aucune recharge ;
 *   8 Dieu Martial    24 blocs, coupe (80), aucune recharge (« plus de limite d'utilisation »).
 * Recharge reelle (l'objet a 0,25 s) : 3 / 2,5 / 2 / 1 s puis aucune ; 10 mana. Sensation de legerete : la gravite est annulee
 * pendant le pas, traces de pas (anneau de Ki) ; petit rebond a l'arrivee ; vitesse x1,15-1,35 pendant 4 s.
 * Retours du 2026-11-10 (soir) :
 *  - sillage : effet Pas_Leger_Sillage_<n> (traits de lumiere, voile et fumee de Ki laisses dans l'air, corps lumineux) au
 *    lieu des orbes poses par le code ;
 *  - ruee guidee (Maitre Martial et au-dela) : si un ennemi se trouve dans un cone de GUIDE_ANGLE devant le regard, a moins
 *    de RANGE x GUIDE_REACH, la ruee le suit et s'arrete devant lui (GUIDE_STOP blocs), tournee vers lui.
 * Fiche de conception : scripts/runes/specs/Rune_PasLeger.json
 */
public final class PasLeger implements RuneSpell {

    // ===== Reglages (equilibrage), indices = palier - 1 =====
    private static final double[] RANGE = {8, 8, 8, 10, 12, 15, 18, 24};
    private static final double[] SPEED = {26, 26, 26, 28, 30, 33, 36, 42};
    private static final float[] CUT = {0, 0, 0, 0, 0, 0, 50, 80};
    private static final long[] COOLDOWN_MS = {3_000, 3_000, 3_000, 2_500, 2_000, 1_000, 0, 0};
    private static final double CUT_RADIUS = 1.8;
    private static final long MAX_MS = 900, FALL_GUARD_MS = 3_000;
    private static final int GHOST_TIER = 4, AERIAL_TIER = 5, GUIDE_TIER = 4;
    private static final double GUIDE_ANGLE = Math.toRadians(28), GUIDE_REACH = 1.3, GUIDE_STOP = 1.6;
    private static final Map<UUID, Long> ready = new ConcurrentHashMap<>();

    // ===== Assets =====
    static final String SPEED_EFFECT = "Pas_Leger_";
    private static final String FX_DUST = "Gaia_Dust_Burst" /* plus utilise */, FX_CUT = "Impact_Sword_Charged", TRAIL_EFFECT = "Pas_Leger_Sillage_";
    private static final String SFX_STEP = "SFX_Light_Melee_T2_Lunge", SFX_CUT = "SFX_Ability_Wind_Strike_Slash";

    @Override public String id() { return "PasLeger"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        int tier = Math.max(3, MartialKi.tier(caster, store));
        UUID uuid = SpellFx.uuid(caster, store);
        long now = System.currentTimeMillis();
        if (uuid != null && COOLDOWN_MS[tier - 1] > 0) {
            Long until = ready.get(uuid);
            if (until != null && until > now) {
                MartialKi.notify(caster, store, "<color:gray>Pas léger : encore " + Math.max(1, (until - now + 999) / 1000) + " s.</color>",
                        NotificationStyle.Default);
                return null;
            }
            ready.put(uuid, now + COOLDOWN_MS[tier - 1]);
        }
        SpellFx.protectFromFall(caster, FALL_GUARD_MS, store);
        SpellFx.effect(caster, SPEED_EFFECT + tier, cb);
        SpellFx.effect(caster, TRAIL_EFFECT + tier, cb);
        Ref<EntityStore> guide = tier >= GUIDE_TIER ? guideTarget(caster, pos, RANGE[tier - 1] * GUIDE_REACH, store) : null;
        Vector3d dir = guide != null ? toward(pos, SpellFx.position(guide, store))
                : tier >= AERIAL_TIER ? SpellFx.lookDirection(caster, store) : SpellFx.forward(caster, store);
        SpellFx.push(caster, new Vector3d(dir).mul(SPEED[tier - 1]).add(0, tier >= AERIAL_TIER || guide != null ? 0.4 : 1.0, 0), false, cb);
        SpellFx.sound(SFX_STEP, pos, cb);                                       // trainees : interaction (dash vanilla)
        Step step = new Step(pos, dir, tier, now);
        step.guide = guide;
        return step;
    }

    /** Ennemi le plus proche dans le cone du regard (angle GUIDE_ANGLE), a portee ; null sinon. */
    private static Ref<EntityStore> guideTarget(Ref<EntityStore> caster, Vector3d feet, double reach, Store<EntityStore> store) {
        Vector3d look = SpellFx.lookDirection(caster, store);
        Vector3d eye = new Vector3d(feet).add(0, 1.6, 0);
        Ref<EntityStore> best = null;
        double bestDist = Double.MAX_VALUE, cos = Math.cos(GUIDE_ANGLE);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, feet, reach, store)) {
            if (RuneSpellRuntime.isDying(e)) continue;
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d to = new Vector3d(p).add(0, 1.0, 0).sub(eye);
            double d = to.length();
            if (d < 1e-3 || d > reach || to.div(d).dot(look) < cos) continue;
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }

    /** Direction des pieds du lanceur vers les pieds de la cible (legere montee incluse si elle est plus haut). */
    private static Vector3d toward(Vector3d from, Vector3d to) {
        Vector3d d = to == null ? new Vector3d(0, 0, 1) : new Vector3d(to).sub(from);
        if (d.lengthSquared() < 1e-6) d.set(0, 0, 1);
        return d.normalize();
    }

    private static final class Step implements Instance {
        final Vector3d origin, dir;
        final int tier;
        final long startAt;
        final Set<Ref<EntityStore>> cut = new HashSet<>();
        Ref<EntityStore> guide;                                                 // ruee guidee : l'ennemi suivi

        Step(Vector3d origin, Vector3d dir, int tier, long startAt) {
            this.origin = origin; this.dir = dir; this.tier = tier; this.startAt = startAt;
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return tier >= GHOST_TIER && System.currentTimeMillis() - startAt < MAX_MS ? 0f : amount;    // pas fantome
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            Vector3d gp = guide != null && guide.isValid() && !RuneSpellRuntime.isDying(guide) ? SpellFx.position(guide, store) : null;
            if (guide != null && gp != null && pos != null) {                  // ruee guidee : suit l'ennemi, s'arrete devant lui
                if (SpellFx.horizontalDistance(pos, gp) <= GUIDE_STOP && Math.abs(gp.y - pos.y) < 2.5) {
                    SpellFx.push(caster, new Vector3d(0, 0.6, 0), false, cb);   // arret net devant la cible
                    SpellFx.teleport(caster, pos, SpellFx.facing(toward(pos, gp), false), store, cb);
                    return true;
                }
                dir.set(toward(pos, gp));
            }
            double range = RANGE[tier - 1] * (guide != null ? GUIDE_REACH : 1.0);
            if (pos == null || now - startAt >= MAX_MS || pos.distance(origin) >= range) {
                SpellFx.push(caster, new Vector3d(dir).mul(2.0).add(0, 2.5, 0), false, cb);   // petit rebond leger a l'arrivee
                return true;
            }
            if (CUT[tier - 1] > 0) {                                            // Maitre Supreme et au-dela : coupe au passage
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, CUT_RADIUS + 1, store)) {
                    if (!cut.add(e)) continue;
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
                    SpellFx.fx(FX_CUT, chest, 1.0f, store, cb);
                    SpellFx.sound(SFX_CUT, chest, cb);
                    MartialKi.kiDamage(caster, e, CUT[tier - 1], "Physical", null, null, cb);
                }
            }
            if (now - startAt >= 80)                                            // gravite annulee : il glisse sans retomber
                SpellFx.push(caster, new Vector3d(dir).mul(SPEED[tier - 1]).add(0, tier >= AERIAL_TIER || guide != null ? 0 : 0.1, 0), false, cb);
            return false;
        }
    }
}
