package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gungnir, la Lance d'Odin (artefact divin, Artefact_Gungnir) : elle ne manque jamais sa cible et revient toujours dans
 * la main de son porteur. Deux sorts du moteur RuneSpell, lances par les interactions de l'arme :
 *   - GungnirLancer (clic maintenu) : la lance part vers l'ennemi vise (ou le plus proche de la ligne de visee), le frappe,
 *     libere un eclat de foudre runique autour de lui, puis revient ; sans ennemi, elle file droit devant et revient ;
 *   - OeilOdin (signature) : marque jusqu'a 6 ennemis proches, les transperce l'un apres l'autre, puis revient.
 * La lance en vol est un modele (Projectile_Config_Gungnir) deplace par le code a chaque tick : trajectoire exacte, aucune
 * physique. Une seule lance en vol par porteur.
 */
public final class Gungnir {

    private Gungnir() {}

    public static final String ITEM_ID = "Artefact_Gungnir";
    private static final String FLYING = "Projectile_Config_Gungnir";
    private static final String FX_HIT = "Taranis_Electric_Burst", FX_MARK = "Bifrost_Divine", FX_RETURN = "Gilgamesh_Sword_Burst";
    private static final String SFX_HIT = "SFX_Spear_Impact", SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap";
    private static final String SFX_THUNDER = "SFX_Global_Weather_Thunder";

    /** Porteurs dont la lance est en vol. */
    private static final Set<Ref<EntityStore>> inFlight = ConcurrentHashMap.newKeySet();

    private static Vector3d eye(Ref<EntityStore> caster, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(caster, store);
        return p != null ? p.add(0, 1.5, 0) : null;
    }

    private static Vector3d chest(Ref<EntityStore> e, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(e, store);
        return p != null ? p.add(0, 1.0, 0) : null;
    }

    // ===================== Lancer =====================

    public static final class Lancer implements RuneSpell {
        private static final double RANGE = 40, ASSIST_ANGLE = Math.toRadians(14), FREE_FLIGHT = 30;
        private static final float DAMAGE = 90f, SPLASH = 25f;
        private static final double SPLASH_RADIUS = 3.5;

