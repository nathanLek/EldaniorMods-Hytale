package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de la Projection d'epee d'Aura (Luo, Epic, Grand Maitre Martial, voie martiale ; refaite le 2026-11-10 selon
 * l'utilisateur) : des epees 3D de Ki (katana vanilla recolore a la couleur de l'aura du palier, translucide, trainee et
 * fumee de Ki : modele Ki_Epee_Proj_T<n> + effet Ki_Epee_Corps_T<n>, gen_voie_martiale.py) volent dans le ciel autour du
 * lanceur, chacune sur une trajectoire aleatoire, gracieuse et ondulee ; la pointe suit toujours le mouvement. (Les epees
 * de particules Ki_Epee_Holo_T, aimees de l'utilisateur, sont gardees pour d'autres competences.)
 * 1 / 3 / 6 / 12 epees (Grand Maitre, Saint, Maitre Supreme, Dieu Martial).
 *
 * Attaque : les epees partent chacune a leur tour (la suivante quand la precedente a fait CHAIN de son premier vol) vers les
 * ennemis a RANGE blocs. Une epee lancee ne revient pas vers le lanceur : elle s'acharne sur sa cible (vol ondule, coupe 45 /
 * 55 / 65 / 80, puis grand arc qui la replace autour de la cible pour la passe suivante) ; apres ATTACKS coupes elle se dissipe
 * en fumee. Si sa cible meurt avant, elle retourne en orbite et pourra repartir. Fin quand toutes les epees ont disparu, ou au
 * bout de DURATION_MS. Epargne groupe, guilde et famille (enemiesAround).
 * Fiche de conception : scripts/runes/specs/Rune_ProjectionEpeeAura.json
 */
public final class ProjectionEpeeAura implements RuneSpell {

    // ===== Reglages (equilibrage), indices = palier - 1 =====
    private static final int[] SWORDS = {1, 1, 1, 1, 1, 3, 6, 12};
    private static final float[] DAMAGE = {45, 45, 45, 45, 45, 55, 65, 80};
    private static final int ATTACKS = 3;
    private static final double RANGE = 16.0, CHAIN = 0.65, WAVE_AMP = 1.2, ARC = 1.4;
    /** Fluidite (retour du 2026-11-10 nuit) : lissage de l'orientation, recul de la prise d'elan, elan au-dela de la cible. */
    private static final double TURN = 0.35, PULL = 0.7, OVERSHOOT = 3.0;
    private static final long WINDUP_MS = 180;
    /** Retour du 2026-11-10 (nuit, 2e) : une epee ne disparait qu'apres ses ATTACKS coupes ; DURATION_MS = plafond de securite. */
    private static final long DURATION_MS = 90_000, FLY_BASE_MS = 380, FLY_PER_BLOCK_MS = 25, SWOOP_MS = 520,
            APPEAR_MS = 500;

    // ===== Assets =====
    private static final String CARRIER = "Projectile_Config_Ki_Epee_Proj_T", BODY = "Ki_Epee_Corps_T", FX_CUT = "Impact_Sword_Charged";
    private static final String SFX_APPEAR = "SFX_Ability_Wind_Strike_Imbue", SFX_CUT = "SFX_Ability_Wind_Strike_Slash",
            SFX_LAUNCH = "SFX_Light_Melee_T2_Lunge";

    @Override public String id() { return "ProjectionEpeeAura"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        int tier = Math.max(5, MartialKi.tier(caster, store));
        long now = System.currentTimeMillis();
        Swarm s = new Swarm(tier, now);
        var r = ThreadLocalRandom.current();
        for (int i = 0; i < SWORDS[tier - 1]; i++) {
            Sword sw = new Sword(i);
            sw.phase0 = i * 2 * Math.PI / SWORDS[tier - 1] + r.nextDouble(-0.3, 0.3);
            sw.radius = r.nextDouble(1.8, 3.0) + (i % 2) * 0.8;          // dans le ciel autour du lanceur
            sw.height = r.nextDouble(2.6, 4.2);
            sw.spin = r.nextDouble(0.9, 1.5) * (r.nextBoolean() ? 1 : -1);
            sw.waveF = r.nextDouble(0.8, 1.6);
            sw.waveA = r.nextDouble(0.3, 0.7);
            sw.wobble = r.nextDouble(0.2, 0.5);
            s.swords.add(sw);
        }
        SpellFx.fx("Ki_Anneau_T" + tier, new Vector3d(pos).add(0, 0.05, 0), 1.6f, store, cb);
        SpellFx.sound(SFX_APPEAR, pos, cb);
        return s;
    }

