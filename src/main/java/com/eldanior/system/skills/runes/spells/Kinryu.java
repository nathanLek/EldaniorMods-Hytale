package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kinryu-O, Lame du Dieu-Dragon d'Or : katana divin du Clan Luo, tresor du Patriarche avec la Tenue Ancestrale du Dieu-Dragon
 * d'Or (FamilyKeySystem). Modele, objets et interactions : ceux des katanas Luo (scripts/artefacts/gen_luo_katanas.py,
 * LuoKatanas) ; seuls le maintien et la signature lui sont propres (LuoIai / LuoIaigiri appellent ces sorts quand le katana
 * tenu est Kinryu-O) :
 *   - Souffle du Dragon d'Or (attaque maintenue) : un dragon de lumiere d'or jaillit du fourreau et serpente droit devant sur
 *     {@link Souffle#RANGE} blocs ; il traverse murs et ennemis, chacun touche une fois ({@link Souffle#DAMAGE}).
 *   - Eclipse du Dieu-Dragon (signature) : le soleil s'eteint au-dessus de la zone visee (ecran assombri pour les joueurs
 *     proches, ennemis ralentis), un dragon d'or en descend en spirale et frappe toute la zone, puis deux ondes de choc.
 *     Le porteur est intouchable pendant le sort.
 * Le corps des dragons est une chaine de faisceaux d'or (Ra_Rayon) deplaces a chaque tick.
 */
public final class Kinryu {

    private Kinryu() {}

    public static final String ITEM = "Artefact_Sword_Kinryu";
    private static final String BEAM = "Ra_Rayon";
    private static final String FX_GOLD = "Gilgamesh_Sword_Burst", FX_SLASH = "Impact_Sword_Charged", FX_RING = "Eldanior_Pas_Celeste",
            FX_VOID = "Nyx_Void";
    private static final String SFX_ROAR = "SFX_Ability_Wind_Strike_Slash", SFX_HIT = "SFX_Sword_T2_Impact",
            SFX_BOOM = "SFX_Global_Weather_Thunder";

    static boolean is(LuoKatanas.Katana k) {
        return k != null && ITEM.equals(k.id());
    }

    /** Corps de dragon : faisceaux d'or entre des points successifs, de la tete (gros) a la queue (fin). */
    private static final class Body {
        final Ref<EntityStore>[] beams;
        final float head, tail;

        @SuppressWarnings("unchecked")
        Body(int segments, float head, float tail) {
            this.beams = new Ref[segments];
            this.head = head;
            this.tail = tail;
        }

        void draw(List<Vector3d> points, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (int i = 0; i < beams.length; i++) {
                if (i + 1 >= points.size()) { hide(i, cb); continue; }
                Vector3d a = points.get(i), b = points.get(i + 1);
                float scale = head + (tail - head) * i / Math.max(1, beams.length - 1);
                if (beams[i] == null || !beams[i].isValid()) {
                    if (beams[i] == null) beams[i] = SpellFx.spawnBeam(BEAM, a, b, scale, 8_000, store, cb);   // valide au tick suivant
                } else {
                    SpellFx.beam(beams[i], BEAM, a, b, scale, store);
                }
            }
        }

        private void hide(int i, CommandBuffer<EntityStore> cb) {
            if (beams[i] != null) { SpellFx.remove(beams[i], cb); beams[i] = null; }
        }

        void clear(CommandBuffer<EntityStore> cb) {
            for (int i = 0; i < beams.length; i++) hide(i, cb);
        }

        void abort() {
            for (Ref<EntityStore> b : beams) if (b != null) RuneSpellRuntime.removeLater(b);
        }
    }

    // ===================== Maintien : Souffle du Dragon d'Or =====================

    private static final Map<Ref<EntityStore>, Long> souffleReady = new ConcurrentHashMap<>();

    /** Lance le Souffle (LuoIai, Kinryu-O en main) ; null pendant la recharge. */
    static RuneSpell.Instance souffle(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d feet = SpellFx.position(caster, store);
        if (feet == null) return null;
        long now = System.currentTimeMillis();
        Long ready = souffleReady.get(caster);
        if (ready != null && now < ready) return null;
        souffleReady.put(caster, now + Souffle.COOLDOWN_MS);
        Vector3d look = SpellFx.lookDirection(caster, store).normalize();
        TenryumaruSystem.drawn(caster);
        SpellFx.sound(SFX_ROAR, feet, cb);
        SpellFx.sound2dNear(SFX_BOOM, feet, 24, store);
        SpellFx.fx(FX_RING, new Vector3d(feet).add(0, 0.1, 0), 2.0f, store, cb);
        return new Souffle(new Vector3d(feet).add(0, 1.3, 0).add(new Vector3d(look).mul(1.2)), look, now);
    }

