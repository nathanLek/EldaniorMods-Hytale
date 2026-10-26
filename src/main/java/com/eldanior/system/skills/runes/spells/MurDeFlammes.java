package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune du Mur de Flammes (Feu, Rare) : mur de feu de 10 blocs perpendiculaire au regard qui brule tout ennemi qui le
 * traverse ou s'en approche. Zone persistante (6 s), sans modification du terrain.
 * Fiche de conception : scripts/runes/specs/Rune_MurDeFlammes.json
 */
public final class MurDeFlammes implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, DAMAGE_EVERY_MS = 500, PILLAR_EVERY_MS = 3_000, SOUND_EVERY_MS = 2_000;
    private static final double TARGET_RANGE = 20.0, FALLBACK = 8.0;
    private static final double HALF_LENGTH = 5.0, SPACING = 1.25, THICKNESS = 1.5, HEIGHT = 3.0;
    private static final float DAMAGE = 10f;

    // ===== Assets =====
    private static final String BURN = "Elda_Brulure";
    private static final String FX_SPAWN = "Fire_AoE_Spawn", FX_PILLAR = "Muspelheim_Fire_Pillar", FX_HIT = "Impact_Fire";
    private static final String SFX_IGNITE = "SFX_Flame_Ignite", SFX_LOOP = "SFX_Flame_Default_Loop";

    @Override public String id() { return "MurDeFlammes"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        World world = SpellFx.world(store);
        Vector3d center = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK);
        Vector3d fwd = SpellFx.forward(caster, store);
        Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
        if (side.lengthSquared() < 1e-6) side.set(1, 0, 0);
        side.normalize();

        List<Vector3d> points = new ArrayList<>();
        for (double t = -HALF_LENGTH; t <= HALF_LENGTH + 1e-6; t += SPACING) {
            points.add(SpellFx.ground(world, new Vector3d(center).add(new Vector3d(side).mul(t))));
        }
        for (Vector3d p : points) SpellFx.fx(FX_SPAWN, new Vector3d(p).add(0, 0.1, 0), 0.5f, store, cb);
        SpellFx.sound(SFX_IGNITE, center, cb);
        return new Wall(center, side, points, System.currentTimeMillis());
    }

    private static final class Wall implements Instance {
        final Vector3d center, side;
        final List<Vector3d> points;
        final long startAt;
        long lastDamage, lastPillar = Long.MIN_VALUE / 2, lastSound;

        Wall(Vector3d center, Vector3d side, List<Vector3d> points, long startAt) {
            this.center = center; this.side = side; this.points = points; this.startAt = startAt;
            this.lastDamage = startAt - DAMAGE_EVERY_MS; this.lastSound = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastPillar >= PILLAR_EVERY_MS) {
                lastPillar = now;
                for (Vector3d p : points) SpellFx.fx(FX_PILLAR, p, 0.3f, store, cb);
            }
            if (now - lastSound >= SOUND_EVERY_MS) { lastSound = now; SpellFx.sound(SFX_LOOP, center, cb); }
            if (now - lastDamage >= DAMAGE_EVERY_MS) {
                lastDamage = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, HALF_LENGTH + THICKNESS + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || !touches(p)) continue;
                    SpellFx.damage(caster, e, "Fire", DAMAGE, null, null, cb);
                    SpellFx.effect(e, BURN, cb);
                    SpellFx.fx(FX_HIT, new Vector3d(p).add(0, 1, 0), 0.6f, store, cb);
                }
            }
            return false;
        }

        /** Distance au segment du mur (dans le plan) et hauteur par rapport au sol du mur. */
        private boolean touches(Vector3d p) {
            double dx = p.x - center.x, dz = p.z - center.z;
            double along = dx * side.x + dz * side.z;
            if (Math.abs(along) > HALF_LENGTH + 0.5) return false;
            double across = Math.abs(-dx * side.z + dz * side.x);
            return across <= THICKNESS && p.y >= center.y - 1.5 && p.y <= center.y + HEIGHT;
        }
    }
}
