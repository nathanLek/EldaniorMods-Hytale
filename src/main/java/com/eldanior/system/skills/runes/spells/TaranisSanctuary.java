package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.core.entity.entities.Player;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sanctuaire de Taranis — sort divin de foudre (Rune_SanctuaireTaranis, ex-"Sanctuaire de Taranis").
 *
 * Montee de l'orage pendant {@link #BUILDUP_MS} : la roue de Taranis (dieu celte de l'orage) tourne dans le ciel sous
 * trois enormes nuages noirs en volume, pluie battante, brouillard d'orage au sol, tonnerre (flashs dans les nuages),
 * vent et eclairs secs. Puis la tempete s'abat pendant {@link #DURATION_MS} :
 *  - chaque seconde, TOUTES les entites ennemies de la zone sont foudroyees (degats + paralysie breve) ;
 *  - des eclairs frappent le sol au hasard ; s'ils touchent un ennemi, la foudre rebondit en chaine sur les ennemis proches ;
 *  - chaque coup ajoute une charge electrique : a {@link #SURCHARGE_AT} charges, l'ennemi est frappe par une surcharge ;
 *  - le lanceur et ses allies dans la zone sont benis (vitesse +25 %).
 * A la fin, un coup de tonnerre colossal frappe le centre et projette tout autour.
 * Le lanceur, son groupe, sa guilde et sa famille sont epargnes.
 */
public final class TaranisSanctuary {

    private TaranisSanctuary() {}

    // ===== Reglages =====
    private static final double TARGET_RANGE = 40.0;
    private static final double FALLBACK_DISTANCE = 15.0;
    static final double RADIUS = SpellFx.DIVINE_ZONE_RADIUS;   // rayon de la tempete (meme zone que la Porte de Niflheim)
    private static final long BUILDUP_MS = 3_500;          // montee de l'orage avant les premiers degats
    private static final long DURATION_MS = 15_000;        // tempete active (apres la montee)
    private static final long DRY_BOLT_EVERY_MS = 700;     // eclairs secs pendant la montee
    private static final long STRIKE_ALL_EVERY_MS = 1000; // chaque seconde : toutes les entites de la zone sont foudroyees
    private static final long RANDOM_BOLT_EVERY_MS = 110; // eclairs d'ambiance au sol (zone de 35 blocs)
    static final float ENTITY_DAMAGE = 350f;              // par foudroiement (15 en 15 s)
    static final float BOLT_DAMAGE = 80f;                 // eclair d'ambiance, s'il touche quelqu'un
    private static final double BOLT_RADIUS = 2.5;
    private static final double STORM_HEIGHT = 36.0;      // hauteur du cercle d'orage
    private static final double CLOUDS_LOW = 42.0, CLOUDS_HIGH = 50.0, CLOUDS_TOP = 60.0; // nuages en volume (au-dessus du cercle d'orage)
    private static final double RAIN_SPACING = 14.0;      // grille d'averses sur toute la zone
    private static final double FOG_SPACING = 6.0;        // grille de taches de brouillard
    private static final double FOG_RADIUS = RADIUS + 5;  // le brouillard deborde de la zone de 5 blocs
    private static final double THUNDER_HEARING = 70.0;   // rayon dans lequel les joueurs entendent le tonnerre
    private static final long WIND_EVERY_MS = 3_000;
    // Eclairs en chaine
    private static final int CHAIN_JUMPS = 3;
    private static final double CHAIN_RANGE = 7.0;
    private static final float CHAIN_FALLOFF = 0.6f;      // degats conserves a chaque rebond
    private static final float CHAIN_BASE_DAMAGE = 200f;
    // Charges electriques
    static final int SURCHARGE_AT = 5;
    private static final float SURCHARGE_DAMAGE = 500f;
    // Coup de tonnerre final
    private static final float FINAL_DAMAGE = 800f;
    private static final double FINAL_PUSH = 14.0, FINAL_LIFT = 8.0;

    // ===== Assets =====
    private static final String STORM_CIRCLE_PROJECTILE = "Projectile_Config_Taranis_Storm_Circle";
    private static final String FX_BOLT = "Taranis_Bolt";
    private static final String FX_ZONE = "Taranis_Zone";
    private static final String FX_STRIKE = "Taranis_Electric_Burst";   // version courte (le Hazard_Electric_Stun vanilla boucle a l'infini)
    private static final String CLOUDS_HIGH_PROJECTILE = "Projectile_Config_Taranis_Storm_Clouds_High";
    private static final String CLOUDS_LOW_PROJECTILE = "Projectile_Config_Taranis_Storm_Clouds_Low";
    private static final String CLOUDS_TOP_PROJECTILE = "Projectile_Config_Taranis_Storm_Clouds_Top";
    private static final String FX_FOG = "Taranis_Fog";                 // brouillard d'orage au sol (duree finie)
    private static final String STUN_EFFECT = "Taranis_Foudroye";
    private static final String SURCHARGE_EFFECT = "Taranis_Surcharge";
    private static final String BLESSING_EFFECT = "Taranis_Benediction";
    private static final String FX_RAIN = "Taranis_Rain";               // pluie battante (duree finie)
    private static final String FX_SKY_FLASH = "Taranis_Sky_Flash";     // flash dans les nuages
    // Uniquement des sons d'orage : tonnerre (stereo -> joue en 2D aux joueurs proches), crepitements de foudre et vent (mono, positionnes)
    private static final String SFX_THUNDER = "SFX_Global_Weather_Thunder";
    private static final String SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap";
    private static final String SFX_SPARKS = "SFX_Emit_Goblin_Electric_Sparks";
    private static final String SFX_WIND = "SFX_Emit_Wind_Gusts";

    static final class Storm {
        final Vector3d center;
        final long startAt;
        long lastStrikeAll, lastBolt, lastWind;
        final List<Ref<EntityStore>> skyModels = new java.util.ArrayList<>(); // roue de Taranis + nuages
        final Map<Ref<EntityStore>, Integer> charges = new java.util.HashMap<>();
        boolean opened;
        Storm(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; this.lastStrikeAll = startAt; }
    }

    private static final Map<UUID, List<Storm>> storms = new ConcurrentHashMap<>();

    /** Declenche la tempete sur le point vise. Appele depuis l'interaction (thread du monde). */
    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return;
        Vector3d center = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK_DISTANCE);
        storms.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(new Storm(center, System.currentTimeMillis()));
    }

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Storm> list = storms.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Storm s : list) {
            if (tickStorm(caster, s, now, store, cb)) list.remove(s);
        }
        if (list.isEmpty()) storms.remove(uuid);
    }

    /** Retourne true quand la tempete est terminee. */
    private static boolean tickStorm(Ref<EntityStore> caster, Storm s, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (!s.opened) {
            s.opened = true;
            open(caster, s, store, cb);
        }
        long elapsed = now - s.startAt;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        if (now - s.lastWind >= WIND_EVERY_MS) {
            s.lastWind = now;
            SpellFx.sound(SFX_WIND, randomPoint(s, rng, store), cb);
        }
        if (elapsed < BUILDUP_MS) {
            // Montee de l'orage : eclairs secs, sans degats
            if (now - s.lastBolt >= DRY_BOLT_EVERY_MS) {
                s.lastBolt = now;
                bolt(randomPoint(s, rng, store), s, store, cb, rng.nextBoolean());
            }
            if (now - s.lastStrikeAll >= STRIKE_ALL_EVERY_MS) {
                s.lastStrikeAll = now;
                bless(caster, s, store, cb);
            }
            return false;
        }
        if (elapsed >= BUILDUP_MS + DURATION_MS) {
            finalThunder(caster, s, store, cb);
            for (Ref<EntityStore> model : s.skyModels) if (model != null && model.isValid()) cb.tryRemoveEntity(model, RemoveReason.REMOVE);
            return true;
        }
        // Eclairs d'ambiance : frappent le sol au hasard dans la zone
        if (now - s.lastBolt >= RANDOM_BOLT_EVERY_MS) {
            s.lastBolt = now;
            Vector3d at = randomPoint(s, rng, store);
            bolt(at, s, store, cb, rng.nextInt(3) == 0);
            for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, at, BOLT_RADIUS, store)) {
                SpellFx.damage(caster, target, "Lightning", BOLT_DAMAGE, null, null, cb);
                charge(caster, s, target, store, cb);
                chain(caster, s, target, store, cb);
            }
        }

        // Chaque seconde : toutes les entites ennemies de la zone sont foudroyees
        if (now - s.lastStrikeAll >= STRIKE_ALL_EVERY_MS) {
            s.lastStrikeAll = now;
            for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, s.center, RADIUS + 2, store)) {
                Vector3d pos = SpellFx.position(target, store);
                if (pos == null || horizontalDistance(pos, s.center) > RADIUS) continue;
                bolt(pos, s, store, cb, false);
                SpellFx.fx(FX_STRIKE, new Vector3d(pos).add(0, 1, 0), 1.0f, store, cb);
                SpellFx.damage(caster, target, "Lightning", ENTITY_DAMAGE, null, "Impact_Light", cb);
                SpellFx.effect(target, STUN_EFFECT, cb);
                charge(caster, s, target, store, cb);
            }
            bless(caster, s, store, cb);
        }
        return false;
    }

    /** Eclair en chaine : la foudre rebondit de la cible vers les ennemis les plus proches (arc visible). */
    private static void chain(Ref<EntityStore> caster, Storm s, Ref<EntityStore> first, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        java.util.Set<Ref<EntityStore>> hit = new java.util.HashSet<>();
        hit.add(first);
        Ref<EntityStore> from = first;
        float damage = CHAIN_BASE_DAMAGE;
        for (int jump = 0; jump < CHAIN_JUMPS; jump++) {
            Vector3d a = SpellFx.position(from, store);
            if (a == null) return;
            Ref<EntityStore> next = null;
            double best = CHAIN_RANGE * CHAIN_RANGE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, a, CHAIN_RANGE, store)) {
                if (hit.contains(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p != null && p.distanceSquared(a) < best) { best = p.distanceSquared(a); next = e; }
            }
            if (next == null) return;
            Vector3d b = SpellFx.position(next, store);
            arc(new Vector3d(a).add(0, 1, 0), new Vector3d(b).add(0, 1, 0), store, cb);
            SpellFx.damage(caster, next, "Lightning", damage, null, null, cb);
            SpellFx.effect(next, STUN_EFFECT, cb);
            SpellFx.sound(SFX_ZAP, b, cb);
            charge(caster, s, next, store, cb);
            hit.add(next);
            from = next;
            damage *= CHAIN_FALLOFF;
        }
    }

    /** Arc electrique entre deux points : eclats electriques tous les ~1.2 blocs. */
    private static void arc(Vector3d a, Vector3d b, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        int steps = (int) Math.min(10, Math.max(2, a.distance(b) / 1.2));
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i <= steps; i++) {
            Vector3d p = new Vector3d(a).lerp(b, i / (double) steps);
            if (i > 0 && i < steps) p.add(rng.nextDouble(-0.3, 0.3), rng.nextDouble(-0.3, 0.3), rng.nextDouble(-0.3, 0.3));
            SpellFx.fx(FX_STRIKE, p, 0.45f, store, cb);
        }
    }

    /** Ajoute une charge electrique ; a SURCHARGE_AT charges : surcharge (eclair dedie, gros degats, paralysie longue). */
    private static void charge(Ref<EntityStore> caster, Storm s, Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        int c = s.charges.merge(target, 1, Integer::sum);
        if (c < SURCHARGE_AT) return;
        s.charges.remove(target);
        Vector3d pos = SpellFx.position(target, store);
        if (pos == null) return;
        SpellFx.fx(FX_BOLT, pos, 2.0f, store, cb);
        SpellFx.fx(FX_STRIKE, new Vector3d(pos).add(0, 1, 0), 2.5f, store, cb);
        skyFlash(s, store, cb);
        SpellFx.sound2dNear(SFX_THUNDER, s.center, THUNDER_HEARING, store);
        SpellFx.damage(caster, target, "Lightning", SURCHARGE_DAMAGE, null, "Impact_Strong", cb);
        SpellFx.effect(target, SURCHARGE_EFFECT, cb);
    }

    /** Sanctuaire : le lanceur et ses allies (groupe, guilde, famille) presents dans la zone sont benis. */
    private static void bless(Ref<EntityStore> caster, Storm s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(s.center, RADIUS + 3, store)) {
            if (ref == null || !ref.isValid() || store.getComponent(ref, Player.getComponentType()) == null) continue;
            if (!ref.equals(caster) && !SpellFx.isAlly(caster, ref, store)) continue;
            Vector3d p = SpellFx.position(ref, store);
            if (p != null && horizontalDistance(p, s.center) <= RADIUS) SpellFx.effect(ref, BLESSING_EFFECT, cb);
        }
    }

    /** Coup de tonnerre final : eclair colossal au centre, onde de choc qui projette et blesse tous les ennemis. */
    private static void finalThunder(Ref<EntityStore> caster, Storm s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_BOLT, s.center, 3.0f, store, cb);
        for (int k = 0; k < 6; k++) {
            double a = k * Math.PI / 3;
            SpellFx.fx(FX_BOLT, SpellFx.ground(SpellFx.world(store),
                    new Vector3d(s.center).add(Math.cos(a) * RADIUS * 0.6, 0, Math.sin(a) * RADIUS * 0.6)), 1.5f, store, cb);
        }
        SpellFx.fx(FX_STRIKE, new Vector3d(s.center).add(0, 1, 0), 5.0f, store, cb);
        skyFlash(s, store, cb);
        SpellFx.sound2dNear(SFX_THUNDER, s.center, THUNDER_HEARING, store);
        SpellFx.sound(SFX_SPARKS, s.center, cb);
        for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, s.center, RADIUS + 2, store)) {
            Vector3d pos = SpellFx.position(target, store);
            if (pos == null) continue;
            Vector3d push = new Vector3d(pos.x - s.center.x, 0, pos.z - s.center.z);
            if (push.lengthSquared() < 1e-4) push.set(1, 0, 0);
            push.normalize().mul(FINAL_PUSH).add(0, FINAL_LIFT, 0);
            SpellFx.damage(caster, target, "Lightning", FINAL_DAMAGE, push, "Impact_Strong", cb);
        }
    }

    private static void skyFlash(Storm s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_SKY_FLASH, new Vector3d(s.center).add(0, CLOUDS_LOW - 2, 0), 24f, store, cb);
    }

    private static void open(Ref<EntityStore> caster, Storm s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_ZONE, new Vector3d(s.center).add(0, 0.05, 0), (float) (RADIUS / 3.0), store, cb);
        // Brouillard : une tache tous les FOG_SPACING blocs, posee au sol, sur tout le disque de la tempete
        int steps = (int) Math.ceil(FOG_RADIUS / FOG_SPACING);
        for (int i = -steps; i <= steps; i++) {
            for (int j = -steps; j <= steps; j++) {
                double dx = i * FOG_SPACING, dz = j * FOG_SPACING;
                if (Math.hypot(dx, dz) > FOG_RADIUS) continue;
                Vector3d at = SpellFx.ground(SpellFx.world(store), new Vector3d(s.center).add(dx, 0, dz));
                SpellFx.fx(FX_FOG, at, 1.0f, store, cb);
            }
        }
        int rain = (int) Math.ceil(RADIUS / RAIN_SPACING);
        for (int i = -rain; i <= rain; i++) {
            for (int j = -rain; j <= rain; j++) {
                double dx = i * RAIN_SPACING, dz = j * RAIN_SPACING;
                if (Math.hypot(dx, dz) <= RADIUS) SpellFx.fx(FX_RAIN, new Vector3d(s.center).add(dx, 0, dz), 1.0f, store, cb);
            }
        }
        SpellFx.sound2dNear(SFX_THUNDER, s.center, THUNDER_HEARING, store);
        SpellFx.sound(SFX_WIND, s.center, cb);
        SpellFx.sound(SFX_SPARKS, s.center, cb);
        s.skyModels.add(spawnFixed(caster, STORM_CIRCLE_PROJECTILE, new Vector3d(s.center).add(0, STORM_HEIGHT, 0), cb));
        s.skyModels.add(spawnFixed(caster, CLOUDS_LOW_PROJECTILE, new Vector3d(s.center).add(0, CLOUDS_LOW, 0), cb));
        s.skyModels.add(spawnFixed(caster, CLOUDS_HIGH_PROJECTILE, new Vector3d(s.center).add(0, CLOUDS_HIGH, 0), cb));
        s.skyModels.add(spawnFixed(caster, CLOUDS_TOP_PROJECTILE, new Vector3d(s.center).add(0, CLOUDS_TOP, 0), cb));
    }

    private static Ref<EntityStore> spawnFixed(Ref<EntityStore> caster, String configId, Vector3d pos, CommandBuffer<EntityStore> cb) {
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(configId);
        if (config == null) {
            EldaniorLogger.error("TaranisSanctuary: projectile introuvable " + configId);
            return null;
        }
        try {
            return ProjectileModule.get().spawnProjectile(caster, cb, config, pos, new Vector3d(1, 0, 0));
        } catch (Exception e) {
            EldaniorLogger.error("TaranisSanctuary: " + configId, e);
            return null;
        }
    }

    private static void bolt(Vector3d ground, Storm s, Store<EntityStore> store, CommandBuffer<EntityStore> cb, boolean thunder) {
        SpellFx.fx(FX_BOLT, ground, 1.0f, store, cb);
        SpellFx.sound(SFX_ZAP, ground, cb);
        if (thunder) {
            SpellFx.sound2dNear(SFX_THUNDER, s.center, THUNDER_HEARING, store);
            skyFlash(s, store, cb);
        }
    }

    private static Vector3d randomPoint(Storm s, ThreadLocalRandom rng, Store<EntityStore> store) {
        double r = RADIUS * Math.sqrt(rng.nextDouble());
        double a = rng.nextDouble(Math.PI * 2);
        return SpellFx.ground(SpellFx.world(store), new Vector3d(s.center.x + Math.cos(a) * r, s.center.y, s.center.z + Math.sin(a) * r));
    }

    private static double horizontalDistance(Vector3d a, Vector3d b) {
        return Math.hypot(a.x - b.x, a.z - b.z);
    }

    public static void handleDisconnect(UUID uuid) { storms.remove(uuid); }
}
