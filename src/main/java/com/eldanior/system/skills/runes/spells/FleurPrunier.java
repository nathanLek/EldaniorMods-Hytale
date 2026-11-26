package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de la Fleur de Prunier (Combat, Unique, Saint Martial, voie martiale du Clan Luo) : 40 petales de Ki dores en 2 s
 * (un toutes les 50 ms) dans un cone de 10 blocs (60 degres) fixe au lancement. Chaque petale touche un ennemi du cone au
 * hasard (25 degats) ; un ennemi touche par 5 petales fleurit : la fleur de Ki explose sur lui (300, rayon 3), une fois.
 * Epargne groupe, guilde et famille.
 * Fiche de conception : scripts/runes/specs/Rune_FleurPrunier.json
 */
public final class FleurPrunier implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final int PETALS = 40, BLOOM_AT = 5;
    private static final long PETAL_MS = 50;
    private static final double RANGE = 10.0, HALF_ANGLE = Math.toRadians(30), BLOOM_RADIUS = 3.0;
    private static final float PETAL_DAMAGE = 25f, BLOOM_DAMAGE = 300f;

    // ===== Assets =====
    private static final String FX_PETAL = "Gilgamesh_Sword_Burst", FX_CUT = "Impact_Sword_Charged", FX_BLOOM = "Eldanior_Pas_Celeste";
    private static final String SFX_PETAL = "SFX_Ability_Wind_Strike_Slash", SFX_BLOOM = "SFX_Longsword_Special_Impact";

    @Override public String id() { return "FleurPrunier"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.forward(caster, store);
        dir.y = 0;
        if (dir.lengthSquared() < 1e-4) dir.set(0, 0, 1);
        return new Flurry(new Vector3d(pos), dir.normalize(), System.currentTimeMillis());
    }

    private static final class Flurry implements Instance {
        final Vector3d origin, dir;
        final Map<Ref<EntityStore>, Integer> count = new HashMap<>();
        final Set<Ref<EntityStore>> bloomed = new HashSet<>();
        long next;
        int thrown;

        Flurry(Vector3d origin, Vector3d dir, long now) { this.origin = origin; this.dir = dir; this.next = now; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            while (now >= next && thrown < PETALS) {
                next += PETAL_MS;
                thrown++;
                double a = (rng.nextDouble() * 2 - 1) * HALF_ANGLE, d = 1.5 + rng.nextDouble() * (RANGE - 1.5);   // petale perdu
                Vector3d side = new Vector3d(dir).rotateY(a).mul(d);
                SpellFx.fx(FX_PETAL, new Vector3d(origin).add(side).add(0, 0.6 + rng.nextDouble() * 1.4, 0), 0.35f, store, cb);
                List<Ref<EntityStore>> inCone = cone(caster, store);
                if (inCone.isEmpty()) continue;
                Ref<EntityStore> t = inCone.get(rng.nextInt(inCone.size()));
                Vector3d p = SpellFx.position(t, store);
                if (p == null) continue;
                Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
                SpellFx.fx(FX_PETAL, chest, 0.5f, store, cb);
                if (thrown % 3 == 0) SpellFx.sound(SFX_PETAL, chest, cb);
                SpellFx.damage(caster, t, "Physical", PETAL_DAMAGE, null, null, cb);
                int n = count.merge(t, 1, Integer::sum);
                if (n >= BLOOM_AT && bloomed.add(t)) bloom(caster, p, store, cb);
            }
            return thrown >= PETALS;
        }

        private List<Ref<EntityStore>> cone(Ref<EntityStore> caster, Store<EntityStore> store) {
            List<Ref<EntityStore>> out = new ArrayList<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, origin, RANGE, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d to = new Vector3d(p.x - origin.x, 0, p.z - origin.z);
                if (to.lengthSquared() < 1e-4 || to.normalize().angle(dir) <= HALF_ANGLE) out.add(e);
            }
            return out;
        }

        /** La fleur de Ki eclot sur la cible : explosion de 3 blocs. */
        private void bloom(Ref<EntityStore> caster, Vector3d p, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_BLOOM, new Vector3d(p).add(0, 0.2, 0), 2.2f, store, cb);
            SpellFx.fx(FX_PETAL, new Vector3d(p).add(0, 1.2, 0), 1.8f, store, cb);
            SpellFx.fx(FX_CUT, new Vector3d(p).add(0, 1.1, 0), 1.6f, store, cb);
            SpellFx.sound(SFX_BLOOM, p, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, BLOOM_RADIUS, store)) {
                Vector3d q = SpellFx.position(e, store);
                Vector3d away = q == null ? new Vector3d(0, 1, 0) : new Vector3d(q.x - p.x, 0, q.z - p.z);
                if (away.lengthSquared() < 1e-4) away.set(0, 0, 1);
                SpellFx.damage(caster, e, "Physical", BLOOM_DAMAGE, away.normalize().mul(5).add(0, 3, 0), "Impact_Strong", cb);
            }
        }
    }
}
