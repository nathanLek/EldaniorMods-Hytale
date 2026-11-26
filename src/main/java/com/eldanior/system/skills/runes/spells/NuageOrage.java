package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune du Nuage d'Orage (Foudre, Epic, rune de predilection du Grand Archimage) : un nuage d'orage (modele du Sanctuaire de
 * Taranis ramene a l'echelle 12) se forme 12 blocs au-dessus du point vise (20 blocs) pendant 10 s. Toutes les 0,5 s, un
 * eclair frappe un ennemi au hasard sous le nuage (rayon 7) : 70 degats et 0,5 s d'etourdissement a 1,5 bloc autour ;
 * sans ennemi, il tombe au hasard dans la zone.
 * Fiche de conception : scripts/runes/specs/Rune_NuageOrage.json
 */
public final class NuageOrage implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, STRIKE_EVERY_MS = 500, ZONE_EVERY_MS = 100, STUN_MS = 500;
    private static final double RANGE = 20.0, RADIUS = 7.0, CLOUD_HEIGHT = 12.0, BOLT_RADIUS = 1.5, CLOUD_HITBOX = 8.0;
    private static final float DAMAGE = 70f, CLOUD_SCALE = 12f;

    // ===== Assets =====
    private static final String CLOUD = "Projectile_Config_Taranis_Storm_Clouds_Low", CLOUD_MODEL = "Taranis_Storm_Clouds_Low", STUN = "Taranis_Foudroye";
    private static final String FX_BOLT = "Taranis_Bolt", FX_STRIKE = "Taranis_Electric_Burst", FX_FLASH = "Taranis_Sky_Flash", FX_ZONE = "Taranis_Zone";
    private static final String SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap";

    @Override public String id() { return "NuageOrage"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        Ref<EntityStore> cloud = SpellFx.spawnModel(caster, CLOUD, new Vector3d(center).add(0, CLOUD_HEIGHT, 0), new Vector3d(1, 0, 0), cb);
        SpellFx.notSaved(cloud, store, cb);
        return new Storm(center, cloud, SpellFx.world(store), System.currentTimeMillis());
    }

    private static final class Storm implements Instance {
        final Vector3d center;
        final Ref<EntityStore> cloud;
        final World world;
        final long startAt;
        long lastStrike, lastZone;
        boolean scaled;

        Storm(Vector3d center, Ref<EntityStore> cloud, World world, long startAt) {
            this.center = center; this.cloud = cloud; this.world = world; this.startAt = startAt; this.lastStrike = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null || now - startAt >= DURATION_MS) return true;
            if (cloud != null && cloud.isValid()) {
                if (!scaled) { scaled = true; SpellFx.setModelScale(cloud, CLOUD_MODEL, CLOUD_SCALE, CLOUD_HITBOX, cb); }
                SpellFx.hold(cloud, new Vector3d(center).add(0, CLOUD_HEIGHT, 0), null, store);
            }
            if (now - lastZone >= ZONE_EVERY_MS) { lastZone = now; SpellFx.fx(FX_ZONE, new Vector3d(center).add(0, 0.1, 0), (float) (RADIUS / 3.0), store, cb); }
            if (now - lastStrike >= STRIKE_EVERY_MS) { lastStrike = now; strike(caster, store, cb); }
            return false;
        }

        /** Un eclair : sur un ennemi au hasard sous le nuage, sinon en un point au hasard de la zone. */
        private void strike(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            List<Ref<EntityStore>> under = SpellFx.enemiesAround(caster, center, RADIUS + 2, store);
            under.removeIf(e -> { Vector3d p = SpellFx.position(e, store); return p == null || SpellFx.horizontalDistance(p, center) > RADIUS; });
            Vector3d ground;
            if (!under.isEmpty()) ground = SpellFx.position(under.get(rng.nextInt(under.size())), store);
            else {
                double a = rng.nextDouble(Math.PI * 2), r = RADIUS * Math.sqrt(rng.nextDouble());
                Vector3d p = new Vector3d(center.x + Math.cos(a) * r, center.y, center.z + Math.sin(a) * r);
                ground = world != null ? SpellFx.ground(world, p) : p;
            }
            if (ground == null) return;
            SpellFx.fx(FX_FLASH, new Vector3d(center).add(0, CLOUD_HEIGHT - 1, 0), 3.0f, store, cb);
            SpellFx.fx(FX_BOLT, ground, 0.5f, store, cb);                     // eclair de ~25 blocs a l'echelle 1 : 12 blocs, jusqu'au nuage
            SpellFx.fx(FX_STRIKE, new Vector3d(ground).add(0, 0.5, 0), 1.0f, store, cb);
            SpellFx.sound(SFX_ZAP, ground, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, ground, BOLT_RADIUS + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, ground) > BOLT_RADIUS) continue;
                SpellFx.damage(caster, e, "Lightning", DAMAGE, null, "Impact_Light", cb);
                SpellFx.stun(e, STUN, STUN_MS, store, cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { SpellFx.remove(cloud, cb); }

        @Override
        public void abort() { RuneSpellRuntime.removeLater(cloud); }
    }
}