    private static final class Souffle implements RuneSpell.Instance {
        static final double RANGE = 26, SPEED = 30, AMPLITUDE = 1.7, WAVE = 0.5, HIT_RADIUS = 2.4, SPACING = 1.5;
        static final float DAMAGE = 170f;
        static final int SEGMENTS = 8;
        static final long COOLDOWN_MS = 4_000, FX_MS = 70;

        final Vector3d origin, dir, side, up;
        final long startAt;
        final List<Vector3d> trail = new ArrayList<>();               // positions de la tete, la plus recente en premier
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        final Body body = new Body(SEGMENTS, 1.7f, 0.5f);
        double travelled;
        long last, lastFx;

        Souffle(Vector3d origin, Vector3d dir, long now) {
            this.origin = origin;
            this.dir = dir;
            Vector3d s = new Vector3d(dir).cross(0, 1, 0);
            if (s.lengthSquared() < 1e-4) s.set(1, 0, 0);
            this.side = s.normalize();
            this.up = new Vector3d(side).cross(dir).normalize();
            this.startAt = now;
            this.last = now;
        }

        Vector3d at(double t) {
            return new Vector3d(origin).add(new Vector3d(dir).mul(t))
                    .add(new Vector3d(side).mul(Math.sin(t * WAVE) * AMPLITUDE * Math.min(1, t / 4)))
                    .add(new Vector3d(up).mul(Math.sin(t * WAVE * 0.5 + 0.6) * AMPLITUDE * 0.45 * Math.min(1, t / 4)));
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            double step = SPEED * Math.min(0.2, (now - last) / 1000.0);
            last = now;
            double tail = SEGMENTS * SPACING;
            if (travelled >= RANGE + tail) return true;                                   // la queue a fini de passer
            double before = travelled;
            travelled += step;
            double headT = Math.min(travelled, RANGE);
            for (double t = before; t < Math.min(travelled, RANGE); t += 0.5) touch(caster, at(t), store, cb);
            Vector3d head = at(headT);
            List<Vector3d> points = new ArrayList<>();
            for (int i = 0; i <= SEGMENTS; i++) {
                double t = travelled - i * SPACING;
                if (t < 0) break;
                points.add(at(Math.min(t, RANGE)));
            }
            body.draw(points, store, cb);
            if (now - lastFx >= FX_MS && travelled < RANGE) {
                SpellFx.fx(FX_GOLD, head, 0.9f, store, cb);
                lastFx = now;
            }
            return false;
        }

        private void touch(Ref<EntityStore> caster, Vector3d head, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, head, HIT_RADIUS + 1, store)) {
                if (hit.contains(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || new Vector3d(p).add(0, 1, 0).distance(head) > HIT_RADIUS) continue;
                hit.add(e);
                SpellFx.damage(caster, e, "Physical", DAMAGE, new Vector3d(dir).mul(5).add(0, 3, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), 1.3f, store, cb);
                SpellFx.fx(FX_GOLD, new Vector3d(p).add(0, 1.1, 0), 1.2f, store, cb);
                SpellFx.sound(SFX_HIT, p, cb);
                Tenryumaru.energy(caster, 2f, store);
            }
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { body.clear(cb); }

        @Override public void abort() { body.abort(); }
    }

    // ===================== Signature : Eclipse du Dieu-Dragon =====================

    /** Lance l'Eclipse (LuoIaigiri, Kinryu-O en main) sur le point vise. */
    static RuneSpell.Instance eclipse(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d feet = SpellFx.position(caster, store);
        if (feet == null) return null;
        Vector3d aim = SpellFx.targetPoint(caster, store, Eclipse.REACH, 8);
        Vector3d center = aim != null ? SpellFx.ground(SpellFx.world(store), aim) : new Vector3d(feet);
        if (center == null) center = new Vector3d(feet);
        TenryumaruSystem.drawn(caster);
        SpellFx.protectFromFall(caster, 4_000, store);
        return new Eclipse(center, System.currentTimeMillis());
    }