        @Override public String id() { return "GungnirLancer"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!inFlight.add(caster)) return null;                                     // deja en vol
            Vector3d from = eye(caster, store);
            if (from == null) { inFlight.remove(caster); return null; }
            Vector3d look = SpellFx.lookDirection(caster, store);
            Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);
            if (target == null) target = assisted(caster, from, look, store);
            List<Ref<EntityStore>> targets = target != null ? List.of(target) : List.of();
            Vector3d free = new Vector3d(from).add(new Vector3d(look).mul(FREE_FLIGHT));
            return Flight.launch(caster, from, look, targets, free, 40, 50, 8_000, false, store, cb);
        }

        /** Visee assistee : l'ennemi le plus proche de la ligne de visee (cone de 14 degres). */
        private static Ref<EntityStore> assisted(Ref<EntityStore> caster, Vector3d from, Vector3d look, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestAngle = ASSIST_ANGLE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, from, RANGE, store)) {
                Vector3d c = chest(e, store);
                if (c == null) continue;
                double angle = new Vector3d(c).sub(from).angle(look);
                if (angle < bestAngle) { bestAngle = angle; best = e; }
            }
            return best;
        }
    }

    // ===================== Oeil d'Odin =====================

    public static final class OeilOdin implements RuneSpell {
        private static final double RADIUS = 25;
        private static final int MAX_TARGETS = 6;

        @Override public String id() { return "OeilOdin"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d from = eye(caster, store);
            if (from == null || !inFlight.add(caster)) return null;
            List<Ref<EntityStore>> chain = chain(caster, from, store);
            if (chain.isEmpty()) { inFlight.remove(caster); return null; }
            for (Ref<EntityStore> e : chain) {                                           // l'oeil d'Odin les marque
                Vector3d c = chest(e, store);
                if (c != null) SpellFx.fx(FX_MARK, c.add(0, 1.6, 0), 0.5f, store, cb);
            }
            SpellFx.sound(SFX_ZAP, from, cb);
            Vector3d look = SpellFx.lookDirection(caster, store);
            return Flight.launch(caster, from, look, chain, null, 60, 55, 12_000, true, store, cb);
        }

        /** Ennemis du rayon, dans l'ordre du trajet : chaque fois le plus proche du precedent. */
        private static List<Ref<EntityStore>> chain(Ref<EntityStore> caster, Vector3d from, Store<EntityStore> store) {
            List<Ref<EntityStore>> left = new ArrayList<>(SpellFx.enemiesAround(caster, from, RADIUS, store));
            List<Ref<EntityStore>> out = new ArrayList<>();
            Vector3d at = new Vector3d(from);
            while (!left.isEmpty() && out.size() < MAX_TARGETS) {
                final Vector3d here = at;
                Ref<EntityStore> next = left.stream().filter(e -> chest(e, store) != null)
                        .min(Comparator.comparingDouble(e -> chest(e, store).distanceSquared(here))).orElse(null);
                if (next == null) break;
                left.remove(next);
                out.add(next);
                at = chest(next, store);
            }
            return out;
        }
    }

    // ===================== Lance en vol =====================

    /**
     * Trajet de la lance : chaque cible a son tour (la poursuit jusqu'au contact), sinon le point libre (en touchant le
     * premier ennemi croise), puis retour vers la main du porteur.
     */
    private static final class Flight implements RuneSpell.Instance {
        private static final double HIT_RADIUS = 1.3, CATCH_RADIUS = 1.6;
        private static final long MARK_DELAY_MS = 500, SPAWN_WAIT_MS = 1_000;

        final Ref<EntityStore> caster, spear;
        final List<Ref<EntityStore>> targets;
        final Vector3d free;
        final double outSpeed, backSpeed;
        final long startedAt, timeoutMs;
        final boolean signature;
        Vector3d pos, dir;
        long last;
        int index;
        boolean returning, spawned;

        private Flight(Ref<EntityStore> caster, Ref<EntityStore> spear, Vector3d pos, Vector3d dir, List<Ref<EntityStore>> targets,
                       Vector3d free, double outSpeed, double backSpeed, long timeoutMs, boolean signature) {
            this.caster = caster; this.spear = spear; this.pos = pos; this.dir = dir; this.targets = targets; this.free = free;
            this.outSpeed = outSpeed; this.backSpeed = backSpeed; this.timeoutMs = timeoutMs; this.signature = signature;
            this.startedAt = this.last = System.currentTimeMillis();
        }

        static Flight launch(Ref<EntityStore> caster, Vector3d from, Vector3d look, List<Ref<EntityStore>> targets, Vector3d free,
                             double outSpeed, double backSpeed, long timeoutMs, boolean signature,
                             Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d start = new Vector3d(from).add(new Vector3d(look).mul(1.0));
            Ref<EntityStore> spear = SpellFx.spawnModel(caster, FLYING, start, look, cb);
            if (spear == null) { inFlight.remove(caster); return null; }
            return new Flight(caster, spear, start, new Vector3d(look), targets, free, outSpeed, backSpeed, timeoutMs, signature);
        }

        @Override
        public boolean tick(Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!caster.isValid() || now - startedAt > timeoutMs) return true;
            if (!spawned) {                                                              // creee par le CommandBuffer : au tick suivant
                if (SpellFx.position(spear, store) == null) return now - startedAt > SPAWN_WAIT_MS;
                spawned = true;
            }
            if (!spear.isValid()) return true;
            double dt = Math.min(0.1, (now - last) / 1000.0);
            last = now;
            if (signature && now - startedAt < MARK_DELAY_MS) { SpellFx.hold(spear, pos, SpellFx.facing(dir, true), store); return false; }
            Vector3d goal = goal(store);
            if (goal == null) return false;                                              // cible disparue : passe a la suivante
            Vector3d to = new Vector3d(goal).sub(pos);
            double dist = to.length(), step = (returning ? backSpeed : outSpeed) * dt;
            if (dist > 1e-4) dir = to.div(dist);
            if (dist <= step) pos = new Vector3d(goal); else pos.add(new Vector3d(dir).mul(step));
            SpellFx.hold(spear, pos, SpellFx.facing(dir, true), store);
            if (returning) {
                if (dist <= Math.max(step, CATCH_RADIUS)) { SpellFx.fx(FX_RETURN, pos, 0.4f, store, cb); return true; }
                return false;
            }
            if (index < targets.size()) {
                if (dist <= Math.max(step, HIT_RADIUS)) strike(targets.get(index++), store, cb);
            } else {                                                                     // vol libre : premier ennemi croise
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, HIT_RADIUS + 0.5, store)) { strike(e, store, cb); break; }
                if (!returning && dist <= step) returning = true;
            }
            if (index >= targets.size() && !targets.isEmpty()) returning = true;
            return false;
        }

        /** Prochain point : la cible en cours (sautee si elle a disparu), le point libre, ou la main du porteur. */
        private Vector3d goal(Store<EntityStore> store) {
            if (returning) return eye(caster, store);
            while (index < targets.size()) {
                Ref<EntityStore> t = targets.get(index);
                Vector3d c = t.isValid() ? chest(t, store) : null;
                if (c != null) return c;
                index++;
            }
            if (!targets.isEmpty()) { returning = true; return eye(caster, store); }
            return free;
        }

        /** Coup de la lance : degats, foudre runique ; le lancer touche aussi les ennemis autour de l'impact. */
        private void strike(Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = chest(target, store);
            if (at == null) return;
            Vector3d push = new Vector3d(dir.x, 0, dir.z);
            if (push.lengthSquared() > 1e-4) push.normalize().mul(signature ? 4 : 6).add(0, 3, 0); else push = null;
            SpellFx.damage(caster, target, "Physical", signature ? 120f : Lancer.DAMAGE, push, "Impact_Light", cb);
            SpellFx.fx(FX_HIT, at, signature ? 1.2f : 1.0f, store, cb);
            SpellFx.sound(SFX_HIT, at, cb);
            SpellFx.sound(SFX_ZAP, at, cb);
            if (signature) {
                SpellFx.fx(FX_MARK, at, 0.6f, store, cb);
                if (index >= targets.size()) SpellFx.sound(SFX_THUNDER, at, cb);       // dernier transperce
                return;
            }
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, Lancer.SPLASH_RADIUS, store))
                if (!e.equals(target)) SpellFx.damage(caster, e, "Physical", Lancer.SPLASH, null, "Impact_Light", cb);
            returning = true;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(spear, cb);
            inFlight.remove(caster);
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(spear);
            inFlight.remove(caster);
        }
    }
}