    private static final class Sword {
        final int index;
        double phase0, radius, height, spin, waveF, waveA, wobble, sign = 1;
        Vector3d pos, dir = new Vector3d(0, 0, 1), from, swoopTo;
        Ref<EntityStore> target, model;
        boolean dressed;                                                        // effet translucide pose (entite prete)
        int state, attacks;                                                     // 0 orbite, 1 attaque, 2 arc autour de la cible
        long stateAt, flyMs;
        boolean gone, launched, hitDone, lastPass;
        double hitK = 0.7;
        Sword(int index) { this.index = index; }
    }

    private static final class Swarm implements Instance {
        final int tier;
        final long startAt;
        final List<Sword> swords = new ArrayList<>();
        final Map<Ref<EntityStore>, Long> own = new ConcurrentHashMap<>();
        Sword lastLaunched;
        int launches;

        Swarm(int tier, long startAt) { this.tier = tier; this.startAt = startAt; }

        /** Orbite gracieuse : cercle qui respire (rayon ondule), hauteur en vague, sens et vitesses propres a chaque epee. */
        private Vector3d orbit(Sword sw, Vector3d me, double t) {
            double a = sw.phase0 + t * sw.spin;
            double r = sw.radius + Math.sin(t * sw.waveF * 1.3 + sw.index) * sw.wobble;
            double h = sw.height + Math.sin(t * sw.waveF + sw.index * 0.7) * sw.waveA;
            return new Vector3d(me).add(Math.cos(a) * r, h, Math.sin(a) * r);
        }

        /** Vol ondule de from vers to (k de 0 a 1) : interpolation lissee + vague laterale decroissante + arc. */
        private static Vector3d wave(Sword sw, Vector3d from, Vector3d to, double k, double arc) {
            double e = k * k * (3 - 2 * k);
            Vector3d line = new Vector3d(to).sub(from);
            Vector3d side = new Vector3d(line).cross(0, 1, 0);
            if (side.lengthSquared() < 1e-6) side.set(1, 0, 0);
            side.normalize();
            return new Vector3d(from).add(new Vector3d(line).mul(e))
                    .add(side.mul(Math.sin(k * Math.PI * 2) * WAVE_AMP * (1 - k) * sw.sign)).add(0, Math.sin(k * Math.PI) * arc, 0);
        }

        /** Deplace l'epee ; son orientation suit le mouvement en douceur (lissage TURN), plus de saccades de la pointe. */
        private void move(Sword sw, Vector3d next) {
            if (sw.pos != null) {
                Vector3d d = new Vector3d(next).sub(sw.pos);
                if (d.lengthSquared() > 1e-5) aim(sw, d.normalize(), TURN);
            }
            sw.pos = next;
        }

