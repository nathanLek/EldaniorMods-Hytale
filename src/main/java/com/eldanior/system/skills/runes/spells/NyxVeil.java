package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.builtin.beam.AttachedBeam;
import com.hypixel.hytale.builtin.beam.BeamComponent;
import com.hypixel.hytale.builtin.beam.asset.Beam;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MouseButtonState;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Le Trone du Neant Eternel — sort divin de tenebres (Rune_VoileNyx) : le lanceur devient maitre de la mort et du
 * neant sur tout un royaume ({@link #REALM} blocs autour de lui) pendant {@link #END_MS}.
 *  - Le royaume : un globe de goudron noir se referme sur la zone (on peut y entrer, on n'en sort pas) ; sous les pieds,
 *    tout le sol du globe devient noir (sur {@link #GROUND_DEPTH} blocs) sous un tapis de brume noire ; seul le trone
 *    eclaire le royaume.
 *  - Le trone : le trone de ferraille du jeu, noirci aux reflets violets, s'eleve et flotte au centre ; le lanceur y
 *    prend place, une couronne noire a la petite flamme violette au-dessus de la tete. Dans le royaume : +50 % de
 *    degats infliges, -30 % subis.
 *  - Les ombres : chaque adversaire voit son ombre se dresser, sa copie noire un peu plus faible (90 % de sa vie et de
 *    sa force). Elle court sur lui, tourne autour, se fend et frappe ; un monstre au contact la frappe en retour, un
 *    joueur la frappe au clic. Abattue, elle se reforme quelques secondes plus tard.
 *  - Les gestes du souverain : clic gauche, quatre chaines violettes jaillissent du sol comme des grappins et
 *    enchainent l'ennemi vise jusqu'a la fin du sort ; clic droit, le retour au neant : le sol s'ouvre et aspire
 *    lentement l'ennemi vise, puis se referme en sol noir.
 * A la fin, les ombres fondent, le trone redescend dans le sol, le globe se dissout et la terre reprend ses couleurs.
 * Le lanceur, son groupe, sa guilde et sa famille sont epargnes.
 */
public final class NyxVeil {

    private NyxVeil() {}

    // ===== Royaume =====
    static final double REALM = 30.0;
    private static final long BUILD_MS = 3_000, UNDER_MS = 6_000, FINALE_AT = 58_000, END_MS = 60_000;
    private static final int GROUND_DEPTH = 6;                          // le sol du globe noircit sur 6 blocs
    private static final double INSIDE_MARGIN = 2.5, PULL_IN_BAND = 2.5;
    private static final long BORDER_EVERY_MS = 100, MIST_EVERY_MS = 2_500, MARK_EVERY_MS = 1_000, SCAN_EVERY_MS = 400;
    private static final double MIST_SPACING = 3.5;
    private static final long LEVITATION_FX_EVERY_MS = 600;
    private static final float DAMAGE_DEALT = 1.5f, DAMAGE_TAKEN = 0.7f;
    // Trone flottant (taille du trone du jeu)
    private static final int THRONE_HOVER = 5;
    private static final double SEAT_OFFSET = 0.9, THRONE_RISE_FROM = -4.0;
    // Ombres (copies menees par le code)
    private static final int MAX_SHADOWS = 25;
    private static final long RISE_MS = 800, LUNGE_MS = 220, RETREAT_MS = 400, FLINCH_MS = 350;
    private static final long STRIKE_MIN_MS = 900, STRIKE_MAX_MS = 1_400, RESPAWN_MS = 6_000, LOCK_EVERY_MS = 500, MOB_HIT_EVERY_MS = 1_300;
    private static final double RUN_SPEED = 6.5, WALK_SPEED = 4.0, LUNGE_SPEED = 14.0, RETREAT_SPEED = 7.0;
    private static final double ENGAGE = 2.6, CIRCLE = 1.9, LUNGE_REACH = 0.7, RETREAT_TO = 3.0, CIRCLE_SPEED = 1.6, MOB_REACH = 2.4;
    private static final float SHADOW_STRENGTH = 0.9f, POWER_PER_MAX_HP = 0.07f, MIN_POWER = 5f;
    private static final float MANA_STEAL = 0.005f;
    // Gestes : chaines de l'enfer et retour au neant
    private static final double AIM_RANGE = 60.0, AIM_ALIGN = 0.9;
    private static final long CHAIN_COOLDOWN_MS = 1_000, CHAIN_SHOOT_MS = 380, CHAIN_STAGGER_MS = 70, CHAIN_TICK_MS = 1_000, CHAIN_REFRESH_MS = 100;
    private static final long CHAIN_PULSE_EVERY_MS = 1_600, CHAIN_PULSE_MS = 220;
    private static final int CHAINS = 4;
    private static final double CHAIN_ANCHOR = 1.6, CHAIN_ANCHOR_PER_WIDTH = 0.9, CHAIN_ATTACH_RATIO = 0.65, CHAIN_YANK = 0.35;
    private static final float CHAIN_SCALE = 0.8f;
    private static final float CHAIN_MANA_DRAIN = 0.04f;                // les chaines aspirent 4 % du mana max par seconde (rendu au souverain)
    private static final long ANCHOR_FX_EVERY_MS = 900;
    // Chaines enroulees autour du corps : deux ceintures de segments (taille, torse), comme les grappins
    private static final int WRAP_SEGMENTS = 6;
    private static final double[] WRAP_HEIGHTS = {0.42, 0.68};
    private static final double WRAP_MARGIN = 0.12;
    private static final float WRAP_SCALE = 0.45f;
    private static final long VOID_COOLDOWN_MS = 3_000, VOID_MS = 4_500, VOID_TICK_MS = 250;
    private static final int VOID_DEPTH = 6;
    private static final double VOID_SINK_SPEED = 2.2;
    private static final float VOID_DAMAGE = 100f;
    // Blocs
    private static final String TAR = "Nyx_Goudron";

    // ===== Assets =====
    private static final String THRONE_PROJECTILE = "Projectile_Config_Nyx_Throne";
    private static final String SHADOW_PROJECTILE = "Projectile_Config_Chronos_Ghost";   // porteur invisible habille par le code
    private static final String CHAIN_BEAM = "Nyx_Chain";
    private static final String FX_MIST = "Nyx_Mist";
    private static final String FX_DRAIN = "Nyx_Drain_Burst";
    private static final String FX_SMOKE = "Divine_Smoke_Short";
    private static final String FX_VOID = "Nyx_Void";
    private static final String SOVEREIGN_EFFECT = "Nyx_Ombre", CROWN_EFFECT = "Nyx_Couronne";
    private static final String MARK_EFFECT = "Nyx_Marque";
    private static final String CLONE_EFFECT = "Nyx_Clone";
    private static final String BOUND_EFFECT = "Nyx_Etreinte", CHAINED_EFFECT = "Nyx_Enchaine";
    private static final String SFX_CAST = "SFX_Dragon_Void_Finale_Roar";
    private static final String SFX_RISE = "SFX_Bear_Voidtaken_Charge_Roar";
    private static final String SFX_STRIKE = "SFX_Daggers_T2_Slash_Impact";
    private static final String SFX_CHAIN_SHOT = "SFX_Ability_Chainhook_Shot", SFX_CHAIN_HIT = "SFX_Ability_Chainhook_Hit", SFX_CHAIN_CLANK = "SFX_Lever_Metal_On";
    private static final String FX_CHAIN_BURST = "Nyx_Chain_Burst", FX_CHAIN_ANCHOR = "Nyx_Chain_Anchor";
    private static final String SFX_VOID = "SFX_Emit_Dungeon_Goblin_Void_Strike";
    private static final String SFX_PASS = "SFX_Dungeon_Goblin_Portal_Void_Teleport_Cinematic";

    record Cell(int x, int y, int z) {}
    record Click(MouseButtonType button, Ref<EntityStore> target) {}

    /** Ombre d'un joueur ennemi : sa copie voilee de noir, menee par le code. */
    static final class Shadow {
        final Ref<EntityStore> ref, owner;
        Ref<EntityStore> target;
        final Vector3d pos;
        final long bornAt;
        final float power;
        double circle;
        int dir;
        float hp;
        long nextStrike, lungeAt = -1, retreatUntil, flinchUntil, lastLock, lastMobHit;
        boolean dressed, struck;
        MovementStates states;
        Shadow(Ref<EntityStore> ref, Ref<EntityStore> owner, Vector3d pos, float ownerMaxHp, long now, ThreadLocalRandom rng) {
            this.ref = ref; this.owner = owner; this.target = owner; this.pos = pos; this.bornAt = now;
            this.hp = ownerMaxHp * SHADOW_STRENGTH;
            this.power = Math.max(MIN_POWER, ownerMaxHp * POWER_PER_MAX_HP);
            this.circle = rng.nextDouble(Math.PI * 2); this.dir = rng.nextBoolean() ? 1 : -1; this.nextStrike = now + RISE_MS + 400;
        }
    }

    /** Ennemi enchaine : quatre chaines (faisceaux) qui jaillissent du sol puis s'accrochent a lui. */
    static final class Chained {
        final Ref<EntityStore> target;
        final List<Vector3d> anchors;
        final List<Ref<EntityStore>> beams = new ArrayList<>(), wraps = new ArrayList<>();
        final long startAt;
        final double attach, height, width;                             // hauteur d'accroche et gabarit de la creature
        long lastRefresh, pulseAt, lastAnchorFx;
        boolean attached;
        Chained(Ref<EntityStore> target, List<Vector3d> anchors, long startAt, double attach, double height, double width) {
            this.target = target; this.anchors = anchors; this.startAt = startAt; this.attach = attach; this.height = height; this.width = width; this.pulseAt = startAt + CHAIN_SHOOT_MS + CHAIN_PULSE_EVERY_MS;
        }
    }

    static final class Sinking {
        final Ref<EntityStore> target;
        final Vector3d at;
        final long startAt;
        final Map<Cell, String> carved = new HashMap<>();
        long lastTick;
        Sinking(Ref<EntityStore> target, Vector3d at, long startAt) { this.target = target; this.at = at; this.startAt = startAt; }
    }

    static final class Realm {
        final UUID owner;
        final Ref<EntityStore> caster;
        final Vector3d center, front;
        final long startAt;
        final List<Cell> dome = new ArrayList<>(), under = new ArrayList<>();
        final List<long[]> columns = new ArrayList<>();
        final Map<Ref<EntityStore>, Shadow> shadows = new HashMap<>();
        final Map<Ref<EntityStore>, Long> respawnAt = new HashMap<>();
        final List<Chained> chains = new ArrayList<>();
        final List<Sinking> sinkings = new ArrayList<>();
        final List<int[]> fluids = new ArrayList<>();                    // eau / lave retirees : x, y, z, index du fluide
        Set<Integer> seenPrimary, seenSecondary;
        TerrainRestore.Restore domeBatch, underBatch, groundBatch, throneBatch;
        Ref<EntityStore> throne;
        int domePlaced, underPlaced, groundPlaced, domeRemoved;
        long lastTick, lastBorder, lastMist, lastMark, lastScan, lastChain, lastVoid, lastChainTick, lastLevitation;
        boolean seated, finale, groundRestored;
        Realm(UUID owner, Ref<EntityStore> caster, Vector3d center, Vector3d front, long startAt) {
            this.owner = owner; this.caster = caster; this.center = center; this.front = front; this.startAt = startAt; this.lastTick = startAt;
        }
        Vector3d support() { return new Vector3d(center.x, center.y + THRONE_HOVER, center.z); }
        Vector3d seat() { return new Vector3d(center.x, center.y + THRONE_HOVER + 1, center.z); }
    }

    private static final Map<UUID, List<Realm>> realms = new ConcurrentHashMap<>();
    private static final Map<UUID, ConcurrentLinkedQueue<Click>> clicks = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Shadow> shadowIndex = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Integer> shadowHits = new ConcurrentHashMap<>();

    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        Vector3d pos = SpellFx.position(caster, store);
        if (uuid == null || pos == null) return;
        Vector3d center = new Vector3d(Math.floor(pos.x) + 0.5, Math.floor(pos.y), Math.floor(pos.z) + 0.5);
        Realm r = new Realm(uuid, caster, center, SpellFx.forward(caster, store), System.currentTimeMillis());
        r.seenPrimary = SpellFx.chains(caster, store, InteractionType.Primary);
        r.seenSecondary = SpellFx.chains(caster, store, InteractionType.Secondary);
        plan(r);
        realms.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(r);
        clicks.remove(uuid);
        SpellFx.protectFromFall(caster, END_MS + 5_000, store);
        SpellFx.sound(SFX_CAST, center, store);
        SpellFx.shake(center, REALM * 2, "Impact_Strong", store);
    }

    /** Evenement souris : gestes du souverain, coups des joueurs sur les ombres. */
    public static void onMouseButton(PlayerMouseButtonEvent e) {
        try {
            var mb = e.getMouseButton();
            if (mb == null || mb.state != MouseButtonState.Pressed || e.getPlayerRefComponent() == null) return;
            Ref<EntityStore> target = e.getTargetEntityRef();
            if (mb.mouseButtonType == MouseButtonType.Left && target != null && shadowIndex.containsKey(target)) shadowHits.merge(target, 1, Integer::sum);
            UUID uuid = e.getPlayerRefComponent().getUuid();
            if (uuid != null && realms.containsKey(uuid) && (mb.mouseButtonType == MouseButtonType.Left || mb.mouseButtonType == MouseButtonType.Right)) {
                clicks.computeIfAbsent(uuid, k -> new ConcurrentLinkedQueue<>()).add(new Click(mb.mouseButtonType, target));
            }
        } catch (Exception ex) {
            EldaniorLogger.error("NyxVeil: clic", ex);
        }
    }

    /** +50 % de degats pour le souverain dans son royaume. */
    public static float damageMultiplier(Ref<EntityStore> attacker) {
        return inOwnRealm(attacker) ? DAMAGE_DEALT : 1f;
    }

    public static float damageTakenMultiplier(Ref<EntityStore> target) {
        return inOwnRealm(target) ? DAMAGE_TAKEN : 1f;
    }

    private static boolean inOwnRealm(Ref<EntityStore> ref) {
        if (realms.isEmpty() || ref == null || !ref.isValid()) return false;
        long now = System.currentTimeMillis();
        for (List<Realm> list : realms.values()) {
            for (Realm r : list) {
                long t = now - r.startAt;
                if (!r.caster.equals(ref) || t < BUILD_MS || t >= FINALE_AT) continue;
                Vector3d p = SpellFx.position(ref, ref.getStore());
                if (p != null && p.distance(r.center) <= REALM) return true;
            }
        }
        return false;
    }

    // ===================== PLAN =====================

    /** Globe : moitie haute (dans l'air, construite d'abord) et moitie basse (dans le sol) ; colonnes du sol. */
    private static void plan(Realm r) {
        int R = (int) Math.ceil(REALM) + 1, cx = (int) Math.floor(r.center.x), cy = (int) Math.floor(r.center.y), cz = (int) Math.floor(r.center.z);
        for (int dy = -R; dy <= R; dy++) {
            for (int dx = -R; dx <= R; dx++) {
                for (int dz = -R; dz <= R; dz++) {
                    double d = Math.sqrt(dx * dx + dy * (double) dy + dz * dz);
                    if (Math.abs(d - REALM) > 0.55) continue;
                    (dy >= -3 ? r.dome : r.under).add(new Cell(cx + dx, cy + dy, cz + dz));
                }
            }
        }
        r.dome.sort((a, b) -> Integer.compare(a.y(), b.y()));
        r.under.sort((a, b) -> Integer.compare(b.y(), a.y()));
        List<long[]> cols = new ArrayList<>();
        for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++) {
            double d = Math.hypot(dx, dz);
            if (d <= REALM - 0.5) cols.add(new long[]{cx + dx, cz + dz, (long) (d * 100)});
        }
        cols.sort((a, b) -> Long.compare(a[2], b[2]));
        r.columns.addAll(cols);
    }

    // ===================== TICK =====================

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Realm> list = realms.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Realm r : list) if (tickRealm(caster, r, now, store, cb)) list.remove(r);
        if (list.isEmpty()) { realms.remove(uuid); clicks.remove(uuid); }
    }

    private static boolean tickRealm(Ref<EntityStore> caster, Realm r, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        long t = now - r.startAt, dt = now - r.lastTick;
        r.lastTick = now;
        World world = SpellFx.world(store);
        if (r.domeBatch == null) open(caster, r, world, store, cb);
        if (t < FINALE_AT) {
            buildGlobe(r, t, world);
            blackenGround(r, t, world);
            floatThrone(r, t, now, store, cb);
            if (!r.seated && t >= BUILD_MS) seat(caster, r, store, cb);
            if (t >= BUILD_MS) {
                border(caster, r, now, world, store, cb);
                realmLife(caster, r, now, world, store, cb);
                gestures(caster, r, now, world, store, cb);
                scanShadows(caster, r, now, world, store, cb);
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                r.shadows.values().removeIf(s -> {
                    boolean gone = tickShadow(caster, r, s, now, dt, store, cb, rng);
                    if (gone) { shadowIndex.remove(s.ref); shadowHits.remove(s.ref); r.respawnAt.put(s.owner, now + RESPAWN_MS); }
                    return gone;
                });
            } else clicks.remove(r.owner);
            tickChains(caster, r, t, now, store, cb);
            tickSinkings(caster, r, now, world, store, cb);
            return false;
        }
        finale(r, t, now, world, store, cb);
        return t >= END_MS;
    }

    // ===================== I. LE ROYAUME ET LE TRONE =====================

    private static void open(Ref<EntityStore> caster, Realm r, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        long restoreAt = r.startAt + END_MS + 60_000;                                        // filet de securite
        r.domeBatch = TerrainRestore.open(world, restoreAt);
        r.underBatch = TerrainRestore.open(world, restoreAt);
        r.groundBatch = TerrainRestore.open(world, restoreAt);
        r.throneBatch = TerrainRestore.open(world, restoreAt);
        Vector3d s = r.support();
        TerrainRestore.Restore batch = r.throneBatch;
        world.execute(() -> {
            try { placeInAir(world, batch, (int) Math.floor(s.x), (int) Math.floor(s.y), (int) Math.floor(s.z)); }
            catch (Exception e) { EldaniorLogger.error("NyxVeil: assise", e); }
        });
        r.throne = SpellFx.spawnModel(caster, THRONE_PROJECTILE, new Vector3d(r.center).add(0, THRONE_RISE_FROM, 0), r.front, cb);
        SpellFx.fx(FX_SMOKE, new Vector3d(r.center).add(0, 1, 0), 4.0f, store, cb);
        SpellFx.fx(FX_DRAIN, new Vector3d(r.center).add(0, 1, 0), 3.0f, store, cb);
    }

    /** Le globe se referme : moitie haute (de bas en haut, dans l'air), puis moitie basse dans le sol. */
    private static void buildGlobe(Realm r, long t, World world) {
        int target = (int) Math.min(r.dome.size(), r.dome.size() * Math.min(1.0, t / (double) BUILD_MS));
        if (target > r.domePlaced) {
            List<Cell> batch = new ArrayList<>(r.dome.subList(r.domePlaced, target));
            r.domePlaced = target;
            TerrainRestore.Restore restore = r.domeBatch;
            world.execute(() -> {
                try { for (Cell c : batch) fillTar(world, restore, r, c.x(), c.y(), c.z(), true); }
                catch (Exception e) { EldaniorLogger.error("NyxVeil: globe", e); }
            });
        }
        if (t < BUILD_MS) return;
        int under = (int) Math.min(r.under.size(), r.under.size() * Math.min(1.0, (t - BUILD_MS) / (double) (UNDER_MS - BUILD_MS)));
        if (under > r.underPlaced) {
            List<Cell> batch = new ArrayList<>(r.under.subList(r.underPlaced, under));
            r.underPlaced = under;
            TerrainRestore.Restore restore = r.underBatch;
            world.execute(() -> {
                try { for (Cell c : batch) fillTar(world, restore, r, c.x(), c.y(), c.z(), true); }
                catch (Exception e) { EldaniorLogger.error("NyxVeil: globe (sous le sol)", e); }
            });
        }
    }

    /** Le sol du globe devient noir sur GROUND_DEPTH blocs, du centre vers le bord (plantes basses retirees). */
    private static void blackenGround(Realm r, long t, World world) {
        int target = (int) Math.min(r.columns.size(), r.columns.size() * Math.min(1.0, t / (double) UNDER_MS));
        if (target <= r.groundPlaced) return;
        List<long[]> batch = new ArrayList<>(r.columns.subList(r.groundPlaced, target));
        r.groundPlaced = target;
        TerrainRestore.Restore restore = r.groundBatch;
        int baseY = (int) Math.floor(r.center.y);
        world.execute(() -> {
            try {
                for (long[] c : batch) {
                    int x = (int) c[0], z = (int) c[1];
                    if (TerrainRestore.isProtectedArea(world.getName(), x, baseY, z)) continue;
                    int top = Integer.MIN_VALUE;
                    for (int y = baseY + 10; y >= baseY - 10; y--) {
                        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
                        if (type == null) break;
                        if (WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) { top = y; break; }   // eau, lave : noircie aussi
                        if (TerrainRestore.isEmpty(world, x, y, z)) continue;
                        String id = type.getId();
                        if (id.equals(TAR)) continue;                                           // paroi du globe au-dessus du sol : on cherche le vrai sol
                        if (id.contains("Leaves") || id.startsWith("Wood_")) continue;
                        if (id.startsWith("Plant_")) { TerrainRestore.place(world, restore, x, y, z, id, TerrainRestore.EMPTY, 0); continue; }
                        top = y;
                        break;
                    }
                    if (top == Integer.MIN_VALUE) continue;
                    for (int y = top; y > top - GROUND_DEPTH; y--) fillTar(world, restore, r, x, y, z, false);
                }
            } catch (Exception e) {
                EldaniorLogger.error("NyxVeil: sol noir", e);
            }
        });
    }

    /**
     * Met du goudron dans la case, quel qu'en soit le contenu : bloc plein, demi-dalle, escalier, plante, feuilles,
     * bois, eau ou lave (le fluide est note pour etre remis a la fin). fillAir : remplit aussi une case vide.
     * Seuls les contenus (coffres, etc.) et les zones protegees sont epargnes.
     */
    private static void fillTar(World world, TerrainRestore.Restore batch, Realm r, int x, int y, int z, boolean fillAir) {
        if (TerrainRestore.isProtectedArea(world.getName(), x, y, z)) return;
        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
        if (type == null) return;
        int fluid = WorldBlockUtil.getFluidIndex(world, x, y, z);
        if (fluid > 0) {
            r.fluids.add(new int[]{x, y, z, fluid});
            WorldBlockUtil.setFluid(world, x, y, z, null);
        }
        boolean empty = TerrainRestore.isEmpty(world, x, y, z);
        if (empty) {
            if (fillAir || fluid > 0) TerrainRestore.place(world, batch, x, y, z, TerrainRestore.EMPTY, TAR, 0);
            return;
        }
        String id = type.getId();
        if (id.equals(TAR) || TerrainRestore.isKept(world, id, x, y, z)) return;
        TerrainRestore.place(world, batch, x, y, z, id, TAR, 0);
    }

    /** Remet l'eau et la lave retirees par le royaume (apres la restauration des blocs). */
    private static void restoreFluids(World world, Realm r) {
        for (int[] f : r.fluids) {
            try {
                var fluid = com.hypixel.hytale.server.core.asset.type.fluid.Fluid.getAssetMap().getAsset(f[3]);
                if (fluid != null && TerrainRestore.isEmpty(world, f[0], f[1], f[2])) WorldBlockUtil.setFluid(world, f[0], f[1], f[2], fluid.getId());
            } catch (Exception e) {
                EldaniorLogger.error("NyxVeil: retour des fluides", e);
            }
        }
        r.fluids.clear();
    }

    private static void floatThrone(Realm r, long t, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        double f = Math.min(1.0, t / (double) BUILD_MS), ease = 1 - Math.pow(1 - f, 3);
        double hover = THRONE_HOVER + 1 - SEAT_OFFSET;
        double y = THRONE_RISE_FROM + (hover - THRONE_RISE_FROM) * ease + (f >= 1 ? Math.sin(now / 900.0) * 0.06 : 0);
        SpellFx.hold(r.throne, new Vector3d(r.center).add(0, y, 0), SpellFx.facing(r.front, false), store);
        if (now - r.lastLevitation >= LEVITATION_FX_EVERY_MS) {
            r.lastLevitation = now;
            SpellFx.fx(FX_DRAIN, new Vector3d(r.center).add(0, y - 0.4, 0), 0.9f, store, cb);
        }
    }

    private static void seat(Ref<EntityStore> caster, Realm r, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        r.seated = true;
        SpellFx.teleport(caster, r.seat(), SpellFx.facing(r.front, false), store, cb);
        SpellFx.effect(caster, CROWN_EFFECT, cb);                                            // pose une fois (pas de clignotement)
        SpellFx.effect(caster, SOVEREIGN_EFFECT, cb);
        SpellFx.fx(FX_SMOKE, new Vector3d(r.seat()).add(0, 1, 0), 2.5f, store, cb);
        SpellFx.fx(FX_DRAIN, new Vector3d(r.seat()).add(0, 1.5, 0), 3.0f, store, cb);
        SpellFx.sound(SFX_CAST, r.seat(), cb);
        SpellFx.shake(r.center, REALM * 2, "Impact_Strong", store);
    }

    /** Frontiere du globe : on y entre, on n'en sort pas (le souverain et ses allies passent). */
    private static void border(Ref<EntityStore> caster, Realm r, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now - r.lastBorder < BORDER_EVERY_MS) return;
        r.lastBorder = now;
        double reach = REALM + PULL_IN_BAND + 1;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, r.center, reach, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || p.y < r.center.y - 3) continue;
            double d = new Vector3d(p).add(0, 1, 0).distance(r.center);
            if (d < REALM - 1) { if (d > REALM - INSIDE_MARGIN) pushBack(caster, r, e, p, cb); continue; }
            if (d > REALM && d < REALM + PULL_IN_BAND) pullIn(r, e, p, world, store, cb);
        }
        for (Ref<EntityStore> a : SpellFx.alliesAround(caster, r.center, reach, store)) {
            Vector3d p = SpellFx.position(a, store);
            if (p == null || p.y < r.center.y - 3) continue;
            double d = new Vector3d(p).add(0, 1, 0).distance(r.center);
            if (d > REALM && d < REALM + PULL_IN_BAND) pullIn(r, a, p, world, store, cb);
            else if (d < REALM && d > REALM - 1.8) passOut(r, a, p, world, store, cb);
        }
        Vector3d cp = SpellFx.position(caster, store);
        if (cp != null) {
            double d = new Vector3d(cp).add(0, 1, 0).distance(r.center);
            if (d > REALM && d < REALM + PULL_IN_BAND) pullIn(r, caster, cp, world, store, cb);
            else if (d < REALM && d > REALM - 1.8) passOut(r, caster, cp, world, store, cb);
        }
    }

    private static void pushBack(Ref<EntityStore> caster, Realm r, Ref<EntityStore> e, Vector3d p, CommandBuffer<EntityStore> cb) {
        Vector3d in = new Vector3d(r.center.x - p.x, 0, r.center.z - p.z);
        if (in.lengthSquared() < 1e-4) return;
        SpellFx.damage(caster, e, "Physical", 0.01f, in.normalize().mul(8).add(0, 2, 0), null, cb);
    }

    private static void pullIn(Realm r, Ref<EntityStore> e, Vector3d p, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d dir = new Vector3d(p.x - r.center.x, 0, p.z - r.center.z);
        if (dir.lengthSquared() < 1e-4) return;
        dir.normalize();
        Vector3d to = SpellFx.ground(world, new Vector3d(r.center).add(new Vector3d(dir).mul(REALM - 3.5)));
        SpellFx.fx(FX_SMOKE, new Vector3d(p).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.teleport(e, to, store, cb);
        SpellFx.fx(FX_SMOKE, new Vector3d(to).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_PASS, to, cb);
    }

    private static void passOut(Realm r, Ref<EntityStore> a, Vector3d p, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d dir = new Vector3d(p.x - r.center.x, 0, p.z - r.center.z);
        if (dir.lengthSquared() < 1e-4) return;
        dir.normalize();
        Vector3d to = SpellFx.ground(world, new Vector3d(r.center).add(new Vector3d(dir).mul(REALM + PULL_IN_BAND + 1)));
        SpellFx.teleport(a, to, store, cb);
        SpellFx.fx(FX_SMOKE, new Vector3d(to).add(0, 1, 0), 1.2f, store, cb);
    }

    /** Tapis de brume noire (1 bloc) sur tout le sol, ennemis marques, souverain amplifie et couronne. */
    private static void realmLife(Ref<EntityStore> caster, Realm r, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now - r.lastMist >= MIST_EVERY_MS) {
            r.lastMist = now;
            int k = (int) Math.ceil(REALM / MIST_SPACING);
            for (int i = -k; i <= k; i++) for (int j = -k; j <= k; j++) {
                if (Math.hypot(i * MIST_SPACING, j * MIST_SPACING) > REALM - 1.5) continue;
                SpellFx.fx(FX_MIST, SpellFx.ground(world, new Vector3d(r.center).add(i * MIST_SPACING, 0, j * MIST_SPACING)), 1.0f, store, cb);
            }
        }
        if (now - r.lastMark >= MARK_EVERY_MS) {
            r.lastMark = now;
            for (Ref<EntityStore> e : enemiesInRealm(caster, r, store)) SpellFx.effect(e, MARK_EFFECT, cb);
        }
    }

    /** Ennemis du royaume (les ombres-monstres n'en font pas partie). */
    private static List<Ref<EntityStore>> enemiesInRealm(Ref<EntityStore> caster, Realm r, Store<EntityStore> store) {
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, r.center, REALM, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p != null && p.y >= r.center.y - 8 && alive(e, store)) out.add(e);
        }
        return out;
    }

    // ===================== II. LES OMBRES =====================

    /** Chaque adversaire du royaume voit son ombre se dresser (monstre : vrai monstre noirci ; joueur : sa copie). */
    private static void scanShadows(Ref<EntityStore> caster, Realm r, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now - r.lastScan < SCAN_EVERY_MS) return;
        r.lastScan = now;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (Ref<EntityStore> e : enemiesInRealm(caster, r, store)) {
            if (r.shadows.containsKey(e) || r.shadows.size() >= MAX_SHADOWS || now < r.respawnAt.getOrDefault(e, 0L)) continue;
            if (r.chains.stream().anyMatch(c -> c.target.equals(e)) || r.sinkings.stream().anyMatch(k -> k.target.equals(e))) continue;   // il est deja tenu par le souverain
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            double a = rng.nextDouble(Math.PI * 2);
            Vector3d at = new Vector3d(p).add(Math.cos(a) * 2.5, 0, Math.sin(a) * 2.5);
            Ref<EntityStore> ref = SpellFx.spawnModel(caster, SHADOW_PROJECTILE, new Vector3d(at).sub(0, 2.2, 0), new Vector3d(1, 0, 0), cb);
            if (ref == null) continue;
            Shadow s = new Shadow(ref, e, at, maxHealth(e, store), now, rng);
            r.shadows.put(e, s);
            shadowIndex.put(ref, s);
            SpellFx.fx(FX_SMOKE, new Vector3d(at).add(0, 0.5, 0), 1.6f, store, cb);
            SpellFx.fx(FX_DRAIN, new Vector3d(at).add(0, 0.3, 0), 1.4f, store, cb);
            SpellFx.sound(SFX_RISE, at, cb);
        }
    }

    /** Le monstre prend l'ombre pour cible (si son IA l'accepte). */
    private static void lock(Ref<EntityStore> npcRef, Ref<EntityStore> target, Store<EntityStore> store) {
        try {
            if (store.getComponent(npcRef, NPCEntity.getComponentType()) == null) return;
            MarkedEntitySupport marks = MarkedEntitySupport.get(npcRef, store);
            if (marks != null) marks.setMarkedEntity(MarkedEntitySupport.DEFAULT_TARGET_SLOT, target);
        } catch (Exception ignored) {}
    }

    private static void dress(Shadow s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        s.dressed = true;
        ModelComponent mc = store.getComponent(s.owner, ModelComponent.getComponentType());
        PlayerSkinComponent skin = store.getComponent(s.owner, PlayerSkinComponent.getComponentType());
        if (mc != null && mc.getModel() != null) cb.putComponent(s.ref, ModelComponent.getComponentType(), new ModelComponent(mc.getModel()));
        if (skin != null && skin.getPlayerSkin() != null) cb.putComponent(s.ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin.getPlayerSkin()));
        cb.putComponent(s.ref, MovementStatesComponent.getComponentType(), new MovementStatesComponent());
        SpellFx.effect(s.ref, CLONE_EFFECT, cb);
    }

    /** Ombre : court sur sa cible, tourne autour, se fend, frappe et recule ; un monstre au contact la frappe, un joueur au clic. */
    private static boolean tickShadow(Ref<EntityStore> caster, Realm r, Shadow s, long now, long dt, Store<EntityStore> store, CommandBuffer<EntityStore> cb, ThreadLocalRandom rng) {
        if (!s.ref.isValid()) return now - s.bornAt > 1_000;
        if (!s.dressed) { dress(s, store, cb); return false; }
        if (s.target == null || !s.target.isValid() || !alive(s.target, store) || SpellFx.position(s.target, store) == null) {
            s.target = nearestEnemy(caster, r, s.pos, store);
            if (s.target == null) { dissolve(s, store, cb); return true; }
        }
        Vector3d tp = SpellFx.position(s.target, store);
        if (now - s.lastLock >= LOCK_EVERY_MS) { s.lastLock = now; lock(s.target, s.ref, store); }
        double reach = Math.hypot(tp.x - s.pos.x, tp.z - s.pos.z);
        boolean bound = r.chains.stream().anyMatch(c -> c.target.equals(s.target)) || r.sinkings.stream().anyMatch(k -> k.target.equals(s.target));
        if (!bound && store.getComponent(s.target, NPCEntity.getComponentType()) != null && reach <= MOB_REACH && now - s.lastMobHit >= MOB_HIT_EVERY_MS) {   // enchaine : il ne frappe plus
            s.lastMobHit = now;
            hurt(s, s.power / SHADOW_STRENGTH, now, store, cb);                              // le monstre frappe un peu plus fort
        }
        Integer hits = shadowHits.remove(s.ref);
        if (hits != null) hurt(s, s.power / SHADOW_STRENGTH * hits, now, store, cb);
        if (s.hp <= 0) {
            SpellFx.fx(FX_DRAIN, new Vector3d(s.pos).add(0, 1, 0), 2.0f, store, cb);
            dissolve(s, store, cb);
            return true;
        }
        double dist = Math.hypot(tp.x - s.pos.x, tp.z - s.pos.z), sec = dt / 1000.0;
        Vector3d dirTo = dist > 1e-3 ? new Vector3d(tp.x - s.pos.x, 0, tp.z - s.pos.z).div(dist) : new Vector3d(1, 0, 0);
        Vector3d goal;
        double speed;
        if (now < s.flinchUntil) {
            goal = new Vector3d(s.pos).sub(new Vector3d(dirTo).mul(1.5));
            speed = RETREAT_SPEED;
        } else if (s.lungeAt >= 0) {
            goal = new Vector3d(tp).sub(new Vector3d(dirTo).mul(LUNGE_REACH));
            speed = LUNGE_SPEED;
            if (!s.struck && now - s.lungeAt >= LUNGE_MS) {
                s.struck = true;
                SpellFx.damage(caster, s.target, "Physical", s.power, new Vector3d(dirTo).mul(3).add(0, 1.5, 0), "Impact_Light", cb);
                SpellFx.fx(FX_DRAIN, new Vector3d(tp).add(0, 1, 0), 1.2f, store, cb);
                SpellFx.sound(SFX_STRIKE, tp, cb);
                SpellFx.restore(caster, DefaultEntityStatTypes.getMana(), MANA_STEAL, cb);
                s.retreatUntil = now + RETREAT_MS;
                s.lungeAt = -1;
                if (rng.nextInt(3) == 0) s.dir = -s.dir;
            }
        } else if (now < s.retreatUntil) {
            goal = new Vector3d(tp).sub(new Vector3d(dirTo).mul(RETREAT_TO));
            speed = RETREAT_SPEED;
        } else if (dist > ENGAGE) {
            goal = new Vector3d(tp).sub(new Vector3d(dirTo).mul(CIRCLE));
            speed = RUN_SPEED;
        } else {
            s.circle += sec * CIRCLE_SPEED * s.dir;
            goal = new Vector3d(tp).add(Math.cos(s.circle) * CIRCLE, 0, Math.sin(s.circle) * CIRCLE);
            speed = WALK_SPEED;
            if (now >= s.nextStrike) {
                s.lungeAt = now;
                s.struck = false;
                s.nextStrike = now + rng.nextLong(STRIKE_MIN_MS, STRIKE_MAX_MS);
            }
        }
        Vector3d step = new Vector3d(goal.x - s.pos.x, 0, goal.z - s.pos.z);
        double len = step.length(), move = Math.min(len, speed * sec);
        if (len > 1e-3) s.pos.add(step.div(len).mul(move));
        s.pos.y = tp.y;
        double depth = Math.max(0, 1 - (now - s.bornAt) / (double) RISE_MS) * 2.2;
        Vector3d face = new Vector3d(tp.x - s.pos.x, 0, tp.z - s.pos.z);
        Rotation3f rot = face.lengthSquared() > 1e-4 ? SpellFx.facing(face.normalize(), false) : null;
        SpellFx.hold(s.ref, new Vector3d(s.pos).sub(0, depth, 0), rot, store);
        animate(s, move / Math.max(1e-3, sec), store);
        return false;
    }

    private static float maxHealth(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            EntityStatMap map = store.getComponent(ref, EntityStatMap.getComponentType());
            var v = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
            return v != null && v.getMax() > 0 ? v.getMax() : 100f;
        } catch (Exception e) {
            return 100f;
        }
    }

    private static void hurt(Shadow s, float amount, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        s.hp -= amount;
        s.flinchUntil = now + FLINCH_MS;
        s.lungeAt = -1;
        SpellFx.fx(FX_SMOKE, new Vector3d(s.pos).add(0, 1.2, 0), 0.8f, store, cb);
        SpellFx.sound(SFX_STRIKE, s.pos, cb);
    }

    private static void animate(Shadow s, double speed, Store<EntityStore> store) {
        MovementStatesComponent ms = store.getComponent(s.ref, MovementStatesComponent.getComponentType());
        if (ms == null) return;
        MovementStates st = new MovementStates();
        st.onGround = true;
        st.running = speed > 5.5;
        st.walking = speed > 0.5 && !st.running;
        st.idle = speed <= 0.5;
        st.horizontalIdle = st.idle;
        if (!st.equals(s.states)) { s.states = st; ms.setMovementStates(new MovementStates(st)); }
    }

    private static Ref<EntityStore> nearestEnemy(Ref<EntityStore> caster, Realm r, Vector3d from, Store<EntityStore> store) {
        if (from == null) return null;
        Ref<EntityStore> best = null;
        double bestD = Double.MAX_VALUE;
        for (Ref<EntityStore> e : enemiesInRealm(caster, r, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            double d = p.distance(from);
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    private static void dissolve(Shadow s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_SMOKE, new Vector3d(s.pos).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.remove(s.ref, cb);
    }

    // ===================== III. LES GESTES DU SOUVERAIN =====================

    private static void gestures(Ref<EntityStore> caster, Realm r, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Click> pending = new ArrayList<>();
        ConcurrentLinkedQueue<Click> queue = clicks.get(r.owner);
        if (queue != null) for (Click c; (c = queue.poll()) != null; ) pending.add(c);
        Set<Integer> primary = SpellFx.chains(caster, store, InteractionType.Primary), secondary = SpellFx.chains(caster, store, InteractionType.Secondary);
        boolean chainFallback = false, voidFallback = false;
        for (Integer id : primary) if (!r.seenPrimary.contains(id)) chainFallback = true;
        for (Integer id : secondary) if (!r.seenSecondary.contains(id)) voidFallback = true;
        r.seenPrimary = primary;
        r.seenSecondary = secondary;
        if (pending.isEmpty()) {
            if (chainFallback) pending.add(new Click(MouseButtonType.Left, null));
            if (voidFallback) pending.add(new Click(MouseButtonType.Right, null));
        }
        if (pending.isEmpty() || !inOwnRealm(caster)) return;
        List<Ref<EntityStore>> enemies = enemiesInRealm(caster, r, store);
        for (Click c : pending) {
            Ref<EntityStore> target = c.target() != null && enemies.contains(c.target()) ? c.target() : aimed(caster, enemies, store);
            if (target == null) continue;
            if (c.button() == MouseButtonType.Left) chain(r, target, now, world, store, cb);
            else banish(r, target, now, world, store, cb);
        }
    }

    /** Chaines de l'enfer : quatre chaines violettes jaillissent du sol autour de l'ennemi, comme des grappins. */
    private static void chain(Realm r, Ref<EntityStore> target, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now - r.lastChain < CHAIN_COOLDOWN_MS || r.chains.stream().anyMatch(c -> c.target.equals(target))) return;
        Vector3d p = SpellFx.position(target, store);
        int beam = Beam.getAssetMap().getIndex(CHAIN_BEAM);
        if (p == null || beam < 0) return;
        r.lastChain = now;
        // Taille de la creature : accroche plus haute et ancrages plus ecartes pour les gros monstres (yeti...)
        double height = 1.8, width = 0.6;
        com.hypixel.hytale.server.core.modules.entity.component.BoundingBox box = store.getComponent(target, com.hypixel.hytale.server.core.modules.entity.component.BoundingBox.getComponentType());
        if (box != null && box.getBoundingBox() != null) { height = Math.max(0.8, box.getBoundingBox().height()); width = Math.max(0.4, box.getBoundingBox().getMaximumThickness()); }
        double attach = Math.max(0.9, height * CHAIN_ATTACH_RATIO), spread = CHAIN_ANCHOR + width * CHAIN_ANCHOR_PER_WIDTH;
        List<Vector3d> anchors = new ArrayList<>();
        double off = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        for (int i = 0; i < CHAINS; i++) {
            double a = off + Math.PI * 2 * i / CHAINS;
            anchors.add(SpellFx.ground(world, new Vector3d(p).add(Math.cos(a) * spread, 0, Math.sin(a) * spread)).add(0, 0.05, 0));
        }
        Chained c = new Chained(target, anchors, now, attach, height, width);
        long life = r.startAt + FINALE_AT - now + 2_000;
        for (Vector3d a : anchors) {                                                       // le sol se fend la ou chaque chaine jaillit
            Ref<EntityStore> ref = spawnBeam(a, AttachedBeam.toPosition(beam, CHAIN_SCALE, null, new Vector3d(a).add(0, 0.05, 0)), life, store, cb);
            if (ref != null) c.beams.add(ref);
            SpellFx.fx(FX_CHAIN_BURST, a, 1.0f, store, cb);
        }
        r.chains.add(c);
        SpellFx.sound(SFX_CHAIN_SHOT, p, cb);
    }

    /** Entite porteuse d'un faisceau (comme BeamComponent.spawn, mais via le CommandBuffer). */
    private static Ref<EntityStore> spawnBeam(Vector3d at, AttachedBeam beam, long lifeMs, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        try {
            Holder<EntityStore> h = store.getRegistry().newHolder();
            h.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
            h.addComponent(TransformComponent.getComponentType(), new TransformComponent(new Vector3d(at), new Rotation3f()));
            h.addComponent(BeamComponent.getComponentType(), new BeamComponent(beam));
            h.ensureComponent(UUIDComponent.getComponentType());
            h.ensureComponent(store.getRegistry().getNonSerializedComponentType());
            h.addComponent(DespawnComponent.getComponentType(), DespawnComponent.despawnInSeconds(store.getResource(TimeResource.getResourceType()), lifeMs / 1000f));
            return cb.addEntity(h, AddReason.SPAWN);
        } catch (Exception e) {
            EldaniorLogger.error("NyxVeil: chaine", e);
            return null;
        }
    }

    /**
     * Les chaines s'allongent du sol vers l'ennemi, puis s'accrochent a lui : il est enchaine (statut, maillons autour
     * du corps) et immobilise jusqu'a la fin du sort.
     */
    private static void tickChains(Ref<EntityStore> caster, Realm r, long t, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        boolean hit = now - r.lastChainTick >= CHAIN_TICK_MS;
        if (hit) r.lastChainTick = now;
        int beam = Beam.getAssetMap().getIndex(CHAIN_BEAM);
        r.chains.removeIf(c -> {
            Vector3d p = c.target.isValid() && alive(c.target, store) ? SpellFx.position(c.target, store) : null;
            if (p == null) { releaseChains(c, null, cb); return true; }                    // morte : les chaines tombent
            Vector3d body = new Vector3d(p).add(0, c.attach, 0);
            if (!c.attached) {                                                               // coup de fouet, une chaine apres l'autre
                boolean done = true;
                for (int i = 0; i < c.beams.size(); i++) {
                    double f = Math.min(1.0, Math.max(0, now - c.startAt - i * CHAIN_STAGGER_MS) / (double) CHAIN_SHOOT_MS);
                    if (f < 1) done = false;
                    double ease = 1 - Math.pow(1 - f, 3) + 0.08 * Math.sin(Math.PI * f);       // depasse un peu, puis se tend
                    aimOne(c, i, beam, new Vector3d(c.anchors.get(i)).lerp(body, Math.min(1.05, ease)), store);
                }
                if (done) {
                    c.attached = true;
                    SpellFx.lockInPlace(c.target, r.startAt + FINALE_AT - now, store);
                    SpellFx.effect(c.target, CHAINED_EFFECT, cb);
                    wrap(c, beam, p, r.startAt + FINALE_AT - now + 2_000, store, cb);
                    SpellFx.fx(FX_CHAIN_BURST, new Vector3d(p).add(0, 0.05, 0), 1.3f, store, cb);   // (l'impact de la Chain Hook du jeu boucle sans fin : non utilise)
                    SpellFx.fx(FX_DRAIN, body, 1.5f, store, cb);
                    SpellFx.sound(SFX_CHAIN_HIT, p, cb);
                    SpellFx.shake(p, 12, "Impact_Light", store);
                }
                return false;
            }
            // Tension : les chaines se retendent par a-coups, la cible est tiree vers le sol
            double yank = 0;
            if (now >= c.pulseAt) {
                double f = (now - c.pulseAt) / (double) CHAIN_PULSE_MS;
                if (f >= 1) c.pulseAt = now + CHAIN_PULSE_EVERY_MS + ThreadLocalRandom.current().nextLong(600);
                else {
                    yank = Math.sin(Math.PI * f) * CHAIN_YANK;
                    if (f < 0.2 && now - c.lastRefresh >= CHAIN_REFRESH_MS) {
                        SpellFx.sound(SFX_CHAIN_CLANK, p, cb);
                        for (Vector3d a : c.anchors) SpellFx.fx(FX_CHAIN_BURST, a, 0.55f, store, cb);
                    }
                }
            }
            if (now - c.lastRefresh >= CHAIN_REFRESH_MS) {
                c.lastRefresh = now;
                Vector3d pulled = new Vector3d(body).sub(0, yank, 0);
                for (int i = 0; i < c.beams.size(); i++) aimOne(c, i, beam, pulled, store);
                refreshWrap(c, beam, p, store);
            }
            if (now - c.lastAnchorFx >= ANCHOR_FX_EVERY_MS) {                              // cercles runiques aux ancrages
                c.lastAnchorFx = now;
                for (Vector3d a : c.anchors) SpellFx.fx(FX_CHAIN_ANCHOR, a, 1.0f, store, cb);
            }
            if (hit) {
                SpellFx.effect(c.target, CHAINED_EFFECT, cb);
                drainMana(caster, c.target, body, store, cb);
            }
            return false;
        });
    }

    /** Vivante : ni marquee morte par le moteur (animation de mort comprise), ni a 0 point de vie. */
    private static boolean alive(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            if (store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent.getComponentType()) != null) return false;
            EntityStatMap map = store.getComponent(ref, EntityStatMap.getComponentType());
            var hp = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
            return hp == null || hp.get() > 0;
        } catch (Exception e) {
            return true;
        }
    }

    /** Les chaines aspirent le mana de la victime et le rendent au souverain (aucun degat). */
    private static void drainMana(Ref<EntityStore> caster, Ref<EntityStore> target, Vector3d body, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        try {
            EntityStatMap from = store.getComponent(target, EntityStatMap.getComponentType());
            var mana = from != null ? from.get(DefaultEntityStatTypes.getMana()) : null;
            if (mana == null || mana.get() <= 0) return;
            float amount = Math.min(mana.get(), mana.getMax() * CHAIN_MANA_DRAIN);
            from.addStatValue(DefaultEntityStatTypes.getMana(), -amount);
            EntityStatMap to = store.getComponent(caster, EntityStatMap.getComponentType());
            if (to != null) to.addStatValue(DefaultEntityStatTypes.getMana(), amount);
            SpellFx.fx(FX_DRAIN, body, 0.7f, store, cb);
        } catch (Exception e) {
            EldaniorLogger.error("NyxVeil: aspiration du mana", e);
        }
    }

    /** Point de la ceinture `ring`, segment k, autour du corps (rayon selon la carrure). */
    private static Vector3d wrapPoint(Chained c, Vector3d feet, int ring, int k) {
        double a = Math.PI * 2 * k / WRAP_SEGMENTS + ring * 0.5, rad = c.width / 2 + WRAP_MARGIN;
        return new Vector3d(feet).add(Math.cos(a) * rad, c.height * WRAP_HEIGHTS[ring] + (k % 2 == 0 ? 0.04 : -0.04), Math.sin(a) * rad);
    }

    /** Enroule deux ceintures de chaine autour du corps (chaque segment est un faisceau de chaine). */
    private static void wrap(Chained c, int beam, Vector3d feet, long lifeMs, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (int ring = 0; ring < WRAP_HEIGHTS.length; ring++) {
            for (int k = 0; k < WRAP_SEGMENTS; k++) {
                Vector3d from = wrapPoint(c, feet, ring, k), to = wrapPoint(c, feet, ring, k + 1);
                Ref<EntityStore> ref = spawnBeam(from, AttachedBeam.toPosition(beam, WRAP_SCALE, null, to), lifeMs, store, cb);
                if (ref != null) c.wraps.add(ref);
            }
        }
    }

    /** Les ceintures suivent le corps. */
    private static void refreshWrap(Chained c, int beam, Vector3d feet, Store<EntityStore> store) {
        for (int i = 0; i < c.wraps.size(); i++) {
            Ref<EntityStore> ref = c.wraps.get(i);
            if (!ref.isValid()) continue;
            int ring = i / WRAP_SEGMENTS, k = i % WRAP_SEGMENTS;
            SpellFx.hold(ref, wrapPoint(c, feet, ring, k), null, store);
            BeamComponent bc = store.getComponent(ref, BeamComponent.getComponentType());
            if (bc != null) bc.set(List.of(AttachedBeam.toPosition(beam, WRAP_SCALE, null, wrapPoint(c, feet, ring, k + 1))));
        }
    }

    /** Pointe la chaine i de son ancrage vers `to`. */
    private static void aimOne(Chained c, int i, int beam, Vector3d to, Store<EntityStore> store) {
        BeamComponent bc = c.beams.get(i).isValid() ? store.getComponent(c.beams.get(i), BeamComponent.getComponentType()) : null;
        if (bc != null) bc.set(List.of(AttachedBeam.toPosition(beam, CHAIN_SCALE, null, new Vector3d(to))));
    }

    private static void releaseChains(Chained c, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.unlock(c.target);
        for (Ref<EntityStore> b : c.beams) SpellFx.remove(b, cb);
        for (Ref<EntityStore> w : c.wraps) SpellFx.remove(w, cb);
    }

    private static void banish(Realm r, Ref<EntityStore> target, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now - r.lastVoid < VOID_COOLDOWN_MS || r.sinkings.stream().anyMatch(s -> s.target.equals(target))) return;
        Vector3d p = SpellFx.position(target, store);
        if (p == null) return;
        r.lastVoid = now;
        r.chains.removeIf(c -> {
            if (!c.target.equals(target)) return false;
            releaseChains(c, store, cb);
            return true;
        });
        Sinking s = new Sinking(target, SpellFx.ground(world, p), now);
        r.sinkings.add(s);
        TerrainRestore.Restore batch = r.groundBatch;
        world.execute(() -> {
            try { openPit(world, batch, s); }
            catch (Exception e) { EldaniorLogger.error("NyxVeil: retour au neant", e); }
        });
        SpellFx.effect(target, BOUND_EFFECT, cb);
        SpellFx.fx(FX_VOID, new Vector3d(s.at).add(0, 0.5, 0), 1.5f, store, cb);
        SpellFx.sound(SFX_VOID, s.at, cb);
    }

    private static Ref<EntityStore> aimed(Ref<EntityStore> caster, List<Ref<EntityStore>> enemies, Store<EntityStore> store) {
        Vector3d eye = SpellFx.position(caster, store);
        if (eye == null) return null;
        eye.add(0, 1.6, 0);
        Vector3d look = SpellFx.lookDirection(caster, store);
        Ref<EntityStore> best = null;
        double bestAlign = AIM_ALIGN;
        for (Ref<EntityStore> e : enemies) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d to = new Vector3d(p).add(0, 1, 0).sub(eye);
            double d = to.length();
            if (d < 0.5 || d > AIM_RANGE) continue;
            double align = to.div(d).dot(look);
            if (align > bestAlign) { bestAlign = align; best = e; }
        }
        return best;
    }

    private static void tickSinkings(Ref<EntityStore> caster, Realm r, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        r.sinkings.removeIf(s -> {
            boolean alive = s.target.isValid() && SpellFx.position(s.target, store) != null;
            if (!alive || now - s.startAt >= VOID_MS) {
                if (alive) {
                    SpellFx.teleport(s.target, new Vector3d(s.at).add(0, 0.5, 0), store, cb);
                    SpellFx.damage(caster, s.target, "Physical", 0.01f, new Vector3d(0, 6, 0), null, cb);
                }
                closePit(world, r, s);
                SpellFx.fx(FX_SMOKE, new Vector3d(s.at).add(0, 0.5, 0), 1.5f, store, cb);
                return true;
            }
            if (now - s.lastTick >= VOID_TICK_MS) {
                s.lastTick = now;
                SpellFx.damage(caster, s.target, "Physical", VOID_DAMAGE, new Vector3d(0, -VOID_SINK_SPEED, 0), null, cb);
                SpellFx.effect(s.target, BOUND_EFFECT, cb);
                SpellFx.fx(FX_VOID, new Vector3d(s.at).add(0, 0.3, 0), 1.5f, store, cb);
            }
            return false;
        });
    }

    private static void openPit(World world, TerrainRestore.Restore batch, Sinking s) {
        int px = (int) Math.floor(s.at.x), py = (int) Math.floor(s.at.y), pz = (int) Math.floor(s.at.z);
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            int x = px + dx, z = pz + dz;
            if (TerrainRestore.isProtectedArea(world.getName(), x, py, z)) continue;
            for (int y = py; y >= py - VOID_DEPTH; y--) {
                BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
                if (type == null || TerrainRestore.isEmpty(world, x, y, z)) continue;
                String id = type.getId();
                if (TerrainRestore.isKept(world, id, x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) break;
                if (TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0)) s.carved.put(new Cell(x, y, z), id);
            }
        }
    }

    private static void closePit(World world, Realm r, Sinking s) {
        Map<Cell, String> carved = new HashMap<>(s.carved);
        TerrainRestore.Restore batch = r.groundBatch;
        world.execute(() -> {
            try {
                for (Map.Entry<Cell, String> e : carved.entrySet()) {
                    Cell c = e.getKey();
                    if (TerrainRestore.isEmpty(world, c.x(), c.y(), c.z())) TerrainRestore.place(world, batch, c.x(), c.y(), c.z(), TerrainRestore.EMPTY, e.getValue(), 0);
                }
            } catch (Exception ex) {
                EldaniorLogger.error("NyxVeil: fermeture du gouffre", ex);
            }
        });
    }

    // ===================== IV. LA FIN =====================

    private static void finale(Realm r, long t, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (!r.finale) {
            r.finale = true;
            for (Shadow s : r.shadows.values()) { dissolve(s, store, cb); shadowIndex.remove(s.ref); shadowHits.remove(s.ref); }
            r.shadows.clear();
            for (Chained c : r.chains) releaseChains(c, store, cb);
            r.chains.clear();
            for (Sinking s : r.sinkings) {
                if (s.target.isValid()) SpellFx.teleport(s.target, new Vector3d(s.at).add(0, 0.5, 0), store, cb);
                closePit(world, r, s);
            }
            r.sinkings.clear();
            clicks.remove(r.owner);
            if (r.caster.isValid()) {
                com.eldanior.system.config.Effects.EffectsManager.removeEffect(r.caster, CROWN_EFFECT, cb);
                com.eldanior.system.config.Effects.EffectsManager.removeEffect(r.caster, SOVEREIGN_EFFECT, cb);
            }
            TerrainRestore.Restore throne = r.throneBatch;
            world.execute(() -> { try { TerrainRestore.restoreNow(world, throne); } catch (Exception e) { EldaniorLogger.error("NyxVeil: assise", e); } });
            SpellFx.fx(FX_SMOKE, new Vector3d(r.seat()).add(0, 1, 0), 4.0f, store, cb);
            SpellFx.sound(SFX_CAST, r.center, cb);
        }
        double f = Math.min(1.0, (t - FINALE_AT) / (double) (END_MS - FINALE_AT));
        double hover = THRONE_HOVER + 1 - SEAT_OFFSET;
        SpellFx.hold(r.throne, new Vector3d(r.center).add(0, hover + (THRONE_RISE_FROM - hover) * f * f, 0), SpellFx.facing(r.front, false), store);
        int target = (int) Math.min(r.domePlaced, r.domePlaced * f);
        if (target > r.domeRemoved) {
            List<Cell> batch = new ArrayList<>(r.dome.subList(r.domePlaced - target, r.domePlaced - r.domeRemoved));
            r.domeRemoved = target;
            world.execute(() -> {
                try {
                    for (Cell c : batch) {
                        BlockType type = WorldBlockUtil.getBlockType(world, c.x(), c.y(), c.z());
                        if (type != null && TAR.equals(type.getId())) WorldBlockUtil.setBlock(world, c.x(), c.y(), c.z(), TerrainRestore.EMPTY);
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("NyxVeil: dissolution du globe", e);
                }
            });
        }
        if (t >= END_MS && !r.groundRestored) {
            r.groundRestored = true;
            SpellFx.remove(r.throne, cb);
            TerrainRestore.Restore dome = r.domeBatch, under = r.underBatch, ground = r.groundBatch;
            world.execute(() -> {
                try { TerrainRestore.restoreNow(world, dome); TerrainRestore.restoreNow(world, ground); TerrainRestore.restoreNow(world, under); restoreFluids(world, r); }
                catch (Exception e) { EldaniorLogger.error("NyxVeil: retour de la terre", e); }
            });
        }
    }

    // ===================== OUTILS =====================

    private static void placeInAir(World world, TerrainRestore.Restore batch, int x, int y, int z) {
        if (TerrainRestore.isProtectedArea(world.getName(), x, y, z)) return;
        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
        if (type == null) return;
        boolean empty = TerrainRestore.isEmpty(world, x, y, z);
        if (!empty && !type.getId().startsWith("Plant_")) return;
        if (WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) return;
        TerrainRestore.place(world, batch, x, y, z, empty ? TerrainRestore.EMPTY : type.getId(), TAR, 0);
    }

    public static void handleDisconnect(UUID uuid) {
        clicks.remove(uuid);
        List<Realm> list = realms.remove(uuid);
        if (list == null) return;
        for (Realm r : list) {
            for (Shadow s : r.shadows.values()) { shadowIndex.remove(s.ref); shadowHits.remove(s.ref); }
        }
        TerrainRestore.save();
    }
}
