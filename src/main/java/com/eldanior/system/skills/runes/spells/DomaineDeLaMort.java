package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune du Domaine de la Mort (Tenebres, Unique) : domaine fixe de 15 blocs pendant 15 s. Les ennemis dedans se fanent
 * (degats et ralentissement chaque seconde) ; chaque ennemi qui meurt dans le domaine rend 10 % de PV au lanceur.
 * Fiche de conception : scripts/runes/specs/Rune_DomaineDeLaMort.json
 */
public final class DomaineDeLaMort implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, WITHER_EVERY_MS = 1_000, BORDER_EVERY_MS = 800, MIST_EVERY_MS = 3_000;
    private static final double RADIUS = 15.0, HEIGHT = 8.0;
    private static final float DAMAGE = 20f, HEAL_PER_KILL = 0.10f;
    private static final int BORDER_POINTS = 24;

    // ===== Assets =====
    private static final String SLOW = "Elda_Givre";
    private static final String FX_BORDER = "Nyx_Void", FX_MIST = "Nyx_Mist", FX_WITHER = "Nyx_Drain_Burst";
    private static final String SFX_OPEN = "SFX_Bear_Voidtaken_Roar";

    @Override public String id() { return "DomaineDeLaMort"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.sound2dNear(SFX_OPEN, pos, RADIUS * 2, store);
        SpellFx.shake(pos, RADIUS, "Impact_Light", store);
        return new Domain(pos, System.currentTimeMillis());
    }

    private static final class Domain implements Instance {
        final Vector3d center;
        final long startAt;
        final Set<Ref<EntityStore>> marked = new HashSet<>();      // ennemis frappes dans le domaine (pour reconnaitre leur mort)
        final Set<Ref<EntityStore>> reaped = new HashSet<>();
        long lastWither, lastBorder, lastMist = Long.MIN_VALUE / 2;

        Domain(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastBorder >= BORDER_EVERY_MS) {
                lastBorder = now;
                for (int i = 0; i < BORDER_POINTS; i++) {
                    double a = Math.PI * 2 * i / BORDER_POINTS;
                    SpellFx.fx(FX_BORDER, new Vector3d(center).add(Math.cos(a) * RADIUS, 0.2, Math.sin(a) * RADIUS), 0.6f, store, cb);
                }
            }
            if (now - lastMist >= MIST_EVERY_MS) {
                lastMist = now;
                for (double dx = -RADIUS + 3; dx <= RADIUS - 3; dx += 5)
                    for (double dz = -RADIUS + 3; dz <= RADIUS - 3; dz += 5)
                        if (dx * dx + dz * dz <= RADIUS * RADIUS) SpellFx.fx(FX_MIST, new Vector3d(center).add(dx, 0.1, dz), 0.8f, store, cb);
            }
            reap(caster, store, cb);
            if (now - lastWither < WITHER_EVERY_MS) return false;
            lastWither = now;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS || p.y < center.y - 4 || p.y > center.y + HEIGHT) continue;
                marked.add(e);
                SpellFx.damage(caster, e, "Poison", DAMAGE, null, null, cb);
                SpellFx.effect(e, SLOW, cb);
                SpellFx.fx(FX_WITHER, p.add(0, 1, 0), 0.5f, store, cb);
            }
            return false;
        }

        /** Chaque ennemi touche par le domaine qui meurt rend des PV au lanceur. */
        private void reap(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            marked.removeIf(e -> {
                if (!e.isValid()) return true;
                if (store.getComponent(e, DeathComponent.getComponentType()) == null) return false;
                if (reaped.add(e)) SpellFx.restore(caster, DefaultEntityStatTypes.getHealth(), HEAL_PER_KILL, cb);
                return true;
            });
        }
    }
}