        private static void aim(Sword sw, Vector3d want, double k) {
            Vector3d mixed = new Vector3d(sw.dir).mul(1 - k).add(new Vector3d(want).mul(k));
            if (mixed.lengthSquared() > 1e-6) sw.dir = mixed.normalize();
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d me = SpellFx.position(caster, store);
            if (me == null || now - startAt >= DURATION_MS) return true;
            double t = (now - startAt) / 1000.0;
            own.values().removeIf(v -> v < now);
            List<Ref<EntityStore>> enemies = null;
            int alive = 0;
            for (Sword sw : swords) {
                if (sw.gone) continue;
                alive++;
                switch (sw.state) {
                    case 0 -> {                                                 // orbite (sortie du lanceur au debut)
                        Vector3d o = orbit(sw, me, t);
                        double rise = Math.min(1.0, (now - startAt) / (double) APPEAR_MS);
                        if (rise < 1.0) {                                       // les epees jaillissent du lanceur
                            double e = 1 - Math.pow(1 - rise, 3);
                            Vector3d chest = new Vector3d(me).add(0, 1.2, 0);
                            o = chest.add(new Vector3d(o).sub(chest).mul(e)).add(0, Math.sin(rise * Math.PI) * 0.8, 0);
                        }
                        move(sw, o);
                        // a tour de role : la suivante part un peu avant que la precedente ne touche (CHAIN de son trajet)
                        boolean turn = lastLaunched == null || lastLaunched.gone || lastLaunched.state == 0 || lastLaunched.state == 2
                                || (lastLaunched.state == 1 && now - lastLaunched.stateAt >= lastLaunched.flyMs * lastLaunched.hitK * CHAIN);
                        if (turn && now - startAt >= APPEAR_MS) {
                            if (enemies == null) enemies = SpellFx.enemiesAround(caster, me, RANGE, store);
                            if (!enemies.isEmpty()) attack(sw, enemies.get(launches++ % enemies.size()), now, store, cb);
                        }
                    }
                    case 3 -> {                                                 // prise d'elan : recule, pointe vers la cible
                        Vector3d tp = alive(sw.target, store);
                        if (tp == null) { sw.state = 0; continue; }
                        Vector3d to = new Vector3d(tp).add(0, 1.1, 0);
                        Vector3d toward = new Vector3d(to).sub(sw.from);
                        if (toward.lengthSquared() < 1e-4) toward.set(0, 0, 1);
                        toward.normalize();
                        double k = Math.min(1.0, (now - sw.stateAt) / (double) WINDUP_MS);
                        double e = Math.sin(k * Math.PI / 2);
                        sw.pos = new Vector3d(sw.from).sub(new Vector3d(toward).mul(PULL * e)).add(0, 0.25 * e, 0);
                        aim(sw, toward, 0.35);                                  // la pointe se tourne vers la cible
                        if (k < 1.0) continue;
                        strike(sw, to, now);
                        SpellFx.sound(SFX_LAUNCH, sw.pos, cb);
                    }
                    case 1 -> {                                                 // frappe : accelere, traverse la cible, continue
                        Vector3d tp = alive(sw.target, store);
                        if (tp == null && !sw.hitDone) { sw.state = 0; continue; }
                        double k = Math.min(1.0, (now - sw.stateAt) / (double) sw.flyMs);
                        double e = k < sw.hitK ? sw.hitK * Math.pow(k / sw.hitK, 1.8)                // acceleration jusqu'a la cible
                                : sw.hitK + (1 - sw.hitK) * Math.sin((k - sw.hitK) / (1 - sw.hitK) * Math.PI / 2);   // elan amorti
                        Vector3d line = new Vector3d(sw.swoopTo).sub(sw.from);
                        Vector3d side = new Vector3d(line).cross(0, 1, 0);
                        if (side.lengthSquared() < 1e-6) side.set(1, 0, 0);
                        side.normalize().mul(Math.sin(e * Math.PI) * 0.45 * sw.sign);
                        move(sw, new Vector3d(sw.from).add(new Vector3d(line).mul(e)).add(side));
                        if (!sw.hitDone && e >= sw.hitK) {                      // la coupe, en passant
                            sw.hitDone = true;
                            Vector3d at = tp != null ? new Vector3d(tp).add(0, 1.1, 0) : new Vector3d(sw.pos);
                            SpellFx.fx(FX_CUT, at, 1.1f, store, cb);
                            SpellFx.fx("Ki_Orbe_T" + tier, at, 0.7f, store, cb);
                            SpellFx.sound(SFX_CUT, at, cb);
                            if (tp != null) MartialKi.kiDamage(caster, sw.target, DAMAGE[tier - 1], "Physical", null, own, cb);
                            if (++sw.attacks >= ATTACKS) { sw.lastPass = true; }
                        }
                        if (k < 1.0) continue;
                        if (sw.lastPass) { dissipate(sw, store, cb); continue; }
                        if (tp == null) { sw.state = 0; continue; }          // cible morte : coups restants, retour en orbite
                        var r = ThreadLocalRandom.current();                    // grand arc qui la replace autour de la cible
                        double a = r.nextDouble(0, Math.PI * 2);
                        sw.from = new Vector3d(sw.pos);
                        sw.swoopTo = new Vector3d(tp).add(Math.cos(a) * 3.2, r.nextDouble(2.0, 3.4), Math.sin(a) * 3.2);
                        sw.sign = -sw.sign;
                        sw.state = 2;
                        sw.stateAt = now;
                    }
                    case 2 -> {                                                 // arc ample, puis nouvelle passe
                        Vector3d tp = alive(sw.target, store);
                        if (tp == null) { sw.state = 0; continue; }
                        double k = Math.min(1.0, (now - sw.stateAt) / (double) SWOOP_MS);
                        move(sw, wave(sw, sw.from, sw.swoopTo, k, 1.8));
                        if (k >= 1.0) attack(sw, sw.target, now, store, cb);
                    }
                    default -> { }
                }
            }
            draw(caster, store, cb);
            return alive == 0;
        }

