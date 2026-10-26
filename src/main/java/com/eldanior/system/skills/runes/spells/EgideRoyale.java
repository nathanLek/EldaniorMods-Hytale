package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune de l'Egide Royale (Lumiere, Rare) : dome de lumiere fixe de 6 blocs pendant 6 s. Les projectiles qui se dirigent
 * vers l'interieur sont detruits (le tireur n'est pas connu : tout projectile entrant), les ennemis qui le franchissent
 * sont repousses, et les allies a l'interieur subissent 40 % de degats en moins tant qu'ils y restent.
 * Fiche de conception : scripts/runes/specs/Rune_EgideRoyale.json
 */
public final class EgideRoyale implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, WALL_EVERY_MS = 500, REPEL_EVERY_MS = 250, ALLY_EVERY_MS = 500;
    private static final double RADIUS = 6.0, PUSH = 12.0, LIFT = 3.0, MIN_PROJECTILE_SPEED = 2.0;
    private static final float REPEL_DAMAGE = 10f;
    private static final int WALL_POINTS = 16;

    // ===== Assets =====
    private static final String AEGIS = "Elda_Egide";
    private static final String FX_WALL = "Bifrost_Divine", FX_BLOCK = "Taranis_Electric_Burst";
    private static final String SFX_RAISE = "SFX_Portal_Void", SFX_BLOCK = "SFX_Glass_Break";

    @Override public String id() { return "EgideRoyale"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.sound(SFX_RAISE, pos, cb);
        return new Dome(pos, System.currentTimeMillis());
    }

    private static final class Dome implements Instance {
        final Vector3d center;
        final long startAt;
        final Set<Ref<EntityStore>> shielded = new HashSet<>();
        long lastWall = Long.MIN_VALUE / 2, lastRepel, lastAlly = Long.MIN_VALUE / 2;

        Dome(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastWall >= WALL_EVERY_MS) { lastWall = now; wall(store, cb); }
            blockProjectiles(store, cb);
            if (now - lastRepel >= REPEL_EVERY_MS) { lastRepel = now; repel(caster, store, cb); }
            if (now - lastAlly >= ALLY_EVERY_MS) { lastAlly = now; shield(caster, now, store, cb); }
            return false;
        }

        /** Paroi du dome : anneaux de lumiere a plusieurs hauteurs. */
        private void wall(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (double h : new double[]{0.3, 2.0, 3.8, 5.2}) {
                double r = Math.sqrt(Math.max(0, RADIUS * RADIUS - h * h));
                int n = Math.max(4, (int) (WALL_POINTS * r / RADIUS));
                for (int i = 0; i < n; i++) {
                    double a = Math.PI * 2 * i / n + h;
                    SpellFx.fx(FX_WALL, new Vector3d(center).add(Math.cos(a) * r, h, Math.sin(a) * r), 0.35f, store, cb);
                }
            }
        }

        /** Detruit les projectiles en vol qui entrent dans le dome (les porteurs immobiles des sorts sont ignores). */
        private void blockProjectiles(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(center, RADIUS + 2, store)) {
                if (store.getComponent(ref, Projectile.getComponentType()) == null) continue;
                Velocity v = store.getComponent(ref, Velocity.getComponentType());
                Vector3d p = SpellFx.position(ref, store);
                if (v == null || p == null || v.getSpeed() < MIN_PROJECTILE_SPEED) continue;
                Vector3d toCenter = new Vector3d(center).sub(p);
                if (toCenter.length() > RADIUS + 1 || v.getX() * toCenter.x + v.getY() * toCenter.y + v.getZ() * toCenter.z <= 0) continue;
                SpellFx.fx(FX_BLOCK, p, 0.5f, store, cb);
                SpellFx.sound(SFX_BLOCK, p, cb);
                SpellFx.remove(ref, cb);
            }
        }

        private void repel(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - center.x, 0, p.z - center.z);
                if (out.lengthSquared() < 0.25) out.set(1, 0, 0);
                SpellFx.damage(caster, e, "Elemental", REPEL_DAMAGE, out.normalize().mul(PUSH).add(0, LIFT, 0), null, cb);
            }
        }

        /** Protection appliquee une fois a l'entree (duree = temps restant), retiree a la sortie : pas de clignotement. */
        private void shield(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Set<Ref<EntityStore>> inside = new HashSet<>();
            Vector3d cp = SpellFx.position(caster, store);
            if (cp != null && cp.distance(center) <= RADIUS) inside.add(caster);
            for (Ref<EntityStore> a : SpellFx.alliesAround(caster, center, RADIUS, store)) inside.add(a);
            float left = Math.max(0.1f, (DURATION_MS - (now - startAt)) / 1000f);
            for (Ref<EntityStore> a : inside) {
                if (shielded.add(a)) EffectsManager.applyCustomEffect(a, AEGIS, left, OverlapBehavior.OVERWRITE, cb);
            }
            shielded.removeIf(a -> {
                if (inside.contains(a)) return false;
                remove(a, cb);
                return true;
            });
        }

        private static void remove(Ref<EntityStore> a, CommandBuffer<EntityStore> cb) {
            if (!a.isValid()) return;
            try { EffectsManager.removeEffect(a, AEGIS, cb); } catch (Exception e) { EldaniorLogger.error("EgideRoyale: effet", e); }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> a : shielded) remove(a, cb);
            shielded.clear();
        }
    }
}
