package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Rune de la Couronne d'Autorite (Spatial, Unique, rune de predilection de l'Archonte Supreme) : 15 s de couronne doree
 * (modele Apollo_Laurel) au-dessus du lanceur. Aucun ennemi n'entre a moins de 6 blocs : il est repousse (10 b/s) et
 * subit 40 degats (une fois par seconde au plus) ; les projectiles qui volent vers le lanceur dans ce cercle sont detruits.
 * Chaque clic (3 au plus, 1 s d'intervalle) prononce un decret : 150 degats et ejection de tous les ennemis a 12 blocs.
 * Fiche de conception : scripts/runes/specs/Rune_CouronneAutorite.json
 */
public final class CouronneAutorite implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, CONTACT_EVERY_MS = 1_000, DECREE_EVERY_MS = 1_000;
    private static final double AURA = 6.0, DECREE_RADIUS = 12.0, CROWN_HEIGHT = 2.15, MIN_PROJECTILE_SPEED = 3.0;
    private static final double REPEL = 10.0, REPEL_LIFT = 3.0, EJECT = 16.0, EJECT_LIFT = 7.0;
    private static final int DECREES = 3, RING_POINTS = 24;
    private static final float CONTACT = 40f, DECREE = 150f;

    // ===== Assets =====
    private static final String CROWN = "Projectile_Config_Apollo_Laurel";
    private static final String FX_SPARK = "Bifrost_Divine";
    private static final String SFX_DECREE = "SFX_Portal_Neutral";

    @Override public String id() { return "CouronneAutorite"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Ref<EntityStore> crown = SpellFx.spawnModel(caster, CROWN, new Vector3d(pos).add(0, CROWN_HEIGHT, 0), new Vector3d(1, 0, 0), cb);
        SpellFx.notSaved(crown, store, cb);
        SpellFx.fx(FX_SPARK, new Vector3d(pos).add(0, CROWN_HEIGHT, 0), 1.5f, store, cb);
        return new Reign(caster, crown, System.currentTimeMillis());
    }

    private static final class Reign implements Instance {
        final Ref<EntityStore> caster, crown;
        final long startAt;
        final Map<Ref<EntityStore>, Long> lastContact = new HashMap<>();
        volatile boolean decreeRequested;
        int decrees;
        long lastDecree;

        Reign(Ref<EntityStore> caster, Ref<EntityStore> crown, long startAt) { this.caster = caster; this.crown = crown; this.startAt = startAt; }

        @Override public boolean listensClicks() { return true; }

        @Override
        public void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> casterRef, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            decreeRequested = true;                                               // applique dans tick
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= DURATION_MS) return true;
            SpellFx.hold(crown, new Vector3d(pos).add(0, CROWN_HEIGHT, 0), null, store);
            if (decreeRequested) {
                decreeRequested = false;
                if (decrees < DECREES && now - lastDecree >= DECREE_EVERY_MS) { lastDecree = now; decrees++; decree(pos, store, cb); }
            }
            repel(pos, now, store, cb);
            annul(pos, store, cb);
            return false;
        }

        /** Nul n'approche : les ennemis entres dans le cercle en sont chasses. */
        private void repel(Vector3d pos, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, AURA, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, pos) > AURA) continue;
                Vector3d out = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
                if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                out.normalize().mul(REPEL).add(0, REPEL_LIFT, 0);
                Long last = lastContact.get(e);
                if (last == null || now - last >= CONTACT_EVERY_MS) {
                    lastContact.put(e, now);
                    SpellFx.damage(caster, e, "Crush", CONTACT, out, null, cb);
                    SpellFx.fx(FX_SPARK, new Vector3d(p).add(0, 1, 0), 0.5f, store, cb);
                } else {
                    SpellFx.push(e, out, false, cb);
                }
            }
        }

        /** Les projectiles qui volent vers le lanceur s'eteignent au bord de la couronne. */
        private void annul(Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d center = new Vector3d(pos).add(0, 1, 0);
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(center, AURA, store)) {
                if (store.getComponent(ref, Projectile.getComponentType()) == null) continue;
                Velocity v = store.getComponent(ref, Velocity.getComponentType());
                Vector3d p = SpellFx.position(ref, store);
                if (v == null || p == null || v.getSpeed() < MIN_PROJECTILE_SPEED) continue;
                if (new Vector3d(v.getX(), v.getY(), v.getZ()).dot(new Vector3d(center).sub(p)) <= 0) continue;   // tirs du lanceur : ignores
                SpellFx.remove(ref, cb);
                SpellFx.fx(FX_SPARK, p, 0.6f, store, cb);
            }
        }

        /** Decret : tout ennemi a 12 blocs est frappe et chasse. */
        private void decree(Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, DECREE_RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
                if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                SpellFx.damage(caster, e, "Crush", DECREE, out.normalize().mul(EJECT).add(0, EJECT_LIFT, 0), "Impact_Strong", cb);
            }
            for (int i = 0; i < RING_POINTS; i++) {
                double a = i * 2 * Math.PI / RING_POINTS;
                SpellFx.fx(FX_SPARK, new Vector3d(pos).add(Math.cos(a) * DECREE_RADIUS, 0.5, Math.sin(a) * DECREE_RADIUS), 0.6f, store, cb);
            }
            SpellFx.fx(FX_SPARK, new Vector3d(pos).add(0, CROWN_HEIGHT, 0), 2.0f, store, cb);
            SpellFx.sound(SFX_DECREE, pos, cb);
            SpellFx.shake(pos, DECREE_RADIUS + 8, "Impact_Strong", store);
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { SpellFx.remove(crown, cb); }

        @Override public void abort() { RuneSpellRuntime.removeLater(crown); }
    }
}
