package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.FlyMode;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Courroux de Muspelheim — sort ultime de feu (Rune_CourrouxMuspelheim).
 *
 * Chronologie (t = 0 au declenchement ; poussee vers le haut, animations et cercles des mains en JSON) :
 *   - LEVITATE_AT : le lanceur est fige en l'air (vol force, vitesses nulles) ;
 *   - SEED_AT     : fin de l'animation de lancement -> un projectile invisible part la ou il vise ;
 *   - impact de ce projectile (bloque dans un bloc ou disparu sur une creature, sans portee max) : un immense
 *     cercle magique rouge (modele 3D, diametre = zone du sort) s'ouvre SKY_HEIGHT blocs au-dessus du point d'impact ;
 *   - RELEASE_AT  : le lanceur redescend (immunite aux degats de chute) ;
 *   - une fois le lanceur au sol ET le cercle ouvert depuis RAIN_DELAY : les {@link #METEOR_COUNT} meteorites
 *     tombent une par une du cercle (zone rouge au sol pendant la chute, la meteorite grossit en approchant) ;
 *   - impact : explosion (degats instantanes + souffle qui propulse, EXPLOSION_RADIUS), puis vague de flammes du Gobliterator
 *     (Furnace Blast) qui s'etend jusqu'a DAMAGE_RADIUS : chaque ennemi atteint par le front subit des degats de
 *     brulure, la brulure celeste et un knockback qui le projette hors de la zone.
 * Mise en scene : ailes et couronne de feu pendant la levitation ; cercle runique 3D (anneaux contrarotatifs) au
 * milieu d'un ciel embrase (nuages de feu, pluie de cendres et de braises) ; une partie des meteorites est guidee sur
 * les ennemis ; chaque impact fait trembler la camera, brule le terrain (cratere, roche volcanique, arbres calcines,
 * restaures apres {@link #SCORCH_RESTORE_MS}) et laisse un sol ardent {@link #BURNING_MS} ; enfin le Meteore du
 * Jugement, trois fois plus gros, s'abat au centre. Sur une zone gelee par la Porte de Niflheim, l'impact vaporise la
 * glace (explosion de vapeur, degats supplementaires) au lieu de bruler le sol.
 * Le projectile "graine" est suivi par le code a chaque tick (pas d'interaction d'impact : lancee depuis le code,
 * une interaction s'executerait dans le contexte du lanceur). Les meteorites sont purement visuelles : le code les
 * retire a l'impact et applique lui-meme les degats (lanceur, groupe, guilde et famille epargnes).
 */
public final class MuspelheimWrath {

    private MuspelheimWrath() {}

    // ===== Reglages =====
    public static final int METEOR_COUNT = 20;
    private static final double FALLBACK_DISTANCE = 18.0;   // si le projectile graine n'a pas pu partir
    private static final double TARGET_RANGE = 40.0;        // idem : portee de visee de secours
    private static final double RAIN_RADIUS = SpellFx.DIVINE_ZONE_RADIUS;   // rayon de la zone de pluie (meme zone que la Porte de Niflheim)
    private static final double MIN_SPACING = 9.0;          // espacement mini entre impacts
    private static final double SKY_HEIGHT = 160.0;         // hauteur du cercle (modele 3D) au-dessus de la zone
    private static final double METEOR_ABOVE_CIRCLE = 20.0; // les meteorites apparaissent 20 blocs au-dessus du cercle et le traversent
    private static final double SPAWN_JITTER = 3.0;         // chaque meteorite part du cercle a +/- 3 blocs a la verticale de son impact
    private static final double LEVITATE_AT = 0.45;         // s : sommet de la poussee vers le haut (JSON)
    private static final double SEED_AT = 3.6;              // s : fin de l'animation de lancement (JSON)
    private static final double RELEASE_AT = 6.6;           // s : 3 s de vol apres le lancement (JSON), puis redescente
    private static final long SEED_TIMEOUT_MS = 10000;      // pas de portee max : securite si la graine ne touche jamais rien
    private static final long RAIN_DELAY_MS = 1000;         // le cercle s'ouvre, puis la pluie commence
    private static final long LANDING_MIN_MS = 250;         // attente mini avant de tester le sol
    private static final long LANDING_TIMEOUT_MS = 4000;    // la pluie demarre quoi qu'il arrive apres ce delai
    private static final double METEOR_INTERVAL = 0.6;      // s entre deux meteorites
    private static final double METEOR_JITTER = 0.1;
    private static final float GROWTH_START = 1.0f;         // echelle de la meteorite a l'apparition...
    private static final float GROWTH_END = 1.4f;           // ...et a l'impact (x15 -> x21)
    private static final long FALL_PROTECTION_MS = 4000;
    private static final double FX_VIEW_RADIUS = 200.0;     // rayon d'envoi des particules aux joueurs
    static final float DAMAGE = 1200f;                      // explosion : degats au centre (instantanes)
    static final double EXPLOSION_RADIUS = 8.0;             // rayon de l'explosion : degats + souffle qui propulse (= zone rouge)
    private static final float EDGE_DAMAGE_RATIO = 0.5f;    // degats de l'explosion au bord de son rayon
    static final double DAMAGE_RADIUS = 3.5;                // portee de la vague de flammes (degats + visuel)
    static final float WAVE_DAMAGE = 400f;                  // vague de flammes : degats de brulure a son passage
    private static final double WAVE_SPEED = 15.0;          // blocs/s : la vague atteint 3.5 blocs en 0.23 s
    private static final String BURN_EFFECT = "Muspelheim_Brulure";
    private static final double KNOCKBACK_FORCE = 15.0;     // vague : repousse hors de la zone (comme le Furnace Blast)...
    private static final double KNOCKBACK_UP = 5.0;         // ...avec un souleve vertical
    private static final double BLAST_FORCE = 22.0;         // souffle de l'explosion : propulsion au centre...
    private static final double BLAST_UP = 9.0;             // ...et souleve vertical (moitie au bord du rayon)
    // Meteore du Jugement (dernier, au centre)
    private static final float GIANT_SCALE = 3.0f;
    private static final long GIANT_DELAY_MS = 1500;        // apres la derniere meteorite
    private static final float GIANT_DAMAGE = 2500f;
    private static final double GIANT_RADIUS = 14.0;
    private static final double GIANT_BLAST = 30.0, GIANT_UP = 12.0;
    // Meteorites guidees sur les ennemis
    private static final double GUIDED_CHANCE = 0.4;
    // Tremblement de camera
    private static final double SHAKE_RADIUS = 45.0;
    // Terre brulee (restauree) et sol ardent
    private static final double SCORCH_RADIUS = 5.0, CRATER_RADIUS = 2.0;
    private static final double GIANT_SCORCH_RADIUS = 9.0, GIANT_CRATER_RADIUS = 4.0;
    static final long SCORCH_RESTORE_MS = 180_000;
    static final long BURNING_MS = 30_000;
    private static final double BURNING_RADIUS = 4.5;
    private static final float BURNING_DAMAGE = 3f;          // par seconde
    private static final String BURNING_EFFECT = "Muspelheim_Embrasement";
    // Combo Niflheim : vaporisation
    private static final float STEAM_DAMAGE = 500f;
    private static final double STEAM_RADIUS = 7.0;
    // Mise en scene
    private static final double CLOUDS_BELOW = 12.0, CLOUDS_ABOVE = 14.0, CLOUDS_TOP = 28.0; // nuages de feu autour du cercle
    private static final double VORTEX_BELOW = 3.0;                        // tourbillon de feu juste sous le cercle
    private static final double FIRE_PILLAR_CHANCE = 0.35;                 // colonnes de feu sur certains impacts
    private static final double ASH_SPACING = 15.0;
    private static final double WINGS_HEIGHT = 1.35, WINGS_BACK = 0.35, CROWN_HEIGHT = 2.25;
    private static final String[] SCORCHED_GROUND = {"Soil_Dirt_Burnt", "Soil_Dirt_Burnt", "Rock_Basalt", "Rock_Volcanic", "Rock_Volcanic_Cracked_Lava"};
    private static final String LAVA_ROCK = "Rock_Volcanic_Cracked_Lava";
    private static final String LAVA = "Lava_Source";                 // lave immobile (pas d'ecoulement), retiree a la restauration
    private static final double CRATER_LAVA_CHANCE = 0.8;            // cases du cratere remplies de lave
    private static final double SCATTER_LAVA_CHANCE = 0.07;          // flaques de lave dans la terre brulee
    /** Blocs poses par la Porte de Niflheim (fondus par une meteorite). */
    private static final String[] FROST_PLACED = {"Snow", "Ice", "Permafrost"};
    private static final String BURNT_LEAVES = "Plant_Leaves_Burnt";
    private static final String BURNT_TRUNK = "Wood_Burnt_Trunk";
    private static final String[] FOLIAGE_KEYWORDS = {"Plant", "Flower", "Fern", "Mushroom", "Bush", "Grass_Tall", "Weed", "Reed"};
    /** Blocs deja transformes par un sort (brules ou geles par Niflheim) : jamais retouches (restaurations independantes). */
    private static final String[] SPELL_BLOCK_KEYWORDS = {"Burnt", "Basalt", "Volcanic", "Snow", "Rock_Ice", "Rubble_Ice", "Wood_Ice", "Permafrost"};

    // ===== Assets (rayon de reference des particules vanilla Gobliterator : 3 blocs) =====
    private static final String SEED_PROJECTILE = "Projectile_Config_Muspelheim_Seed";
    private static final String SKY_CIRCLE_PROJECTILE = "Projectile_Config_Muspelheim_Sky_Circle"; // cercle en modele 3D
    private static final String METEOR_PROJECTILE = "Projectile_Config_Muspelheim_Meteor";
    private static final String FX_IMPACT_ZONE = "Muspelheim_Impact_Zone";
    private static final String FX_EXPLOSION = "Explosion_Big";
    private static final String FX_HEAT_BLAST = "Gobliterator_Furnace_Blast";      // vague de chaleur (Gobliterator phase 1)
    private static final double HEAT_BLAST_BASE_RADIUS = 4.3;                        // rayon vanilla du Furnace Blast
    private static final String FX_BLAST_BURST = "Gobliterator_Explosive_Barrel_Explosion";
    private static final String SFX_HEAT_BLAST = "SFX_Gobliterator_Furnace_Burst";
    private static final String CAMERA_IMPACT = "Impact_Strong";
    private static final String FX_WAVE_HIT = "Explosion_Big";                         // gerbe de feu sur chaque cible touchee
    private static final String SFX_WAVE_HIT = "SFX_Effect_Burn_World";
    private static final String FX_GROUND_FIRE = "Gobliterator_Rocket_AoE";
    private static final String FX_SMOKE = "Divine_Smoke_Short";       // Smoke_Black vanilla boucle a l'infini : version a duree finie
    private static final String SFX_SEED = "SFX_Staff_Flame_Fireball_Launch";
    private static final String SFX_CIRCLE = "SFX_Gobliterator_Arena_Horn";
    private static final String SFX_FALL = "SFX_Gobliterator_Rocket_Barrage_Projectile";
    private static final String SFX_IMPACT = "SFX_Gobliterator_Rocket_Barrage_Explosion";
    private static final String SFX_FINAL_IMPACT = "SFX_Gobliterator_Self_Destruct_Explosion";
    private static final String CLOUDS_LOW_PROJECTILE = "Projectile_Config_Muspelheim_Fire_Clouds_Low";
    private static final String CLOUDS_HIGH_PROJECTILE = "Projectile_Config_Muspelheim_Fire_Clouds_High";
    private static final String CLOUDS_TOP_PROJECTILE = "Projectile_Config_Muspelheim_Fire_Clouds_Top";
    private static final String VORTEX_PROJECTILE = "Projectile_Config_Muspelheim_Fire_Vortex";
    private static final String FX_FIRE_PILLAR = "Muspelheim_Fire_Pillar";
    private static final String WINGS_PROJECTILE = "Projectile_Config_Muspelheim_Wings";
    private static final String CROWN_PROJECTILE = "Projectile_Config_Muspelheim_Crown";
    private static final String FX_ASH_RAIN = "Muspelheim_Ash_Rain";
    private static final String FX_BURNING = "Muspelheim_Burning_Ground";
    private static final String FX_STEAM = "Muspelheim_Steam_Burst";
    private static final String FX_ICE_SHARDS = "Niflheim_Freeze_Burst";
    private static final String CAMERA_SHAKE = "Impact_Strong";

    /** Pluies en cours, par lanceur (plusieurs possibles : la recharge est plus courte que la pluie). */
    private static final Map<UUID, List<Rain>> activeRains = new ConcurrentHashMap<>();
    /** Fin de l'immunite aux degats de chute (ms), par lanceur. */
    private static final Map<UUID, Long> fallProtectedUntil = new ConcurrentHashMap<>();

    static final class Meteor {
        Vector3d impact, spawn;
        final long spawnAt;
        long impactAt;
        final boolean last, giant, guided;
        boolean spawned, exploded;
        Ref<EntityStore> projectile;

        Meteor(Vector3d impact, Vector3d spawn, long spawnAt, long impactAt, boolean last, boolean giant, boolean guided) {
            this.impact = impact; this.spawn = spawn;
            this.spawnAt = spawnAt; this.impactAt = impactAt; this.last = last; this.giant = giant; this.guided = guided;
        }
    }

    /** Point d'impact qui brule encore (sol ardent). */
    record Burning(Vector3d pos, long until) {}

    /** Vague de flammes partie d'un impact : touche chaque cible une seule fois quand le front l'atteint. */
    static final class Wave {
        final Vector3d center;
        final long startAt;
        final java.util.Set<Ref<EntityStore>> hit = new java.util.HashSet<>();
        Wave(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }
    }

    static final class Rain {
        final long levitateAt, seedAt, releaseAt;
        final List<Meteor> meteors = new ArrayList<>();
        final List<Wave> waves = new ArrayList<>();
        boolean levitating, seedLaunched, released, rainStarted;
        // Projectile graine
        Ref<EntityStore> seed;
        Vector3d seedOrigin, seedLastPos;
        long seedLaunchedAt;
        // Zone, connue a l'impact de la graine
        List<Vector3d> impactPoints;
        Vector3d circleCenter;
        long circleOpenedAt;
        Ref<EntityStore> circleEntity;
        final List<Ref<EntityStore>> skyModels = new ArrayList<>();   // nuages de feu
        Ref<EntityStore> wings, crown;
        final List<Burning> burning = new ArrayList<>();
        long lastBurnPulse;
        TerrainRestore.Restore scorch;                                 // terrain brule a restaurer
        double meteorSpeed = 40.0;
        // Etat de vol a restaurer a la fin
        FlyMode prevFly;
        float prevHorizontalFlySpeed, prevVerticalFlySpeed;

        Rain(long levitateAt, long seedAt, long releaseAt) {
            this.levitateAt = levitateAt; this.seedAt = seedAt; this.releaseAt = releaseAt;
        }
    }

    // ===================== LANCEMENT =====================

    /** Programme le sort. Appele depuis l'interaction (thread du monde). */
    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = getUuid(caster, store);
        if (uuid == null) return;
        long now = System.currentTimeMillis();
        activeRains.computeIfAbsent(uuid, k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                .add(new Rain(now + (long) (LEVITATE_AT * 1000), now + (long) (SEED_AT * 1000), now + (long) (RELEASE_AT * 1000)));
    }

    /** Lanceur au sol et cercle ouvert : les meteorites tombent une par une depuis le cercle. */
    private static void startRain(Rain rain, long now) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(METEOR_PROJECTILE);
        double speed = config != null && config.getLaunchForce() > 0 ? config.getLaunchForce() : 40.0;
        rain.meteorSpeed = speed;
        double delay = 0;
        for (int i = 0; i < rain.impactPoints.size(); i++) {
            Vector3d impact = rain.impactPoints.get(i);
            Vector3d spawn = new Vector3d(impact.x + rng.nextDouble(-SPAWN_JITTER, SPAWN_JITTER), rain.circleCenter.y + METEOR_ABOVE_CIRCLE,
                    impact.z + rng.nextDouble(-SPAWN_JITTER, SPAWN_JITTER));
            long spawnAt = now + (long) (delay * 1000);
            long impactAt = spawnAt + (long) (spawn.distance(impact) / speed * 1000);
            boolean guided = i > 0 && rng.nextDouble() < GUIDED_CHANCE;
            rain.meteors.add(new Meteor(impact, spawn, spawnAt, impactAt, false, false, guided));
            delay += METEOR_INTERVAL + rng.nextDouble(-METEOR_JITTER, METEOR_JITTER);
        }
        // Meteore du Jugement : trois fois plus gros, au centre, apres les autres
        Vector3d center = rain.impactPoints.get(0);
        Vector3d spawn = new Vector3d(center.x, rain.circleCenter.y + METEOR_ABOVE_CIRCLE, center.z);
        long spawnAt = now + (long) (delay * 1000) + GIANT_DELAY_MS;
        rain.meteors.add(new Meteor(new Vector3d(center), spawn, spawnAt, spawnAt + (long) (spawn.distance(center) / speed * 1000), true, true, false));
        rain.rainStarted = true;
    }

    /** Centre de la pluie : bloc vise, sinon devant le joueur, ramene au sol. */
    private static Vector3d findTargetCenter(Ref<EntityStore> caster, Store<EntityStore> store, World world, Vector3d casterPos) {
        Vector3i block = TargetUtil.getTargetBlock(caster, TARGET_RANGE, store);
        if (block != null) return new Vector3d(block.x + 0.5, block.y + 1.0, block.z + 0.5);

        HeadRotation head = store.getComponent(caster, HeadRotation.getComponentType());
        Vector3d dir = head != null ? new Vector3d(head.getDirection()) : new Vector3d(0, 0, 1);
        dir.y = 0;
        if (dir.lengthSquared() < 1e-6) dir.set(0, 0, 1);
        dir.normalize().mul(FALLBACK_DISTANCE);
        return projectToGround(world, new Vector3d(casterPos).add(dir));
    }

    private static List<Vector3d> pickImpactPoints(World world, Vector3d center, ThreadLocalRandom rng) {
        List<Vector3d> points = new ArrayList<>();
        points.add(projectToGround(world, center)); // le premier impact tombe au centre de la visee
        int attempts = 0;
        while (points.size() < METEOR_COUNT && attempts++ < 500) {
            double r = RAIN_RADIUS * Math.sqrt(rng.nextDouble());
            double a = rng.nextDouble(Math.PI * 2);
            Vector3d candidate = projectToGround(world,
                    new Vector3d(center.x + Math.cos(a) * r, center.y, center.z + Math.sin(a) * r));
            boolean tooClose = false;
            for (Vector3d p : points) {
                if (p.distanceSquared(candidate) < MIN_SPACING * MIN_SPACING) { tooClose = true; break; }
            }
            if (!tooClose) points.add(candidate);
        }
        // Ordre de chute aleatoire (pas de balayage regulier depuis le centre)
        java.util.Collections.shuffle(points.subList(1, points.size()), rng);
        return points;
    }

    /** Ramene le point sur le premier bloc solide en dessous (ou juste au-dessus). */
    private static Vector3d projectToGround(World world, Vector3d point) {
        Vector3d hit = TargetUtil.getTargetLocation(world, id -> id != 0,
                point.x, point.y + 12, point.z, 0, -1, 0, 40);
        return hit != null ? new Vector3d(point.x, hit.y, point.z) : new Vector3d(point);
    }

    // ===================== CHRONOLOGIE (appelee par le systeme a chaque tick) =====================

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Rain> rains = activeRains.get(uuid);
        if (rains == null) return;
        for (Rain rain : rains) {
            if (tickRain(uuid, rain, caster, store, cb)) rains.remove(rain);
        }
        if (rains.isEmpty()) activeRains.remove(uuid);
    }

    /** Fait avancer une pluie ; retourne true quand elle est terminee. */
    private static boolean tickRain(UUID uuid, Rain rain, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        long now = System.currentTimeMillis();

        if (!rain.levitating && now >= rain.levitateAt) {
            rain.levitating = true;
            startLevitation(caster, rain, store, cb);
        }
        if (!rain.seedLaunched && now >= rain.seedAt) {
            rain.seedLaunched = true;
            launchSeed(caster, rain, store, cb, now);
        }
        if (rain.seedLaunched && rain.circleCenter == null) {
            trackSeed(caster, rain, store, cb, now);
        }
        if (rain.levitating && !rain.released) followAdornments(caster, rain, store, cb);
        if (!rain.released && now >= rain.releaseAt) {
            rain.released = true;
            stopLevitation(caster, rain, store, cb);
            removeAdornments(rain, store, cb);
            fallProtectedUntil.put(uuid, now + FALL_PROTECTION_MS);
        }
        if (rain.released && !rain.rainStarted && rain.circleCenter != null && now >= rain.circleOpenedAt + RAIN_DELAY_MS) {
            long sinceRelease = now - rain.releaseAt;
            if ((sinceRelease >= LANDING_MIN_MS && isOnGround(caster, store)) || sinceRelease >= LANDING_TIMEOUT_MS) {
                startRain(rain, now);
            }
        }

        boolean done = rain.rainStarted;
        for (Meteor m : rain.meteors) {
            if (!m.spawned && now >= m.spawnAt) {
                m.spawned = true;
                if (m.guided) retarget(caster, rain, m, store, now);
                m.projectile = spawnMeteor(caster, m, cb);
                spawnFx(FX_IMPACT_ZONE, new Vector3d(m.impact).add(0, 0.05, 0), (float) ((m.giant ? GIANT_RADIUS : EXPLOSION_RADIUS) / 3.0), 0f, store, cb);
            }
            if (m.spawned && !m.exploded) growMeteor(m, now, store, cb);
            if (!m.exploded && now >= m.impactAt) {
                m.exploded = true;
                if (m.projectile != null && m.projectile.isValid()) cb.tryRemoveEntity(m.projectile, RemoveReason.REMOVE);
                explode(caster, rain, m, store, cb, now);
                rain.waves.add(new Wave(m.impact, now));
            }
            if (!m.exploded) done = false;
        }
        rain.waves.removeIf(w -> tickWave(caster, w, now, store, cb));
        if (!rain.waves.isEmpty()) done = false;
        if (done) {                                       // pluie terminee : le ciel se referme
            if (rain.circleEntity != null && rain.circleEntity.isValid()) cb.tryRemoveEntity(rain.circleEntity, RemoveReason.REMOVE);
            for (Ref<EntityStore> model : rain.skyModels) if (model != null && model.isValid()) cb.tryRemoveEntity(model, RemoveReason.REMOVE);
            rain.skyModels.clear();
            if (rain.scorch != null) { TerrainRestore.save(); rain.scorch = null; }
        }
        // Sol ardent : chaque seconde, les ennemis sur un point d'impact encore brulant prennent feu
        rain.burning.removeIf(b -> now >= b.until());
        if (!rain.burning.isEmpty() && now - rain.lastBurnPulse >= 1000) {
            rain.lastBurnPulse = now;
            for (Burning b : rain.burning) {
                for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, b.pos(), BURNING_RADIUS, store)) {
                    SpellFx.damage(caster, target, "Fire", BURNING_DAMAGE, null, null, cb);
                    SpellFx.effect(target, BURNING_EFFECT, cb);
                }
            }
        }
        return done && rain.burning.isEmpty();
    }

    // ===================== PROJECTILE GRAINE =====================

    /** Lance le projectile invisible la ou le lanceur regarde. */
    private static void launchSeed(Ref<EntityStore> caster, Rain rain, Store<EntityStore> store, CommandBuffer<EntityStore> cb, long now) {
        TransformComponent transform = store.getComponent(caster, TransformComponent.getComponentType());
        HeadRotation head = store.getComponent(caster, HeadRotation.getComponentType());
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(SEED_PROJECTILE);
        if (transform == null || config == null) {
            EldaniorLogger.error("MuspelheimWrath: impossible de lancer la graine (" + SEED_PROJECTILE + ")");
            openCircle(caster, rain, findTargetCenter(caster, store, worldOf(store), transform != null ? transform.getPosition() : new Vector3d()), store, cb, now);
            return;
        }
        Vector3d dir = head != null ? new Vector3d(head.getDirection()).normalize() : new Vector3d(0, 0, 1);
        Vector3d origin = new Vector3d(transform.getPosition()).add(0, 1.5, 0).add(new Vector3d(dir).mul(1.0));
        rain.seedOrigin = origin;
        rain.seedLaunchedAt = now;
        try {
            rain.seed = ProjectileModule.get().spawnProjectile(caster, cb, config, new Vector3d(origin), dir);
        } catch (Exception e) {
            EldaniorLogger.error("MuspelheimWrath: lancement de la graine", e);
        }
        playSound(SFX_SEED, origin, cb);
        if (rain.seed == null) openCircle(caster, rain, findTargetCenter(caster, store, worldOf(store), transform.getPosition()), store, cb, now);
    }

    /** Suit la graine : elle s'est arretee (bloc) ou a disparu (creature) -> ouverture du cercle. Pas de portee max. */
    private static void trackSeed(Ref<EntityStore> caster, Rain rain, Store<EntityStore> store, CommandBuffer<EntityStore> cb, long now) {
        if (rain.seed == null) return;
        Vector3d pos = null;
        if (rain.seed.isValid()) {
            TransformComponent t = store.getComponent(rain.seed, TransformComponent.getComponentType());
            if (t != null) pos = new Vector3d(t.getPosition());
        }
        long flying = now - rain.seedLaunchedAt;
        boolean landed;
        if (pos == null) {
            // Pas encore cree (premier tick) ou detruit en touchant une creature
            landed = rain.seedLastPos != null && flying > 100;
            pos = rain.seedLastPos;
        } else {
            landed = rain.seedLastPos != null && flying > 150 && pos.distanceSquared(rain.seedLastPos) < 0.0025;
            rain.seedLastPos = pos;
        }
        if (!landed && flying > SEED_TIMEOUT_MS) {
            landed = true;
            if (pos == null) pos = rain.seedOrigin;
        }
        if (!landed) return;

        if (rain.seed.isValid()) cb.tryRemoveEntity(rain.seed, RemoveReason.REMOVE);
        openCircle(caster, rain, projectToGround(worldOf(store), pos), store, cb, now);
    }

    /** Ouvre le cercle du ciel au-dessus du point vise et prepare les points d'impact. */
    private static void openCircle(Ref<EntityStore> caster, Rain rain, Vector3d center, Store<EntityStore> store, CommandBuffer<EntityStore> cb, long now) {
        World world = worldOf(store);
        rain.impactPoints = pickImpactPoints(world, center, ThreadLocalRandom.current());
        rain.circleCenter = new Vector3d(center).add(0, SKY_HEIGHT, 0);
        rain.circleOpenedAt = now;
        // Cercle en modele 3D (la particule n'etait pas affichee a cette hauteur) : immobile, retire a la fin de la pluie
        ProjectileConfig circle = ProjectileConfig.getAssetMap().getAsset(SKY_CIRCLE_PROJECTILE);
        if (circle != null) {
            try {
                rain.circleEntity = ProjectileModule.get().spawnProjectile(caster, cb, circle, new Vector3d(rain.circleCenter), new Vector3d(1, 0, 0));
            } catch (Exception e) {
                EldaniorLogger.error("MuspelheimWrath: cercle du ciel", e);
            }
        }
        EldaniorLogger.info(String.format("[CourrouxMuspelheim] Cercle du ciel (modele) %s en (%.1f, %.1f, %.1f)",
                rain.circleEntity != null ? "pose" : "ECHEC", rain.circleCenter.x, rain.circleCenter.y, rain.circleCenter.z));
        playSound(SFX_CIRCLE, rain.circleCenter, cb);
        // Ciel embrase : nuages de feu autour du cercle, pluie de cendres et de braises sur la zone
        rain.skyModels.add(spawnFixed(caster, CLOUDS_LOW_PROJECTILE, new Vector3d(rain.circleCenter).sub(0, CLOUDS_BELOW, 0), cb));
        rain.skyModels.add(spawnFixed(caster, CLOUDS_HIGH_PROJECTILE, new Vector3d(rain.circleCenter).add(0, CLOUDS_ABOVE, 0), cb));
        rain.skyModels.add(spawnFixed(caster, CLOUDS_TOP_PROJECTILE, new Vector3d(rain.circleCenter).add(0, CLOUDS_TOP, 0), cb));
        rain.skyModels.add(spawnFixed(caster, VORTEX_PROJECTILE, new Vector3d(rain.circleCenter).sub(0, VORTEX_BELOW, 0), cb));
        int steps = (int) Math.ceil(RAIN_RADIUS / ASH_SPACING);
        for (int i = -steps; i <= steps; i++) {
            for (int j = -steps; j <= steps; j++) {
                double dx = i * ASH_SPACING, dz = j * ASH_SPACING;
                if (Math.hypot(dx, dz) > RAIN_RADIUS + 2) continue;
                SpellFx.fx(FX_ASH_RAIN, projectToGround(world, new Vector3d(center).add(dx, 0, dz)).add(0, 10, 0), 1.0f, store, cb);
            }
        }
    }

    private static Ref<EntityStore> spawnFixed(Ref<EntityStore> caster, String configId, Vector3d pos, CommandBuffer<EntityStore> cb) {
        return spawnFixed(caster, configId, pos, new Vector3d(1, 0, 0), cb);
    }

    private static Ref<EntityStore> spawnFixed(Ref<EntityStore> caster, String configId, Vector3d pos, Vector3d dir, CommandBuffer<EntityStore> cb) {
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(configId);
        if (config == null) {
            EldaniorLogger.error("MuspelheimWrath: projectile introuvable " + configId);
            return null;
        }
        try {
            return ProjectileModule.get().spawnProjectile(caster, cb, config, new Vector3d(pos), new Vector3d(dir));
        } catch (Exception e) {
            EldaniorLogger.error("MuspelheimWrath: " + configId, e);
            return null;
        }
    }

    // ===================== AILES ET COURONNE DE FEU =====================

    /** Ailes dans le dos et couronne au-dessus de la tete, tant que le lanceur levite. */
    private static void followAdornments(Ref<EntityStore> caster, Rain rain, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return;
        Vector3d look = SpellFx.lookDirection(caster, store);
        Vector3d fwd = new Vector3d(look.x, 0, look.z);
        if (fwd.lengthSquared() < 1e-6) fwd.set(0, 0, 1);
        fwd.normalize();
        Vector3d wingsAt = new Vector3d(pos).add(0, WINGS_HEIGHT, 0).sub(new Vector3d(fwd).mul(WINGS_BACK));
        Vector3d crownAt = new Vector3d(pos).add(0, CROWN_HEIGHT, 0);
        if (rain.wings == null) rain.wings = spawnFixed(caster, WINGS_PROJECTILE, wingsAt, fwd, cb);
        if (rain.crown == null) rain.crown = spawnFixed(caster, CROWN_PROJECTILE, crownAt, cb);
        com.hypixel.hytale.math.vector.Rotation3f facing = new com.hypixel.hytale.math.vector.Rotation3f();
        facing.setYaw(com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath.normalizeTurnAngle(
                com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath.headingFromDirection(fwd.x, fwd.z)));
        hold(rain.wings, wingsAt, facing, store);
        hold(rain.crown, crownAt, null, store);
    }

    private static void hold(Ref<EntityStore> ref, Vector3d pos, com.hypixel.hytale.math.vector.Rotation3f rot, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return;
        t.setPosition(pos);
        if (rot != null) t.setRotation(rot);
        com.hypixel.hytale.server.core.modules.physics.component.Velocity v =
                store.getComponent(ref, com.hypixel.hytale.server.core.modules.physics.component.Velocity.getComponentType());
        if (v != null) v.setZero();
    }

    private static void removeAdornments(Rain rain, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (Ref<EntityStore> ref : new Ref[]{rain.wings, rain.crown}) {
            if (ref == null || !ref.isValid()) continue;
            Vector3d p = SpellFx.position(ref, store);
            if (p != null) spawnFx(FX_WAVE_HIT, p, 0.8f, 0f, store, cb);   // s'eteignent dans une gerbe de feu
            cb.tryRemoveEntity(ref, RemoveReason.REMOVE);
        }
    }

    // ===================== METEORITES GUIDEES =====================

    /** Meteorite guidee : vise un ennemi present dans la zone au moment ou elle apparait. */
    private static void retarget(Ref<EntityStore> caster, Rain rain, Meteor m, Store<EntityStore> store, long now) {
        Vector3d center = rain.impactPoints.get(0);
        List<Ref<EntityStore>> enemies = SpellFx.enemiesAround(caster, center, RAIN_RADIUS, store);
        if (enemies.isEmpty()) return;
        Vector3d target = SpellFx.position(enemies.get(ThreadLocalRandom.current().nextInt(enemies.size())), store);
        if (target == null) return;
        m.impact = projectToGround(worldOf(store), target);
        m.spawn = new Vector3d(m.impact.x, m.spawn.y, m.impact.z);
        m.impactAt = now + (long) (m.spawn.distance(m.impact) / rain.meteorSpeed * 1000);
    }

    private static World worldOf(Store<EntityStore> store) {
        return ((EntityStore) store.getExternalData()).getWorld();
    }

    private static boolean isOnGround(Ref<EntityStore> caster, Store<EntityStore> store) {
        MovementStatesComponent states = store.getComponent(caster, MovementStatesComponent.getComponentType());
        return states != null && states.getMovementStates() != null && states.getMovementStates().onGround;
    }

    /** La meteorite grossit en approchant du sol. */
    private static void growMeteor(Meteor m, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (m.projectile == null || !m.projectile.isValid()) return;
        float progress = Math.min(1f, Math.max(0f, (float) (now - m.spawnAt) / Math.max(1, m.impactAt - m.spawnAt)));
        float scale = (GROWTH_START + (GROWTH_END - GROWTH_START) * progress) * (m.giant ? GIANT_SCALE : 1f);
        EntityScaleComponent comp = store.getComponent(m.projectile, EntityScaleComponent.getComponentType());
        if (comp == null) cb.putComponent(m.projectile, EntityScaleComponent.getComponentType(), new EntityScaleComponent(scale));
        else if (Math.abs(comp.getScale() - scale) > 0.01f) comp.setScale(scale);
    }

    // ===================== LEVITATION =====================

    private static void startLevitation(Ref<EntityStore> caster, Rain rain, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        MovementManager manager = store.getComponent(caster, MovementManager.getComponentType());
        PlayerRef netRef = store.getComponent(caster, PlayerRef.getComponentType());
        if (manager == null || netRef == null || manager.getSettings() == null) return;
        MovementSettings s = manager.getSettings();
        rain.prevFly = s.fly;
        rain.prevHorizontalFlySpeed = s.horizontalFlySpeed;
        rain.prevVerticalFlySpeed = s.verticalFlySpeed;
        // Vol force a vitesse nulle : le lanceur reste suspendu dans les airs
        s.fly = FlyMode.Forced;
        s.horizontalFlySpeed = 0f;
        s.verticalFlySpeed = 0f;
        applyMovement(caster, manager, netRef, true, cb);
    }

    private static void stopLevitation(Ref<EntityStore> caster, Rain rain, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (rain.prevFly == null) return; // la levitation n'a jamais demarre
        MovementManager manager = store.getComponent(caster, MovementManager.getComponentType());
        PlayerRef netRef = store.getComponent(caster, PlayerRef.getComponentType());
        if (manager == null || netRef == null || manager.getSettings() == null) return;
        MovementSettings s = manager.getSettings();
        s.fly = rain.prevFly;
        s.horizontalFlySpeed = rain.prevHorizontalFlySpeed;
        s.verticalFlySpeed = rain.prevVerticalFlySpeed;
        applyMovement(caster, manager, netRef, false, cb);
    }

    private static void applyMovement(Ref<EntityStore> caster, MovementManager manager, PlayerRef netRef,
                                      boolean flying, CommandBuffer<EntityStore> cb) {
        try {
            manager.update(netRef.getPacketHandler());
            cb.putComponent(caster, MovementManager.getComponentType(), manager);
            netRef.getPacketHandler().writeNoCache(new SetMovementStates(new SavedMovementStates(flying)));
        } catch (Exception e) {
            EldaniorLogger.error("MuspelheimWrath: levitation", e);
        }
    }

    /** Vrai si le joueur vient de redescendre du Courroux de Muspelheim (degats de chute annules). */
    public static boolean isFallProtected(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (ref == null || !ref.isValid() || fallProtectedUntil.isEmpty()) return false;
        UUID uuid = getUuid(ref, store);
        if (uuid == null) return false;
        Long until = fallProtectedUntil.get(uuid);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) { fallProtectedUntil.remove(uuid); return false; }
        return true;
    }

    // ===================== METEORITES =====================

    private static Ref<EntityStore> spawnMeteor(Ref<EntityStore> caster, Meteor m, CommandBuffer<EntityStore> cb) {
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(METEOR_PROJECTILE);
        if (config == null) {
            EldaniorLogger.error("MuspelheimWrath: projectile introuvable " + METEOR_PROJECTILE);
            return null;
        }
        playSound(SFX_FALL, m.impact, cb);
        Vector3d dir = new Vector3d(m.impact).sub(m.spawn).normalize();
        try {
            return ProjectileModule.get().spawnProjectile(caster, cb, config, new Vector3d(m.spawn), dir);
        } catch (Exception e) {
            EldaniorLogger.error("MuspelheimWrath: spawn meteorite", e);
            return null;
        }
    }

    /**
     * Impact : visuels + explosion instantanee dont le souffle propulse les entites (la vague de flammes part ensuite,
     * voir {@link #tickWave}), tremblement de camera, puis terre brulee + sol ardent ou, sur une zone gelee par la
     * Porte de Niflheim, explosion de vapeur. Le Meteore du Jugement fait tout en plus grand.
     */
    private static void explode(Ref<EntityStore> caster, Rain rain, Meteor m, Store<EntityStore> store, CommandBuffer<EntityStore> cb, long now) {
        Vector3d pos = m.impact;
        float big = m.giant ? 2.5f : 1f;
        double radius = m.giant ? GIANT_RADIUS : EXPLOSION_RADIUS;
        float zoneScale = (float) (DAMAGE_RADIUS / 3.0);
        // Explosion + vague de chaleur du Gobliterator a l'echelle de la zone + sol embrase + fumee
        spawnFx(FX_HEAT_BLAST, pos, (float) (DAMAGE_RADIUS / HEAT_BLAST_BASE_RADIUS) * 0.7f * big, 0f, store, cb);
        spawnFx(FX_EXPLOSION, new Vector3d(pos).add(0, 1, 0), 5.0f * big, 0f, store, cb);
        spawnFx(FX_BLAST_BURST, new Vector3d(pos).add(0, 0.5, 0), 2.0f * big, 0f, store, cb);
        spawnFx(FX_GROUND_FIRE, pos, zoneScale * big, 5.0f, store, cb);
        spawnFx(FX_SMOKE, new Vector3d(pos).add(0, 1.5, 0), 6.0f * big, 0f, store, cb);
        if (m.giant) {
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3;
                spawnFx(FX_EXPLOSION, new Vector3d(pos).add(Math.cos(a) * 6, 1, Math.sin(a) * 6), 4.0f, 0f, store, cb);
            }
        }
        if (m.giant || ThreadLocalRandom.current().nextDouble() < FIRE_PILLAR_CHANCE) {   // colonne de feu qui jaillit
            spawnFx(FX_FIRE_PILLAR, pos, m.giant ? 2.2f : 1.2f, 0f, store, cb);
        }
        playSound(SFX_HEAT_BLAST, pos, cb);
        playSound(m.last ? SFX_FINAL_IMPACT : SFX_IMPACT, pos, cb);
        SpellFx.shake(pos, SHAKE_RADIUS * (m.giant ? 2 : 1), CAMERA_SHAKE, store);

        float damage = m.giant ? GIANT_DAMAGE : DAMAGE;
        double blastForce = m.giant ? GIANT_BLAST : BLAST_FORCE, blastUp = m.giant ? GIANT_UP : BLAST_UP;
        for (Ref<EntityStore> target : TargetUtil.getAllEntitiesInSphere(pos, radius, store)) {
            if (!isEnemy(caster, target, store)) continue;
            TransformComponent t = store.getComponent(target, TransformComponent.getComponentType());
            double dist = t != null ? t.getPosition().distance(pos) : 0;
            float ratio = 1f - (1f - EDGE_DAMAGE_RATIO) * (float) Math.min(1.0, dist / radius);
            Vector3d blast = t != null ? new Vector3d(t.getPosition()).sub(pos) : new Vector3d(1, 0, 0);
            blast.y = 0;
            if (blast.lengthSquared() < 1e-4) blast.set(1, 0, 0);
            blast.normalize().mul(blastForce * ratio).add(0, blastUp * ratio, 0);
            hit(caster, target, damage * ratio, blast, cb);
        }

        World world = worldOf(store);
        if (NiflheimGate.isFrozenAt(world.getName(), pos)) {          // combo : la glace de Niflheim fond et se vaporise
            double meltRadius = STEAM_RADIUS * big;
            spawnFx(FX_STEAM, new Vector3d(pos).add(0, 1, 0), 8.0f * big, 0f, store, cb);
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3;
                spawnFx(FX_STEAM, new Vector3d(pos).add(Math.cos(a) * meltRadius * 0.6, 1, Math.sin(a) * meltRadius * 0.6), 5.0f, 0f, store, cb);
            }
            spawnFx(FX_ICE_SHARDS, new Vector3d(pos).add(0, 1, 0), 3.0f * big, 0f, store, cb);
            playSound(SFX_HEAT_BLAST, pos, cb);
            for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, pos, meltRadius, store)) {
                SpellFx.damage(caster, target, "Fire", STEAM_DAMAGE, null, "Impact_Strong", cb);
            }
            NiflheimGate.meltPrisons(pos, meltRadius, store, cb);
            Vector3d at = new Vector3d(pos);
            world.execute(() -> {
                try { TerrainRestore.restoreArea(world, at.x, at.z, meltRadius, FROST_PLACED); }
                catch (Exception e) { EldaniorLogger.error("MuspelheimWrath: fonte de la glace", e); }
            });
            return;
        }
        // Terre brulee (restauree plus tard) et sol ardent
        spawnFx(FX_BURNING, new Vector3d(pos).add(0, 0.1, 0), (float) (BURNING_RADIUS / 3.0) * big, 0f, store, cb);
        rain.burning.add(new Burning(new Vector3d(pos), now + BURNING_MS));
        if (rain.scorch == null) rain.scorch = TerrainRestore.open(world, now + SCORCH_RESTORE_MS);
        TerrainRestore.Restore batch = rain.scorch;
        double scorchR = m.giant ? GIANT_SCORCH_RADIUS : SCORCH_RADIUS, craterR = m.giant ? GIANT_CRATER_RADIUS : CRATER_RADIUS;
        Vector3d at = new Vector3d(pos);
        world.execute(() -> {
            try { scorch(world, batch, at, scorchR, craterR); }
            catch (Exception e) { EldaniorLogger.error("MuspelheimWrath: terre brulee", e); }
        });
    }

    /** Brule le terrain autour d'un impact (a executer hors tick ECS) : cratere au centre, roche volcanique, arbres calcines. */
    private static void scorch(World world, TerrainRestore.Restore batch, Vector3d c, double radius, double crater) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int r = (int) Math.ceil(radius), cx = (int) Math.floor(c.x), cy = (int) Math.floor(c.y), cz = (int) Math.floor(c.z);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.hypot(dx, dz);
                if (d > radius || (d > radius - 1 && rng.nextDouble() < 0.5)) continue;   // bord irregulier
                int x = cx + dx, z = cz + dz;
                if (TerrainRestore.isProtectedArea(world.getName(), x, cy, z)) continue;
                boolean inCrater = d <= crater, cratered = false, open = TerrainRestore.isEmpty(world, x, cy + 13, z);
                for (int y = cy + 12; y >= cy - 8; y--) {
                    var type = com.eldanior.system.util.WorldBlockUtil.getBlockType(world, x, y, z);
                    if (type == null) break;
                    String id = type.getId();
                    if (TerrainRestore.isEmpty(world, x, y, z)) { open = true; continue; }
                    boolean wasOpen = open;
                    open = false;
                    if (TerrainRestore.isKept(world, id, x, y, z) || TerrainRestore.contains(id, SPELL_BLOCK_KEYWORDS)) {
                        if (wasOpen && !id.contains("Leaves") && !id.contains("Wood_")) break;
                        continue;
                    }
                    if (id.contains("Leaves")) { TerrainRestore.place(world, batch, x, y, z, id, BURNT_LEAVES, 0); continue; }
                    if (id.startsWith("Wood_") && id.contains("Trunk")) { TerrainRestore.place(world, batch, x, y, z, id, BURNT_TRUNK, 0); continue; }
                    if (TerrainRestore.contains(id, FOLIAGE_KEYWORDS)) {                  // vegetation : consumee
                        TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0);
                        open = true;
                        continue;
                    }
                    if (!wasOpen) continue;
                    if (inCrater && !cratered && d <= crater - 0.5) {                     // cratere : la surface est soufflee...
                        TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0);
                        if (rng.nextDouble() < CRATER_LAVA_CHANCE) TerrainRestore.placeFluid(world, batch, x, y, z, LAVA); // ...et se remplit de lave
                        cratered = true;
                        open = true;
                        continue;
                    }
                    if (!inCrater && rng.nextDouble() < SCATTER_LAVA_CHANCE) {            // flaque de lave dans la terre brulee
                        TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0);
                        TerrainRestore.placeFluid(world, batch, x, y, z, LAVA);
                        break;
                    }
                    String ground = inCrater ? LAVA_ROCK : SCORCHED_GROUND[rng.nextInt(SCORCHED_GROUND.length)];
                    TerrainRestore.place(world, batch, x, y, z, id, ground, 0);
                    break;
                }
            }
        }
    }

    /**
     * Fait avancer une vague de flammes : chaque ennemi atteint par le front subit des degats de brulure,
     * la brulure celeste et un knockback qui le projette hors de la zone. Retourne true quand la vague est finie.
     */
    private static boolean tickWave(Ref<EntityStore> caster, Wave w, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        double radius = Math.min(DAMAGE_RADIUS, WAVE_SPEED * (now - w.startAt) / 1000.0);
        if (radius > 0.1) {
            List<Ref<EntityStore>> reached = new ArrayList<>(TargetUtil.getAllEntitiesInSphere(w.center, radius, store));
            for (Ref<EntityStore> target : reached) {
                if (w.hit.contains(target) || !isEnemy(caster, target, store)) continue;
                w.hit.add(target);
                TransformComponent t = store.getComponent(target, TransformComponent.getComponentType());
                Vector3d push = t != null ? new Vector3d(t.getPosition()).sub(w.center) : new Vector3d(1, 0, 0);
                push.y = 0;
                if (push.lengthSquared() < 1e-4) push.set(1, 0, 0);
                push.normalize().mul(KNOCKBACK_FORCE).add(0, KNOCKBACK_UP, 0);
                hit(caster, target, WAVE_DAMAGE, push, cb);
                if (t != null) {
                    Vector3d at = new Vector3d(t.getPosition()).add(0, 1, 0);
                    spawnFx(FX_WAVE_HIT, at, 0.8f, 0f, store, cb);
                    playSound(SFX_WAVE_HIT, at, cb);
                }
                try {
                    if (!EffectsManager.applyEffect(target, BURN_EFFECT, cb)) {
                        EldaniorLogger.warn("[CourrouxMuspelheim] Brulure non appliquee (" + BURN_EFFECT + " introuvable ?)");
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("MuspelheimWrath: brulure", e);
                }
            }
        }
        return radius >= DAMAGE_RADIUS;
    }

    /** Ennemi valide : joueur ou creature, ni le lanceur ni un allie (groupe, guilde, famille). */
    private static boolean isEnemy(Ref<EntityStore> caster, Ref<EntityStore> target, Store<EntityStore> store) {
        if (target == null || !target.isValid() || target.equals(caster)) return false;
        boolean isPlayer = store.getComponent(target, Player.getComponentType()) != null;
        boolean isNpc = store.getComponent(target, NPCEntity.getComponentType()) != null;
        if (!isPlayer && !isNpc) return false;
        return !(isPlayer && isAlly(caster, target, store));
    }

    /** Degats de feu attribues au lanceur, avec knockback et secousse de camera optionnels. */
    private static void hit(Ref<EntityStore> caster, Ref<EntityStore> target, float amount, Vector3d knockbackVelocity,
                            CommandBuffer<EntityStore> cb) {
        try {
            Damage damage = new Damage(new Damage.EntitySource(caster), DamageCause.getAssetMap().getIndex("Fire"), amount);
            if (knockbackVelocity != null) {
                KnockbackComponent knockback = new KnockbackComponent();
                knockback.setVelocity(knockbackVelocity);
                knockback.setVelocityType(ChangeVelocityType.Set);
                VelocityConfig velocity = new VelocityConfig();
                velocity.setGroundResistanceMax(0f);
                velocity.setThreshold(0.25f);
                knockback.setVelocityConfig(velocity);
                damage.putMetaObject(Damage.KNOCKBACK_COMPONENT, knockback);
                // Comme DamageEntityInteraction : le composant doit etre pose sur la cible pour que la poussee s'applique
                cb.putComponent(target, KnockbackComponent.getComponentType(), knockback);
            }
            int cameraIndex = com.hypixel.hytale.server.core.asset.type.camera.CameraEffect.getAssetMap().getIndex(CAMERA_IMPACT);
            if (cameraIndex >= 0) damage.putMetaObject(Damage.CAMERA_EFFECT, new Damage.CameraEffect(cameraIndex));
            DamageSystems.executeDamage(target, cb, damage);
        } catch (Exception e) {
            EldaniorLogger.error("MuspelheimWrath: degats", e);
        }
    }

    // ===================== ALLIES =====================

    /** Meme groupe, meme guilde ou meme famille noble que le lanceur. */
    static boolean isAlly(Ref<EntityStore> caster, Ref<EntityStore> target, Store<EntityStore> store) {
        UUID a = getUuid(caster, store), b = getUuid(target, store);
        if (a != null && b != null) {
            Party pa = PartyManager.getParty(a);
            if (pa != null && pa == PartyManager.getParty(b)) return true;
        }
        var type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData da = store.getComponent(caster, type), db = store.getComponent(target, type);
        if (da == null || db == null) return false;
        return sameNonEmpty(da.getGuildId(), db.getGuildId()) || sameNonEmpty(da.getNobleFamilyId(), db.getNobleFamilyId());
    }

    private static boolean sameNonEmpty(String x, String y) {
        return x != null && !x.isEmpty() && x.equals(y);
    }

    // ===================== UTILITAIRES =====================

    /**
     * Affiche une particule pour tous les joueurs a moins de {@link #FX_VIEW_RADIUS} blocs.
     * (L'envoi par defaut de ParticleUtil se limite a 75 blocs : le cercle a 100 blocs de haut serait invisible.)
     */
    /** @return le nombre de joueurs a qui la particule a ete envoyee. */
    private static int spawnFx(String id, Vector3d pos, float scale, float maxDuration,
                               Store<EntityStore> store, ComponentAccessor<EntityStore> accessor) {
        try {
            List<Ref<EntityStore>> viewers = new ArrayList<>();
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(pos, FX_VIEW_RADIUS, store)) {
                if (ref != null && ref.isValid() && store.getComponent(ref, Player.getComponentType()) != null) viewers.add(ref);
            }
            if (viewers.isEmpty()) return 0;
            ParticleUtil.spawnParticleEffect(id, pos.x, pos.y, pos.z, 0f, 0f, 0f, scale, null, null, viewers, accessor, maxDuration);
            return viewers.size();
        } catch (Exception e) {
            EldaniorLogger.error("MuspelheimWrath: particule " + id, e);
            return 0;
        }
    }

    private static void playSound(String id, Vector3d pos, ComponentAccessor<EntityStore> accessor) {
        int index = SoundEvent.getAssetMap().getIndex(id);
        if (index < 0) return;
        try {
            SoundUtil.playSoundEvent3d(index, SoundCategory.SFX, pos, accessor);
        } catch (Exception e) {
            EldaniorLogger.error("MuspelheimWrath: son " + id, e);
        }
    }

    static UUID getUuid(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        PlayerRef netRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (netRef == null) return null;
        try { return UUIDExtractor.getUUID(netRef); }
        catch (Exception e) { return null; }
    }

    static boolean hasActiveRain(UUID uuid) {
        return activeRains.containsKey(uuid);
    }

    public static void handleDisconnect(UUID uuid) {
        List<Rain> rains = activeRains.get(uuid);
        if (rains != null) for (Rain r : rains) if (r.scorch != null) TerrainRestore.save();
        activeRains.remove(uuid);
        fallProtectedUntil.remove(uuid);
    }
}