        private Vector3d alive(Ref<EntityStore> target, Store<EntityStore> store) {
            return target != null && target.isValid() && !RuneSpellRuntime.isDying(target) ? SpellFx.position(target, store) : null;
        }

        /** Engage une passe : prise d'elan (etat 3), puis frappe (strike). */
        private void attack(Sword sw, Ref<EntityStore> target, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d tp = SpellFx.position(target, store);
            if (tp == null || sw.pos == null) return;
            boolean fromOrbit = !sw.launched || sw.state == 0;
            sw.target = target;
            sw.from = new Vector3d(sw.pos);
            sw.state = 3;
            sw.stateAt = now;
            sw.flyMs = WINDUP_MS + FLY_BASE_MS;                                 // pour l'enchainement des epees (CHAIN)
            sw.launched = true;
            if (fromOrbit) lastLaunched = sw;                                   // une epee qui quitte l'orbite passe son tour
        }

        /** Frappe : ligne de la position d'elan jusqu'au-dela de la cible (OVERSHOOT), la coupe tombe en la traversant. */
        private void strike(Sword sw, Vector3d to, long now) {
            sw.from = new Vector3d(sw.pos);
            Vector3d dir = new Vector3d(to).sub(sw.from);
            double dist = Math.max(0.5, dir.length());
            dir.div(dist);
            sw.swoopTo = new Vector3d(to).add(new Vector3d(dir).mul(OVERSHOOT)).add(0, 0.6, 0);
            sw.hitK = dist / (dist + OVERSHOOT);
            sw.hitDone = false;
            sw.flyMs = FLY_BASE_MS + (long) ((dist + OVERSHOOT) * FLY_PER_BLOCK_MS);
            sw.state = 1;
            sw.stateAt = now;
        }

        private void dissipate(Sword sw, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            sw.gone = true;
            SpellFx.remove(sw.model, cb);
            sw.model = null;
            SpellFx.fx("Ki_Orbe_T" + tier, sw.pos, 1.2f, store, cb);
            SpellFx.fx("Ki_Anneau_T" + tier, sw.pos, 0.6f, store, cb);
        }

        /** Chaque epee : modele 3D cree a sa premiere position, maintenu sur sa trajectoire, pointe dans le sens du mouvement. */
        private void draw(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Sword sw : swords) {
                if (sw.gone || sw.pos == null) continue;
                if (sw.model == null) {
                    sw.model = SpellFx.spawnModel(caster, CARRIER + tier, sw.pos, sw.dir, cb);
                    SpellFx.notSaved(sw.model, store, cb);
                    continue;
                }
                if (!sw.model.isValid()) continue;                                // le porteur n'existe qu'au tick suivant
                if (!sw.dressed) { sw.dressed = true; SpellFx.effect(sw.model, BODY + tier, cb); }
                SpellFx.hold(sw.model, sw.pos, SpellFx.facing(sw.dir, true), store);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Sword sw : swords) SpellFx.remove(sw.model, cb);
        }

        @Override
        public void abort() {
            for (Sword sw : swords) RuneSpellRuntime.removeLater(sw.model);
        }
    }
}
