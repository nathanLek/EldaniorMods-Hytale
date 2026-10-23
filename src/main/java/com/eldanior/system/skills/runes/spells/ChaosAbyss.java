package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Abime du Chaos — sort divin de l'espace (Rune_AbimeChaos), le vide originel grec.
 *
 * Une minuscule boule noire apparait au sol, la ou vise le lanceur, puis s'eleve jusqu'a {@link #SINGULARITY_HEIGHT}
 * blocs en grossissant ({@link #RISE_MS}) ; arrivee en haut, elle s'ouvre en trou noir supermassif : ombre
 * parfaitement noire (horizon des evenements), fin anneau de photons tres brillant, disque d'accretion plat et etroit
 * (blanc/jaune pres de l'horizon, orange puis rouge au bord), arcs de lentille gravitationnelle au-dessus et en
 * dessous de l'ombre, et l'arriere-plan devie autour.
 *  - Attraction ({@link #PULL_MS}) : le trou noir grossit (de {@link #BASE_HORIZON} blocs de rayon a {@link #GROW_TO}
 *    fois plus) et devore le sol colonne par colonne (les vrais blocs, arbres compris) ; les blocs montent en spirale,
 *    tournent dans le disque de plus en plus vite et plongent dans l'horizon. Les ennemis (joueurs et creatures) sont
 *    souleves et tournent dans le disque, dechiquetes par les forces de maree pres de l'horizon.
 *  - Quasar ({@link #QUASAR_MS}) : gave de matiere, il s'emballe et crache deux jets relativistes aux poles, l'un vers
 *    le ciel, l'autre qui perce le sol sous lui ; tout ce qui se trouve dans un jet est brule.
 *  - Implosion ({@link #IMPLODE_MS}) : le disque entier est englouti d'un coup et l'horizon se contracte en un point.
 *  - Liberation : eclair blanc, onde de choc spherique, la matiere engloutie est recrachee dans toutes les directions,
 *    immense cratere, gros degats et projection ; puis le trou noir s'evapore en quelques etincelles.
 * Le terrain arrache est restaure apres {@link #RESTORE_AFTER_MS}.
 * Pas de l'espace : pendant le sort, chaque clic droit du lanceur le teleporte au point vise.
 */
public final class ChaosAbyss {

    private ChaosAbyss() {}

    // ===== Reglages =====
    private static final double TARGET_RANGE = 100.0, FALLBACK_DISTANCE = 30.0;   // la boule apparait la ou vise le curseur
    static final double RADIUS = SpellFx.DIVINE_ZONE_RADIUS;   // meme zone que la Porte de Niflheim
    private static final double SINGULARITY_HEIGHT = 50.0;
    private static final long RISE_MS = 4_000;                  // la boule monte du sol jusqu'a sa hauteur, puis le sort commence
    private static final double PULL_RADIUS = SINGULARITY_HEIGHT + RADIUS;   // mesure depuis le trou noir (qui est en hauteur)
    // Zone d'attraction : distance a l'axe du trou noir, petite au debut puis de plus en plus large
    private static final double PULL_REACH_START = 10.0, PULL_REACH_END = RADIUS + 15;
    private static final long PULL_MS = 30_000, QUASAR_MS = 3_500, IMPLODE_MS = 1_500, AFTERGLOW_MS = 3_000;
    private static final long RELEASE_AT = PULL_MS + QUASAR_MS + IMPLODE_MS, END_MS = RELEASE_AT + AFTERGLOW_MS;
    private static final long PULL_EVERY_MS = 200, VISUAL_EVERY_MS = 300, ACCRETION_FX_EVERY_MS = 1_000, RUMBLE_EVERY_MS = 2_500, FRONT_EVERY_MS = 700;
    private static final long RISE_FX_EVERY_MS = 150, JET_EVERY_MS = 250, JET_IMPACT_EVERY_MS = 600;
    // Trou noir : grossit avec le temps, de x1 a x5 (rayon de l'ombre : BASE_HORIZON blocs a x1)
    private static final float SPAWN_SCALE = 0.06f, GROW_FROM = 1.0f, GROW_TO = 5.0f;   // minuscule au depart, x1 en haut
    private static final double BASE_HORIZON = 4.0;
    private static final double MODEL_SCALE_PER_BLOCK = 16.0;   // echelle du modele pour 1 bloc de rayon d'ombre (sphere de 4 unites, 64 u/bloc)
    private static final double FX_HORIZON = 1.5;               // rayon d'ombre pour lequel les particules sont dessinees a l'echelle 1
    private static final long MODEL_UPDATE_MS = 400, MODEL_UPDATE_COLLAPSE_MS = 120;
    private static final double HITBOX_HALF = 20.0;
    private static final double DISK_INNER = 1.35, DISK_OUTER = 2.3;   // disque d'accretion, en rayons d'horizon
    private static final double ORBIT_SPEED = 0.9;              // rad/s au bord exterieur du disque (Kepler : plus vite pres du centre)
    private static final double ENTITY_ORBIT = 1.85;            // les ennemis tournent dans le disque a 1,85 rayon d'horizon
    private static final float PULL_DAMAGE_MIN = 8f, PULL_DAMAGE_MAX = 40f, TIDAL_DAMAGE = 120f, JET_DAMAGE = 150f;
    private static final double JET_RADIUS = 3.5;                // rayon du jet (en plus de 15 % du rayon d'horizon)
    private static final float SUPERNOVA_DAMAGE = 1500f;
    private static final double SUPERNOVA_RADIUS = RADIUS, SUPERNOVA_PUSH = 25.0, SUPERNOVA_LIFT = 10.0;
    private static final double CRATER_RADIUS = 22.0, LAUNCH_CRATER_RADIUS = 2.5, JET_CRATER_RADIUS = 6.0;
    private static final int CRATER_DEPTH = 8, LAUNCH_CRATER_DEPTH = 2, JET_CRATER_DEPTH = 2;   // le jet creuse a chaque impact
    private static final int MAX_EJECTA = 150;                    // blocs engloutis recraches a la liberation
    // Sol devore : de plus en plus loin du centre, plusieurs blocs de profondeur par colonne
    private static final long TEAR_EVERY_MS = 100, DEBRIS_FLIGHT_MS = 1_800, DEBRIS_FLIGHT_PER_BLOCK_MS = 35, EJECTA_FLIGHT_MS = 1_800;
    private static final int TEAR_PER_PULSE = 16, TEAR_MAX_DEPTH = 4, TEAR_CENTER_BONUS = 5;
    private static final int MAX_FLYING = 240, MAX_DISK = 400;   // entites-blocs simultanees (au-dela : le bloc disparait)
    private static final double DEBRIS_MIN_R = 5.0, DEBRIS_MAX_R = RADIUS + 8;
    static final long RESTORE_AFTER_MS = 180_000;
    // Pas de l'espace
    private static final double BLINK_RANGE = 25.0;
    private static final int BLINK_CHARGES = 3;
    private static final long BLINK_COOLDOWN_MS = 1_200;

    // ===== Assets =====
    private static final String SINGULARITY_PROJECTILE = "Projectile_Config_Chaos_Singularity";
    private static final String SINGULARITY_MODEL = "Chaos_Singularity";
    private static final String DEBRIS_PROJECTILE = "Projectile_Config_Chaos_Debris";
    private static final String FX_BLACK_HOLE = "Chaos_BlackHole";    // anneau de photons, arcs de lentille, distortion
    private static final String FX_ACCRETION = "Chaos_Accretion";
    private static final String FX_JET = "Chaos_Jet";
    private static final String FX_BURST = "Chaos_Burst";
    private static final String FX_FLASH = "Chaos_Flash";
    private static final String FX_SHOCKWAVE = "Chaos_Shockwave";
    private static final String FX_EXPLOSION = "Explosion_Big";
    private static final String FX_DUST = "Gaia_Dust_Burst";
    private static final String PULLED_EFFECT = "Chaos_Attraction";
    private static final String SFX_OPEN = "SFX_Portal_Void";
    private static final String SFX_BLINK = "SFX_Dungeon_Goblin_Portal_Void_Teleport_Cinematic";
    private static final String SFX_SUPERNOVA = "SFX_Gobliterator_Self_Destruct_Explosion";

    /** Bloc en vol : aspire en spirale jusqu'au disque d'accretion, ou recrache vers l'exterieur par la supernova. */
    static final class Debris {
        final Ref<EntityStore> ref;
        final String block;
        final Vector3d start;
        final double angle, radius, landY;
        final long startAt, flight;
        final boolean ejecta;
        Debris(Ref<EntityStore> ref, String block, Vector3d start, double angle, double radius, double landY, long startAt, long flight, boolean ejecta) {
            this.ref = ref; this.block = block; this.start = start; this.angle = angle; this.radius = radius; this.landY = landY;
            this.startAt = startAt; this.flight = flight; this.ejecta = ejecta;
        }
    }

    /** Bloc du disque d'accretion : orbite a plat, se rapproche de l'horizon (rayon en rayons d'horizon) puis plonge. */
    static final class Orbiting {
        final Ref<EntityStore> ref;
        final String block;
        final double infall, lift;                               // vitesse de chute (rayons/s), petit decalage vertical
        double angle, rf;
        Orbiting(Ref<EntityStore> ref, String block, double angle, double rf, double infall, double lift) {
            this.ref = ref; this.block = block; this.angle = angle; this.rf = rf; this.infall = infall; this.lift = lift;
        }
    }

    static final class Abyss {
        final Vector3d center;
        final long startAt;
        final List<Debris> debris = new ArrayList<>();
        final List<Orbiting> disk = new ArrayList<>();
        final Set<Long> tornCells = new HashSet<>();
        final List<String> swallowed = new ArrayList<>();       // blocs engloutis, recraches a la liberation
        Ref<EntityStore> singularity;
        Set<Integer> seenClicks;
        TerrainRestore.Restore torn;
        int blinks;
        float scale = GROW_FROM, shownScale = -1f;
        long lastTick, lastPull, lastBlink, lastVisual, lastAccretion, lastTear, lastRumble, lastFront, lastRiseFx, lastModel, lastJet, lastJetImpact;
        boolean opened, arrived, quasar, imploding, exploded;
        Abyss(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; this.lastTick = startAt; }
        double horizon() { return scale * BASE_HORIZON; }
        float fxScale() { return (float) (horizon() / FX_HORIZON); }
    }

    private static final Map<UUID, List<Abyss>> abysses = new ConcurrentHashMap<>();

    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return;
        Abyss a = new Abyss(SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK_DISTANCE), System.currentTimeMillis());
        a.seenClicks = SpellFx.chains(caster, store, InteractionType.Secondary);
        abysses.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(a);
    }

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Abyss> list = abysses.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Abyss a : list) if (tickAbyss(caster, a, now, store, cb)) list.remove(a);
        if (list.isEmpty()) abysses.remove(uuid);
    }

    private static boolean tickAbyss(Ref<EntityStore> caster, Abyss a, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        long t = now - a.startAt - RISE_MS;                                                  // < 0 : la boule monte encore
        double dt = Math.min(0.25, (now - a.lastTick) / 1000.0);
        a.lastTick = now;
        Vector3d core = corePosition(a, now - a.startAt);
        if (!a.opened) { a.opened = true; open(caster, a, core, store, cb); }
        blink(caster, a, now, store, cb);
        // Taille : minuscule au sol, x1 en haut, x5 a la fin de l'attraction, pulse en quasar, puis implosion
        if (!a.exploded) {
            a.scale = size(a, t, now - a.startAt);
            SpellFx.hold(a.singularity, core, null, store);
            resize(a, now, t >= PULL_MS + QUASAR_MS, cb);
            if (now - a.lastVisual >= VISUAL_EVERY_MS) {                                   // anneau de photons, lentille
                a.lastVisual = now;
                SpellFx.fx(FX_BLACK_HOLE, core, a.fxScale(), store, cb);
            }
            if (now - a.lastAccretion >= ACCRETION_FX_EVERY_MS && t < PULL_MS + QUASAR_MS) {
                a.lastAccretion = now;
                SpellFx.fx(FX_ACCRETION, core, a.fxScale(), store, cb);
            }
        }
        if (t < 0) { rise(a, core, now, store, cb); return false; }
        if (!a.arrived) { a.arrived = true; ignite(a, core, store, cb); }
        if (t < RELEASE_AT && now - a.lastPull >= PULL_EVERY_MS) {
            a.lastPull = now;
            pull(caster, core, a.horizon(), t, store, cb);
        }
        if (t < PULL_MS) {
            if (now - a.lastRumble >= RUMBLE_EVERY_MS) {
                a.lastRumble = now;
                SpellFx.sound(SFX_OPEN, core, cb);
                SpellFx.sound(SFX_OPEN, a.center, cb);
                SpellFx.shake(a.center, RADIUS * 2, t > PULL_MS / 2 ? "Impact_Strong" : "Impact_Light", store);
            }
            if (now - a.lastFront >= FRONT_EVERY_MS) {                                       // limite de la zone d'attraction
                a.lastFront = now;
                double reach = pullReach(t);
                World world = SpellFx.world(store);
                double off = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
                for (int k = 0; k < 14; k++) {
                    double ang = off + Math.PI * 2 * k / 14;
                    SpellFx.fx(FX_DUST, SpellFx.ground(world, new Vector3d(a.center).add(Math.cos(ang) * reach, 0, Math.sin(ang) * reach)).add(0, 0.5, 0), 1.6f, store, cb);
                }
            }
            if (now - a.lastTear >= TEAR_EVERY_MS && t < PULL_MS - DEBRIS_FLIGHT_MS) {
                a.lastTear = now;
                tearBlocks(caster, a, core, t, now, store, cb);
            }
        } else if (t < PULL_MS + QUASAR_MS) {
            quasar(caster, a, core, now, store, cb);
        } else if (!a.imploding) {
            a.imploding = true;                                                                // silence : tout est englouti
            SpellFx.fx(FX_FLASH, core, 4.0f, store, cb);
            SpellFx.sound(SFX_OPEN, core, cb);
        }
        tickDebris(a, core, now, store, cb);
        if (!a.exploded) tickDisk(a, core, dt, a.imploding ? 12.0 : 1.0, store, cb);
        if (!a.exploded && t >= RELEASE_AT) {
            a.exploded = true;
            SpellFx.remove(a.singularity, cb);
            release(caster, a, core, now, store, cb);
        }
        if (a.exploded) afterglow(a, core, t, now, store, cb);
        if (t >= END_MS && a.debris.isEmpty()) {
            for (Orbiting o : a.disk) SpellFx.remove(o.ref, cb);
            a.disk.clear();
            if (a.torn != null) TerrainRestore.save();
            return true;
        }
        return false;
    }

    /**
     * Taille (multiple de BASE_HORIZON) : minuscule au sol, grossit pendant l'ascension jusqu'a x1, puis jusqu'a x5
     * pendant l'attraction ; pulse en quasar ; se contracte en un point a l'implosion.
     */
    private static float size(Abyss a, long t, long elapsed) {
        if (t < 0) {
            double f = Math.min(1.0, elapsed / (double) RISE_MS);
            return (float) (SPAWN_SCALE + (GROW_FROM - SPAWN_SCALE) * f * f);
        }
        if (t < PULL_MS) return GROW_FROM + (GROW_TO - GROW_FROM) * (float) t / PULL_MS;
        if (t < PULL_MS + QUASAR_MS) return GROW_TO * (float) (1 + 0.05 * Math.sin(t / 90.0));
        double f = Math.min(1.0, (t - PULL_MS - QUASAR_MS) / (double) IMPLODE_MS);
        return Math.max(0.02f, (float) (GROW_TO * Math.pow(1 - f, 2.5)));
    }

    /**
     * Quasar : le trou noir gave crache deux jets relativistes aux poles. Le jet du bas perce le sol sous lui
     * (cratere qui se creuse, explosions) ; tout ennemi pris dans un jet brule et est projete le long de l'axe.
     */
    private static void quasar(Ref<EntityStore> caster, Abyss a, Vector3d core, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (!a.quasar) {
            a.quasar = true;
            SpellFx.fx(FX_FLASH, core, 10.0f, store, cb);
            SpellFx.sound(SFX_SUPERNOVA, core, cb);
            SpellFx.sound(SFX_OPEN, a.center, cb);
        }
        if (now - a.lastJet >= JET_EVERY_MS) {
            a.lastJet = now;
            SpellFx.fx(FX_JET, core, 1.0f, store, cb);
            double radius = JET_RADIUS + a.horizon() * 0.15;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, core, PULL_RADIUS + 70, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || Math.hypot(p.x - core.x, p.z - core.z) > radius) continue;
                SpellFx.damage(caster, e, "Physical", JET_DAMAGE, new Vector3d(0, p.y >= core.y ? 18 : -18, 0), "Impact_Strong", cb);
            }
        }
        if (now - a.lastJetImpact >= JET_IMPACT_EVERY_MS) {                                  // le jet du bas frappe le sol
            a.lastJetImpact = now;
            Vector3d impact = SpellFx.ground(world, new Vector3d(a.center));
            SpellFx.fx(FX_EXPLOSION, new Vector3d(impact).add(0, 1, 0), 4.0f, store, cb);
            SpellFx.fx(FX_SHOCKWAVE, new Vector3d(impact).add(0, 0.2, 0), 3.0f, store, cb);
            SpellFx.shake(a.center, RADIUS * 3, "Impact_Strong", store);
            if (a.torn == null) a.torn = TerrainRestore.open(world, a.startAt + RESTORE_AFTER_MS);
            TerrainRestore.Restore batch = a.torn;
            world.execute(() -> {
                try { crater(world, batch, impact, JET_CRATER_RADIUS, JET_CRATER_DEPTH); }
                catch (Exception ex) { EldaniorLogger.error("ChaosAbyss: impact du jet", ex); }
            });
        }
    }

    /** Apres la liberation : le trou noir s'evapore en un anneau qui s'eteint et quelques etincelles. */
    private static void afterglow(Abyss a, Vector3d core, long t, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (t >= END_MS || now - a.lastVisual < VISUAL_EVERY_MS) return;
        a.lastVisual = now;
        double f = (t - RELEASE_AT) / (double) AFTERGLOW_MS;
        float ring = (float) (0.6 * (1 - f));
        if (ring > 0.05f) SpellFx.fx(FX_BLACK_HOLE, core, ring, store, cb);
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < 3; i++) {
            Vector3d at = new Vector3d(core).add(rng.nextDouble(-6, 6), rng.nextDouble(-6, 6), rng.nextDouble(-6, 6));
            SpellFx.fx(FX_BURST, at, (float) (0.5 * (1 - f) + 0.2), store, cb);
        }
    }

    /**
     * Applique la taille au modele : le modele est remplace par une copie a l'echelle (envoyee aux clients avec le
     * modele), au plus toutes les MODEL_UPDATE_MS (plus souvent pendant l'effondrement).
     */
    private static void resize(Abyss a, long now, boolean collapsing, CommandBuffer<EntityStore> cb) {
        if (a.singularity == null || !a.singularity.isValid()) return;
        float s = (float) (a.horizon() * MODEL_SCALE_PER_BLOCK);
        if (a.shownScale > 0 && (Math.abs(s - a.shownScale) < 0.5f || now - a.lastModel < (collapsing ? MODEL_UPDATE_COLLAPSE_MS : MODEL_UPDATE_MS))) return;
        ModelAsset asset = ModelAsset.getAssetMap().getAsset(SINGULARITY_MODEL);
        if (asset == null) return;
        a.shownScale = s;
        a.lastModel = now;
        // Boite fixe (40 blocs de large, quasi plate) : assez grande pour ne pas etre masquee au loin, sans grossir avec le modele
        Box box = new Box(-HITBOX_HALF / s, -0.2 / s, -0.2 / s, HITBOX_HALF / s, 0.2 / s, 0.2 / s);
        cb.putComponent(a.singularity, ModelComponent.getComponentType(), new ModelComponent(Model.createScaledModel(asset, s, asset.generateRandomAttachmentIds(), box)));
    }

    /** Position du trou noir : jaillit du sol et monte (en ralentissant a l'arrivee) jusqu'a sa hauteur. */
    private static Vector3d corePosition(Abyss a, long elapsed) {
        double f = Math.min(1.0, elapsed / (double) RISE_MS), ease = 1 - Math.pow(1 - f, 3);
        double start = 0.6;                                                                       // posee sur le sol au depart
        return new Vector3d(a.center).add(0, start + (SINGULARITY_HEIGHT - start) * ease, 0);
    }

    /** Ascension : poussiere et sol qui se soulevent au point de depart, trainee sous la boule, grondement. */
    private static void rise(Abyss a, Vector3d core, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now - a.lastRiseFx < RISE_FX_EVERY_MS) return;
        a.lastRiseFx = now;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        SpellFx.fx(FX_BURST, new Vector3d(core).add(0, -a.horizon() - 1, 0), 1.8f, store, cb);
        double ang = rng.nextDouble(Math.PI * 2), r = rng.nextDouble(2, 9);
        SpellFx.fx(FX_DUST, new Vector3d(a.center).add(Math.cos(ang) * r, 0.5, Math.sin(ang) * r), 2.0f, store, cb);
        if ((now - a.startAt) / RISE_FX_EVERY_MS % 6 == 0) SpellFx.shake(a.center, RADIUS, "Impact_Light", store);
    }

    /** Arrivee en haut : le trou noir s'ouvre (flash, onde dans le ciel) et le sort commence. */
    private static void ignite(Abyss a, Vector3d core, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_FLASH, core, 12.0f, store, cb);
        SpellFx.fx(FX_SHOCKWAVE, core, (float) (RADIUS / 2.5), store, cb);
        SpellFx.fx(FX_BURST, core, 8.0f, store, cb);
        SpellFx.sound(SFX_OPEN, core, cb);
        SpellFx.sound(SFX_SUPERNOVA, core, cb);
        SpellFx.shake(a.center, PULL_RADIUS * 2, "Impact_Strong", store);
    }

    private static void open(Ref<EntityStore> caster, Abyss a, Vector3d core, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        a.singularity = SpellFx.spawnModel(caster, SINGULARITY_PROJECTILE, core, new Vector3d(1, 0, 0), cb);
        SpellFx.fx(FX_FLASH, core, 1.5f, store, cb);
        SpellFx.fx(FX_BURST, new Vector3d(a.center).add(0, 0.6, 0), 1.2f, store, cb);
        SpellFx.fx(FX_DUST, new Vector3d(a.center).add(0, 0.5, 0), 1.5f, store, cb);
        World world = SpellFx.world(store);                                                // la boule arrache le sol en jaillissant
        if (a.torn == null) a.torn = TerrainRestore.open(world, a.startAt + RESTORE_AFTER_MS);
        TerrainRestore.Restore batch = a.torn;
        Vector3d c = new Vector3d(a.center);
        world.execute(() -> {
            try { crater(world, batch, c, LAUNCH_CRATER_RADIUS, LAUNCH_CRATER_DEPTH); }
            catch (Exception e) { EldaniorLogger.error("ChaosAbyss: cratere de depart", e); }
        });
        SpellFx.sound(SFX_OPEN, core, cb);
        SpellFx.sound(SFX_OPEN, a.center, cb);
        SpellFx.shake(a.center, PULL_RADIUS * 2, "Impact_Strong", store);
    }

    /** Zone d'attraction (distance a l'axe du trou noir) : petite au debut, elle s'elargit tout au long de l'attraction. */
    private static double pullReach(long t) {
        return PULL_REACH_START + (PULL_REACH_END - PULL_REACH_START) * Math.min(1.0, t / (double) PULL_MS);
    }

    /**
     * Attraction des ennemis (joueurs et creatures) dans la zone d'attraction, qui s'elargit avec le temps. Loin du
     * trou noir, la force est faible : on peut lui resister en s'eloignant. Plus on s'en approche, plus elle est
     * irresistible : souleve, aspire, puis entraine en orbite dans le disque d'accretion, dechiquete par les forces
     * de maree pres de l'horizon. Les degats augmentent avec la proximite et avec le temps.
     */
    private static void pull(Ref<EntityStore> caster, Vector3d core, double horizon, long t, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        double orbit = horizon * ENTITY_ORBIT, diskEdge = horizon * DISK_OUTER, reach = pullReach(t);
        double time = t < PULL_MS ? 0.25 + 0.75 * t / (double) PULL_MS : 1.3;              // de plus en plus fort
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, core, Math.hypot(reach, SINGULARITY_HEIGHT) + diskEdge, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d body = new Vector3d(p).add(0, 1, 0);
            double hx = body.x - core.x, hz = body.z - core.z, hr = Math.hypot(hx, hz), dy = core.y - body.y;
            double d = body.distance(core);
            boolean inDisk = d <= diskEdge + 4;
            if (!inDisk && hr > reach) continue;                                                  // hors de la zone d'attraction
            // Force : faible au bord de la zone, irresistible pres du trou noir
            double edge = Math.max(0, 1 - hr / reach), near = Math.max(0, 1 - (d - diskEdge) / 25.0);
            double force = Math.max(Math.pow(edge, 1.4), near);
            Vector3d out = hr > 1e-3 ? new Vector3d(hx / hr, 0, hz / hr) : new Vector3d(1, 0, 0);
            Vector3d tangent = new Vector3d(-out.z, 0, out.x);
            Vector3d v;
            if (inDisk) {                                                                        // dans le disque : orbite
                double radial = Math.max(-8, Math.min(8, (orbit - hr) * 1.3));
                double vertical = Math.max(-10, Math.min(10, dy * 1.6)) + 1.5;
                v = new Vector3d(out).mul(radial).add(new Vector3d(tangent).mul(9 + 6 * time)).add(0, vertical, 0);
            } else {
                Vector3d in = new Vector3d(core).sub(body).div(d);
                double speed = 2 + 24 * force * time;
                v = new Vector3d(in).mul(speed).add(new Vector3d(tangent).mul((2 + 8 * force) * time)).add(0, 0.6 + 2.2 * force, 0);
            }
            float dmg = (float) ((PULL_DAMAGE_MIN + (PULL_DAMAGE_MAX - PULL_DAMAGE_MIN) * force) * time);
            if (d <= horizon * 1.4) dmg += TIDAL_DAMAGE;                                     // dechiquete par les forces de maree
            SpellFx.damage(caster, e, "Physical", dmg, v, null, cb);
            SpellFx.effect(e, PULLED_EFFECT, cb);
        }
    }

    /** Rayon jusqu'ou le sol est devore : part du centre et depasse la zone aux deux tiers de l'attraction. */
    private static double tearReach(long t) {
        return DEBRIS_MIN_R + (DEBRIS_MAX_R - DEBRIS_MIN_R) * Math.min(1.0, t / (PULL_MS * 0.66));
    }

    /**
     * Devore le sol : a chaque impulsion, des colonnes de blocs (arbres compris) sont arrachees, plus profondement
     * sous le trou noir ; chaque bloc s'envole (entite-bloc du moteur) et sa case est videe (restauree plus tard).
     * Contenus, zones protegees et blocs sous l'eau ne sont pas touches.
     */
    private static void tearBlocks(Ref<EntityStore> caster, Abyss a, Vector3d core, long t, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        if (a.torn == null) a.torn = TerrainRestore.open(world, a.startAt + RESTORE_AFTER_MS);
        TerrainRestore.Restore batch = a.torn;
        double reach = tearReach(t);
        List<int[]> cells = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (int k = 0; k < TEAR_PER_PULSE; k++) {
            double ang = rng.nextDouble(Math.PI * 2), r = Math.sqrt(rng.nextDouble()) * reach;
            Vector3d ground = SpellFx.ground(world, new Vector3d(a.center).add(Math.cos(ang) * r, 0, Math.sin(ang) * r));
            int bx = (int) Math.floor(ground.x), top = (int) Math.floor(ground.y) - 1, bz = (int) Math.floor(ground.z);
            if (TerrainRestore.isProtectedArea(world.getName(), bx, top, bz) || WorldBlockUtil.getFluidIndex(world, bx, top + 1, bz) > 0) continue;
            int depth = 1 + rng.nextInt(TEAR_MAX_DEPTH) + (int) Math.round(TEAR_CENTER_BONUS * Math.max(0, 1 - r / (RADIUS * 0.5)));
            for (int by = top; by > top - depth; by--) {
                if (!a.tornCells.add(cellKey(bx, by, bz))) continue;                       // deja arrache (retrait en attente)
                BlockType type = WorldBlockUtil.getBlockType(world, bx, by, bz);
                if (type == null || TerrainRestore.isEmpty(world, bx, by, bz)) continue;
                String id = type.getId();
                if (TerrainRestore.isKept(world, id, bx, by, bz) || WorldBlockUtil.getFluidIndex(world, bx, by, bz) > 0) break;
                cells.add(new int[]{bx, by, bz});
                ids.add(id);
                if (a.debris.size() >= MAX_FLYING) continue;                               // trop de blocs en vol : il disparait
                Vector3d from = new Vector3d(bx + 0.5, by + 0.5, bz + 0.5);
                long flight = DEBRIS_FLIGHT_MS + (long) (from.distance(core) * DEBRIS_FLIGHT_PER_BLOCK_MS);
                a.debris.add(new Debris(spawnBlock(caster, id, from, store, cb), id, from, Math.atan2(from.z - core.z, from.x - core.x),
                        Math.hypot(from.x - core.x, from.z - core.z), from.y, now, flight, false));
            }
            if (k % 3 == 0) SpellFx.fx(FX_DUST, new Vector3d(bx + 0.5, top + 1, bz + 0.5), 1.0f, store, cb);
        }
        if (cells.isEmpty()) return;
        world.execute(() -> {
            try {
                for (int i = 0; i < cells.size(); i++) {
                    int[] c = cells.get(i);
                    BlockType type = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                    if (type == null || !ids.get(i).equals(type.getId())) continue;
                    TerrainRestore.place(world, batch, c[0], c[1], c[2], ids.get(i), TerrainRestore.EMPTY, 0);
                    BlockType above = WorldBlockUtil.getBlockType(world, c[0], c[1] + 1, c[2]);
                    if (above != null && above.getId().startsWith("Plant_") && !above.getId().contains("Leaves")) {
                        TerrainRestore.place(world, batch, c[0], c[1] + 1, c[2], above.getId(), TerrainRestore.EMPTY, 0);
                    }
                }
            } catch (Exception e) {
                EldaniorLogger.error("ChaosAbyss: blocs arraches", e);
            }
        });
    }

    /** Le vrai bloc (entite-bloc du moteur), deplace par le code ; modele de debris a defaut. */
    private static Ref<EntityStore> spawnBlock(Ref<EntityStore> caster, String blockId, Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = SpellFx.spawnBlock(blockId, pos, store, cb);
        return ref != null ? ref : SpellFx.spawnModel(caster, DEBRIS_PROJECTILE, pos, new Vector3d(1, 0, 0), cb);
    }

    private static long cellKey(int x, int y, int z) {
        return (((long) x & 0x1FFFFF) << 42) | (((long) y & 0x1FFFFF) << 21) | ((long) z & 0x1FFFFF);
    }

    /**
     * Fait voler les blocs : arraches, ils montent de plus en plus vite en tournant jusqu'au bord du disque
     * d'accretion qu'ils rejoignent ; recraches, ils partent en cloche vers l'exterieur et retombent au sol.
     */
    private static void tickDebris(Abyss a, Vector3d core, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        double edge = a.horizon() * DISK_OUTER;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        a.debris.removeIf(d -> {
            double p = Math.min(1.0, (now - d.startAt) / (double) d.flight);
            Vector3d pos;
            if (!d.ejecta) {
                double r = edge + (d.radius - edge) * Math.pow(1 - p, 1.3), ang = d.angle + p * Math.PI * 1.5;
                pos = new Vector3d(core.x + Math.cos(ang) * r, d.start.y + (core.y - d.start.y) * Math.pow(p, 1.6), core.z + Math.sin(ang) * r);
                if (p >= 1 && !a.exploded) {                                                // rejoint le disque d'accretion
                    if (a.disk.size() < MAX_DISK && d.ref != null) {
                        a.disk.add(new Orbiting(d.ref, d.block, ang, DISK_OUTER * rng.nextDouble(0.75, 1.0), rng.nextDouble(0.35, 0.7), rng.nextDouble(-0.05, 0.05)));
                    } else {
                        swallow(a, d.block);
                        SpellFx.remove(d.ref, cb);
                    }
                    return true;
                }
            } else {
                double r = d.radius * p;
                pos = new Vector3d(d.start.x + Math.cos(d.angle) * r, d.start.y + 10 * Math.sin(Math.PI * p) - (d.start.y - d.landY) * p * p,
                        d.start.z + Math.sin(d.angle) * r);
            }
            Rotation3f spin = new Rotation3f();
            spin.setYaw((float) (p * Math.PI * 6 + d.angle));
            spin.setPitch((float) (p * Math.PI * 4));
            SpellFx.hold(d.ref, pos, spin, store);
            if (p >= 1) {
                if (rng.nextInt(4) == 0) SpellFx.fx(FX_DUST, pos, 1.2f, store, cb);
                SpellFx.remove(d.ref, cb);
                return true;
            }
            return false;
        });
    }

    /**
     * Disque d'accretion : chaque bloc tourne a plat (plus vite pres du centre, comme une orbite de Kepler) en se
     * rapprochant de l'horizon, puis plonge dedans et disparait.
     */
    private static void tickDisk(Abyss a, Vector3d core, double dt, double rush, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        double horizon = a.horizon();
        a.disk.removeIf(o -> {
            o.rf -= o.infall * dt * rush * (o.rf < DISK_INNER ? 4 : 1);                   // sous le bord interieur : plongeon
            o.angle += ORBIT_SPEED * Math.pow(DISK_OUTER / Math.max(1.0, o.rf), 1.5) * dt;
            if (o.rf <= 1.0) {
                swallow(a, o.block);
                if (ThreadLocalRandom.current().nextInt(5) == 0) {
                    SpellFx.fx(FX_BURST, new Vector3d(core).add(Math.cos(o.angle) * horizon, 0, Math.sin(o.angle) * horizon), 0.6f, store, cb);
                }
                SpellFx.remove(o.ref, cb);
                return true;
            }
            Vector3d pos = new Vector3d(core).add(Math.cos(o.angle) * o.rf * horizon, o.lift * horizon, Math.sin(o.angle) * o.rf * horizon);
            Rotation3f rot = new Rotation3f();
            rot.setYaw((float) -o.angle);
            SpellFx.hold(o.ref, pos, rot, store);
            return false;
        });
    }

    private static void swallow(Abyss a, String block) {
        if (block != null && a.swallowed.size() < MAX_EJECTA) a.swallowed.add(block);
    }

    /**
     * Liberation : l'horizon contracte en un point se rompt. Eclair blanc, onde de choc spherique et au sol, la matiere
     * engloutie (et ce qui restait en vol) est recrachee dans toutes les directions, immense cratere, degats et projection.
     */
    private static void release(Ref<EntityStore> caster, Abyss a, Vector3d core, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_FLASH, core, 22.0f, store, cb);
        SpellFx.fx(FX_EXPLOSION, core, 14.0f, store, cb);
        SpellFx.fx(FX_BURST, core, 16.0f, store, cb);
        SpellFx.fx(FX_SHOCKWAVE, new Vector3d(core), (float) (SUPERNOVA_RADIUS / 2.0), store, cb);
        SpellFx.fx(FX_SHOCKWAVE, new Vector3d(a.center).add(0, 0.1, 0), (float) (SUPERNOVA_RADIUS / 2.0), store, cb);
        SpellFx.fx(FX_EXPLOSION, new Vector3d(a.center).add(0, 1, 0), 10.0f, store, cb);
        SpellFx.sound(SFX_SUPERNOVA, core, cb);
        SpellFx.sound(SFX_SUPERNOVA, a.center, cb);
        SpellFx.shake(core, SUPERNOVA_RADIUS * 5, "Impact_Strong", store);
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        // Ce qui restait en vol ou dans le disque, puis la matiere engloutie, recraches dans toutes les directions
        List<Ref<EntityStore>> leftovers = new ArrayList<>();
        for (Orbiting o : a.disk) leftovers.add(o.ref);
        for (Debris d : a.debris) if (!d.ejecta) leftovers.add(d.ref);
        a.disk.clear();
        a.debris.removeIf(d -> !d.ejecta);
        for (Ref<EntityStore> ref : leftovers) {
            Vector3d from = ref != null ? SpellFx.position(ref, store) : null;
            if (from == null) { SpellFx.remove(ref, cb); continue; }
            a.debris.add(ejecta(a, ref, null, from, core, rng, now));
        }
        for (String block : a.swallowed) {
            Vector3d from = new Vector3d(core).add(rng.nextDouble(-1, 1), rng.nextDouble(-1, 1), rng.nextDouble(-1, 1));
            a.debris.add(ejecta(a, spawnBlock(caster, block, from, store, cb), block, from, core, rng, now));
        }
        a.swallowed.clear();
        // Immense cratere sous le trou noir
        World world = SpellFx.world(store);
        if (a.torn == null) a.torn = TerrainRestore.open(world, a.startAt + RESTORE_AFTER_MS);
        TerrainRestore.Restore batch = a.torn;
        Vector3d c = new Vector3d(a.center);
        world.execute(() -> {
            try { crater(world, batch, c, CRATER_RADIUS, CRATER_DEPTH); }
            catch (Exception e) { EldaniorLogger.error("ChaosAbyss: cratere", e); }
        });
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, core, SUPERNOVA_RADIUS + SINGULARITY_HEIGHT, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            double ratio = 1 - 0.5 * Math.min(1.0, p.distance(core) / (SUPERNOVA_RADIUS + SINGULARITY_HEIGHT));
            Vector3d out = new Vector3d(p).sub(core);
            out.y = 0;
            if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
            out.normalize().mul(SUPERNOVA_PUSH * ratio).add(0, SUPERNOVA_LIFT * ratio, 0);
            SpellFx.damage(caster, e, "Physical", (float) (SUPERNOVA_DAMAGE * ratio), out, "Impact_Strong", cb);
        }
    }

    /** Bloc recrache : part du trou noir dans une direction au hasard, en cloche, et retombe au sol. */
    private static Debris ejecta(Abyss a, Ref<EntityStore> ref, String block, Vector3d from, Vector3d core, ThreadLocalRandom rng, long now) {
        double ang = Math.atan2(from.z - core.z, from.x - core.x) + rng.nextDouble(-0.6, 0.6);
        return new Debris(ref, block, from, ang, SUPERNOVA_RADIUS * 0.4 + rng.nextDouble(SUPERNOVA_RADIUS * 0.9), a.center.y,
                now, EJECTA_FLIGHT_MS + rng.nextLong(900), true);
    }

    /** Cratere en cuvette : la surface est soufflee, jusqu'a maxDepth blocs au centre (hors tick ECS). */
    private static void crater(World world, TerrainRestore.Restore batch, Vector3d c, double radius, int maxDepth) {
        int r = (int) Math.ceil(radius), cx = (int) Math.floor(c.x), cy = (int) Math.floor(c.y), cz = (int) Math.floor(c.z);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.hypot(dx, dz);
                if (d > radius) continue;
                int depth = (int) Math.ceil(maxDepth * Math.sqrt(1 - (d / radius) * (d / radius)));
                int x = cx + dx, z = cz + dz;
                if (TerrainRestore.isProtectedArea(world.getName(), x, cy, z)) continue;
                for (int y = cy + 10; y >= cy - maxDepth - 10 && depth > 0; y--) {
                    var type = WorldBlockUtil.getBlockType(world, x, y, z);
                    if (type == null) break;
                    if (TerrainRestore.isEmpty(world, x, y, z)) continue;
                    String id = type.getId();
                    if (TerrainRestore.isKept(world, id, x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) break;
                    TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0);
                    if (!id.startsWith("Plant_")) depth--;
                }
            }
        }
    }

    /** Pas de l'espace : un clic droit pendant le sort teleporte le lanceur au point vise. */
    private static void blink(Ref<EntityStore> caster, Abyss a, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Set<Integer> current = SpellFx.chains(caster, store, InteractionType.Secondary);
        boolean clicked = false;
        for (Integer id : current) if (!a.seenClicks.contains(id)) { clicked = true; break; }
        a.seenClicks = current;
        if (!clicked || a.blinks >= BLINK_CHARGES || now - a.lastBlink < BLINK_COOLDOWN_MS) return;
        Vector3d from = SpellFx.position(caster, store);
        if (from == null) return;
        Vector3i block = TargetUtil.getTargetBlock(caster, BLINK_RANGE, store);
        Vector3d to = block != null ? new Vector3d(block.x + 0.5, block.y + 1.0, block.z + 0.5)
                : SpellFx.ground(SpellFx.world(store), new Vector3d(from).add(SpellFx.forward(caster, store).mul(BLINK_RANGE)));
        a.blinks++;
        a.lastBlink = now;
        SpellFx.fx(FX_BURST, new Vector3d(from).add(0, 1, 0), 2.5f, store, cb);
        SpellFx.teleport(caster, to, store, cb);
        SpellFx.protectFromFall(caster, 2_000, store);
        SpellFx.fx(FX_BURST, new Vector3d(to).add(0, 1, 0), 2.5f, store, cb);
        SpellFx.sound(SFX_BLINK, to, cb);
    }

    public static void handleDisconnect(UUID uuid) {
        if (abysses.remove(uuid) != null) TerrainRestore.save();
    }
}