    private static final class Eclipse implements RuneSpell.Instance {
        static final double REACH = 24, RADIUS = 11, SKY = 22, VIEW = 48;
        static final long DARK_MS = 700, DESCENT_MS = 1_100, WAVE_MS = 380, END_MS = DARK_MS + DESCENT_MS + 3 * WAVE_MS;
        static final float IMPACT = 320f, WAVE = 60f;
        static final int SEGMENTS = 10;

        final Vector3d center, sun;
        final long startAt;
        final Body body = new Body(SEGMENTS, 2.4f, 0.8f);
        boolean darkened, struck;
        int waves;
        long lastSun, lastFx;

        Eclipse(Vector3d center, long now) {
            this.center = center;
            this.sun = new Vector3d(center).add(0, SKY + 2, 0);
            this.startAt = now;
        }

        /** Point de la spirale du dragon, s de 0 (dans le ciel) a 1 (au sol, au centre de la zone). */
        Vector3d spiral(double s) {
            s = Math.max(0, Math.min(1, s));
            double angle = s * Math.PI * 2 * 1.75, radius = 1 + 8 * (1 - s);
            return new Vector3d(center).add(Math.cos(angle) * radius, 0.8 + SKY * (1 - s), Math.sin(angle) * radius);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            TenryumaruSystem.drawn(caster);
            long t = now - startAt;
            if (!darkened) {                                                              // le soleil est avale
                darkened = true;
                for (Ref<EntityStore> p : SpellFx.playersNear(center, VIEW, store)) SpellFx.effect(p, "Kinryu_Eclipse_Ciel", cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 2, store)) SpellFx.effect(e, "Kinryu_Eclipse_Effroi", cb);
                SpellFx.sound2dNear(SFX_BOOM, center, VIEW, store);
                SpellFx.fx(FX_RING, new Vector3d(center).add(0, 0.1, 0), 4.0f, store, cb);
            }
            if (t < DARK_MS + DESCENT_MS && now - lastSun >= 300) {                      // soleil noir au-dessus de la zone
                SpellFx.fx(FX_VOID, sun, 3.2f, store, cb);
                SpellFx.fx(FX_GOLD, sun, 1.4f, store, cb);
                lastSun = now;
            }
            if (t >= DARK_MS && !struck) {                                                // le dragon descend en spirale
                double s = Math.min(1, (t - DARK_MS) / (double) DESCENT_MS);
                s = s * s * (3 - 2 * s);
                List<Vector3d> points = new ArrayList<>();
                for (int i = 0; i <= SEGMENTS; i++) {
                    double si = s - i * 0.045;
                    if (si < 0) break;
                    points.add(spiral(si));
                }
                body.draw(points, store, cb);
                if (now - lastFx >= 80) { SpellFx.fx(FX_GOLD, points.get(0), 1.3f, store, cb); lastFx = now; }
                if (s >= 1) { struck = true; strike(caster, IMPACT, RADIUS, new Vector3d(0, 9, 0), store, cb); body.clear(cb); }
            }
            if (struck && waves < 2 && t >= DARK_MS + DESCENT_MS + (waves + 1) * WAVE_MS) {   // ondes de choc
                waves++;
                strike(caster, WAVE, RADIUS + 3 * waves, new Vector3d(0, 4, 0), store, cb);
            }
            return t >= END_MS;
        }

        private void strike(Ref<EntityStore> caster, float damage, double radius, Vector3d knock, Store<EntityStore> store,
                            CommandBuffer<EntityStore> cb) {
            SpellFx.sound2dNear(SFX_BOOM, center, VIEW, store);
            SpellFx.shake(center, radius + 10, "Impact_Strong", store);
            SpellFx.fx(FX_RING, new Vector3d(center).add(0, 0.1, 0), (float) (radius / 2.2), store, cb);
            for (int i = 0; i < 10; i++) {
                double a = i * Math.PI * 2 / 10;
                SpellFx.fx(FX_GOLD, new Vector3d(center).add(Math.cos(a) * radius * 0.6, 0.6, Math.sin(a) * radius * 0.6), 1.5f, store, cb);
            }
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, radius, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                SpellFx.damage(caster, e, "Physical", damage, knock, "Impact_Strong", cb);
                SpellFx.fx(FX_SLASH, new Vector3d(p).add(0, 1.1, 0), 1.4f, store, cb);
                SpellFx.sound(SFX_HIT, p, cb);
            }
        }

        /** Intouchable pendant l'Eclipse. */
        @Override public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) { return 0f; }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { body.clear(cb); }

        @Override public void abort() { body.abort(); }
    }
}
