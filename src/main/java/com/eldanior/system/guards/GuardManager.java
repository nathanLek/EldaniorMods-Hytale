package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.util.NpcFacing;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PNJ de poste (gardes, mage royal, patrouilleur, chevalier errant, villageois, aventurier) : postes persistants, apparition
 * (skin de joueur aleatoire mais stable par poste, armure), reapparition apres la mort, deplacements (PostMovement),
 * contrats des aventuriers recrutes (AdventurerSystem) et joueurs recherches pour PvP.
 *
 * Les gardes ont un niveau fixe (entrees GARDE_* de NpcData : PV virtuels, degats) ; un garde tue reapparait a son poste
 * apres {@link #RESPAWN_MS}.
 *
 * Le patrouilleur (ronde : PostMovement) sert de sentinelle : un monstre a moins de {@link #PATROL_SIGHT} blocs de lui, ou
 * du PvP pres de lui, et tous les gardes a moins de {@link #PATROL_CALL} blocs accourent.
 *
 * Le comportement du garde est dans son role (Template_Intelligent) : il attaque les monstres a vue, appelle les autres
 * gardes a l'aide et revient toujours a son poste (point de laisse = point d'apparition). Les joueurs sont ignores par
 * defaut ; un joueur qui agresse un autre joueur (ou un garde) devient recherche pendant {@link #WANTED_MS} : chaque
 * garde a moins de {@link #ALERT_RANGE} blocs le voit comme hostile. Celui qui se defend contre un recherche n'est pas
 * recherche lui-meme.
 */
public final class GuardManager {

    private GuardManager() {}

    // ===== Reglages =====
    static final double ALERT_RANGE = 24.0, PATROL_SIGHT = 20.0, PATROL_CALL = 60.0;
    static final long WANTED_MS = 30_000, RESPAWN_MS = 120_000;
    /** Aventurier : duree du contrat, niveaux possibles (tire au hasard par poste). Prix : priceFor(niveau). */
    static final long CONTRACT_MS = 45 * 60_000;                                  // duree d une mission d aventurier
    static final int MAX_COMPANIONS = 2;                                           // aventuriers par joueur
    static final int ADVENTURER_MIN_LEVEL = 20, ADVENTURER_MAX_LEVEL = 600;

    /** Prix de recrutement d'un aventurier selon son niveau (arrondi a 10 : niveau 20 -> 440 or, 600 -> 6 820 or). */
    static long priceFor(int level) {
        return Math.round((100 + level * 5) * PRICE_FACTOR / 10.0) * 10;
    }

    /** Majoration des prix des aventuriers (+120 %). */
    private static final double PRICE_FACTOR = 2.2;

    /** Prenoms des villageois (un par poste, tire de sa graine). */
    private static final String[] VILLAGER_NAMES = {"Aldric", "Bertrand", "Clothilde", "Daphne", "Edmond", "Fleur", "Gaspard",
            "Heloise", "Isaure", "Jehan", "Katell", "Lothaire", "Maelys", "Norbert", "Odile", "Perrine", "Quentin", "Roseline",
            "Sibylle", "Tiphaine", "Ulric", "Valentin", "Wilfried", "Ysolde", "Amaury", "Blanche", "Corentin", "Dorothee",
            "Enguerrand", "Faustine", "Godefroy", "Hortense", "Isidore", "Josselin", "Leonie", "Mahaut", "Nicolas", "Ombeline",
            "Philibert", "Rolande", "Sigismond", "Theodora", "Urbain", "Victoire", "Anselme", "Berthe", "Childeric", "Eudes",
            "Gisele", "Hugues", "Ida", "Lancelin", "Melisende", "Oriane", "Pepin", "Raoul", "Sancie", "Tancrede", "Yvon", "Zelie"};
    private static final double REMOVE_RANGE = 6.0;

    /** Poste de garde : un garde d'un rang, a une position fixe ; guard = UUID du garde actuel (null : a faire apparaitre). */
    public static final class Post {
        final String id;
        final GuardType type;
        final String world;
        final double x, y, z;
        final float yaw;
        volatile UUID guard;
        volatile long respawnAt;
        long skinSeed;                                                      // skin et prenom aleatoires, toujours les memes pour ce poste
        int level;                                                          // aventurier : niveau tire a la pose (0 : celui de NpcData)
        volatile UUID owner;                                                // aventurier : joueur qui l'a recrute (null : libre)
        volatile long contractEnd;
        transient volatile boolean spawning;
        transient volatile com.eldanior.system.party.Party party;            // aventurier recrute : groupe qu'il a rejoint
        transient volatile float hpRatio = 1f;                              // aventurier recrute : vie (affichage du groupe)
        transient float lastHp;                                             // aventurier recrute : vie a la seconde precedente
        // Deplacements (PostMovement, thread du monde)
        transient List<Vector3d> round;
        transient int waypoint;
        transient Vector3d target, lookAt;
        transient Ref<EntityStore> visited;
        transient long pauseUntil;
        transient double bestDistance = Double.MAX_VALUE;
        transient long progressAt;

        Post(String id, GuardType type, String world, double x, double y, double z, float yaw) {
            this.id = id; this.type = type; this.world = world; this.x = x; this.y = y; this.z = z; this.yaw = yaw;
        }

        public String getId() { return id; }
        public GuardType getType() { return type; }
        public String getWorld() { return world; }
        public Vector3d position() { return new Vector3d(x, y, z); }
        public boolean isAlive() { return guard != null && respawnAt == 0; }
        public long getRespawnAt() { return respawnAt; }
    }

    private static final Map<String, Post> posts = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> wanted = new ConcurrentHashMap<>();
    private static final AtomicInteger nextId = new AtomicInteger(1);
    private static Path file;

    // ===== Cycle de vie =====

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("guards.properties");
        load();
        startTimer();
        EldaniorLogger.info("[Guards] " + posts.size() + " poste(s) de garde charge(s).");
    }

    private static int ticks;

    /** Boucle de fond : recherches et deplacements (1 s) ; reapparitions, skins et contrats (5 s). */
    private static void startTimer() {
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                pursueWanted();
                ++ticks;
                watchPosts();
                if (ticks % 5 == 0) { respawnDue(); AdventurerSystem.checkContracts(); }
            } catch (Exception e) {
                EldaniorLogger.error("[Guards] boucle", e);
            }
        }, 5, 1, TimeUnit.SECONDS);
    }

    // ===== Postes =====

    public static Collection<Post> getPosts() { return posts.values(); }

    /** Cree un poste a la position (et l'orientation) donnee et y fait apparaitre son garde. Appeler dans le thread du monde. */
    public static Post place(GuardType type, World world, Vector3d pos, float yaw) {
        Post p = new Post("garde_" + nextId.getAndIncrement(), type, world.getName(), pos.x, pos.y, pos.z, yaw);
        java.util.Random rng = new java.util.Random();
        p.skinSeed = rng.nextLong();
        if (type == GuardType.AVENTURIER) p.level = ADVENTURER_MIN_LEVEL + rng.nextInt(ADVENTURER_MAX_LEVEL - ADVENTURER_MIN_LEVEL + 1);
        posts.put(p.id, p);
        spawn(p, world);
        save();
        return p;
    }

    /** Retire le poste le plus proche (et son garde) a moins de REMOVE_RANGE blocs. Appeler dans le thread du monde. */
    public static Post removeNearest(World world, Vector3d pos) {
        Post best = null;
        double bestD = REMOVE_RANGE * REMOVE_RANGE;
        for (Post p : posts.values()) {
            if (!p.world.equals(world.getName())) continue;
            double d = p.position().distanceSquared(pos);
            if (d <= bestD) { bestD = d; best = p; }
        }
        if (best == null) return null;
        posts.remove(best.id);
        UUID g = best.guard;
        if (g != null) {
            Store<EntityStore> store = world.getEntityStore().getStore();
            Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(g);
            if (ref != null && ref.isValid()) store.removeEntity(ref, RemoveReason.REMOVE);
        }
        save();
        return best;
    }

    /** Fait apparaitre le PNJ d'un poste (thread du monde). Son point de laisse est le poste : il y revient toujours. */
    static void spawn(Post p, World world) {
        spawnAs(p, world, p.type.getRoleId(), p.position());
    }

    /** Fait apparaitre le PNJ du poste avec le role donne (aventurier recrute : role compagnon), a la position donnee. */
    static Ref<EntityStore> spawnAs(Post p, World world, String roleId, Vector3d at) {
        try {
            Store<EntityStore> store = world.getEntityStore().getStore();
            Rotation3f rot = new Rotation3f(0, p.yaw, 0);
            var pair = NPCPlugin.get().spawnNPC(store, roleId, null, at, rot);
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) { EldaniorLogger.warn("[Guards] PNJ non cree : " + roleId); return null; }
            NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
            if (npc != null) npc.saveLeashInformation(p.position(), rot);
            if (p.level > 0)                                                                    // niveau du poste (avant le tirage du mod)
                store.putComponent(ref, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(p.level, roleId, true));
            dress(p, ref, roleId, store);
            if (npc != null && PostMovement.moves(p.type)) PostMovement.start(p, npc, world);
            UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
            p.guard = uuid != null ? uuid.getUuid() : null;
            p.respawnAt = 0;
            EldaniorLogger.info("[Guards] " + p.type.getLabel() + " (" + p.id + ") en poste a " + (int) p.x + " " + (int) p.y + " " + (int) p.z);
            return ref;
        } catch (Exception e) {
            EldaniorLogger.error("[Guards] apparition " + p.id, e);
            return null;
        } finally {
            p.spawning = false;
        }
    }

    /** Apparence du PNJ : skin de joueur aleatoire (stable par poste), nom affiche et armure posee par le code. */
    private static void dress(Post p, Ref<EntityStore> ref, String roleId, Store<EntityStore> store) {
        if (p.type.hasRandomSkin()) applySkin(p, ref, store);
        store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(nameplate(p)));
        List<String> armor = GuardType.COMPANION_ROLE.equals(roleId) ? GuardType.COMPANION_ARMOR : p.type.getArmor();
        if (armor.isEmpty()) return;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        var inv = npc != null ? npc.getInventory() : null;
        if (inv == null || inv.getArmor() == null) return;
        for (String id : armor) {
            short slot = id.endsWith("_Head") ? 0 : id.endsWith("_Chest") ? (short) 1 : id.endsWith("_Hands") ? (short) 2 : (short) 3;
            inv.getArmor().setItemStackForSlot(slot, new ItemStack(id, 1));
        }
    }

    /**
     * Nom affiche (les systemes de plaques des monstres ignorent les PNJ de poste) : pas de niveau, sauf pour l'aventurier
     * (niveau et prix). Villageois : un prenom.
     */
    static String nameplate(Post p) {
        if (p.type == GuardType.VILLAGEOIS) return VILLAGER_NAMES[(int) Math.floorMod(p.skinSeed, (long) VILLAGER_NAMES.length)];
        if (p.type == GuardType.AVENTURIER) return "Aventurier Lv." + p.level;
        return p.type.getLabel();
    }

    private static void applySkin(Post p, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerSkin skin = CosmeticsModule.get().generateRandomSkin(new java.util.Random(p.skinSeed));
        store.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin));
        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(CosmeticsModule.get().createModel(skin)));
    }

    /** Poste d'un PNJ (par son UUID), null si ce n'est pas un PNJ de poste. */
    static Post postOf(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        UUIDComponent u = ref != null && ref.isValid() ? store.getComponent(ref, UUIDComponent.getComponentType()) : null;
        if (u == null) return null;
        for (Post p : posts.values()) if (u.getUuid().equals(p.guard)) return p;
        return null;
    }

    static Post post(String id) { return id != null ? posts.get(id) : null; }

    /** Aventuriers recrutes par ce joueur (au plus MAX_COMPANIONS), dans l'ordre des postes. */
    static List<Post> companionsOf(UUID owner) {
        List<Post> out = new ArrayList<>();
        if (owner == null) return out;
        for (Post p : posts.values()) if (owner.equals(p.owner)) out.add(p);
        out.sort(java.util.Comparator.comparing(p -> p.id));
        return out;
    }

    /** Groupe d'un joueur ou d'un aventurier recrute (celui de son joueur). */
    private static com.eldanior.system.party.Party partyOf(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Post p = postOf(ref, store);
        UUID who = p != null && p.owner != null ? p.owner : PostMovement.uuid(ref, store);
        return who != null ? com.eldanior.system.party.PartyManager.getParty(who) : null;
    }

    /**
     * Pas de coups entre un aventurier recrute et son joueur, ni entre lui et le groupe de son joueur (DivineDamageSystem).
     */
    public static boolean blocksFriendlyFire(Ref<EntityStore> attacker, Ref<EntityStore> target, ComponentAccessor<EntityStore> store) {
        if (attacker == null || target == null || !attacker.isValid() || !target.isValid()) return false;
        Post a = postOf(attacker, store), t = postOf(target, store);
        boolean companionA = a != null && a.owner != null, companionT = t != null && t.owner != null;
        if (!companionA && !companionT) return false;
        UUID ua = PostMovement.uuid(attacker, store), ut = PostMovement.uuid(target, store);
        if ((companionT && t.owner.equals(ua)) || (companionA && a.owner.equals(ut))) return true;
        if (companionA && companionT && a.owner.equals(t.owner)) return true;
        com.eldanior.system.party.Party pa = partyOf(attacker, store);
        return pa != null && pa == partyOf(target, store);
    }

    /** Affichage du groupe : niveau et vie d'un aventurier recrute (null si ce n'en est pas un). */
    public static float[] companionInfo(UUID npc) {
        if (npc == null) return null;
        for (Post p : posts.values()) if (npc.equals(p.guard) && p.owner != null) return new float[]{p.level, p.hpRatio};
        return null;
    }

    static void saveLater() { save(); }

    /** Un garde est mort : son poste le fera reapparaitre apres RESPAWN_MS. */
    static void onGuardDeath(UUID guard) {
        if (guard == null) return;
        for (Post p : posts.values()) {
            if (guard.equals(p.guard) && p.respawnAt == 0) {
                if (p.owner != null) AdventurerSystem.onCompanionDeath(p);
                p.respawnAt = System.currentTimeMillis() + RESPAWN_MS;
                save();
                EldaniorLogger.info("[Guards] " + p.type.getLabel() + " (" + p.id + ") tombe, retour dans " + RESPAWN_MS / 1000 + " s");
            }
        }
    }

    private static void respawnDue() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (Post p : posts.values()) {
            if (p.respawnAt == 0 || now < p.respawnAt || p.spawning) continue;
            World world = Universe.get().getWorld(p.world);
            if (world == null) continue;
            p.spawning = true;
            changed = true;
            world.execute(() -> { spawn(p, world); save(); });
        }
        if (changed) EldaniorLogger.info("[Guards] reapparition des gardes tombes");
    }

    // ===== Recherches (PvP) =====

    static boolean isWanted(UUID player) {
        Long until = player != null ? wanted.get(player) : null;
        return until != null && until > System.currentTimeMillis();
    }

    /** Le joueur devient recherche (ou le reste) : les gardes proches l'attaquent. Thread du monde. */
    static void markWanted(UUID player, Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (player == null) return;
        boolean already = isWanted(player);
        wanted.put(player, System.currentTimeMillis() + WANTED_MS);
        int alerted = alert(ref, store);
        if (!already) EldaniorLogger.info("[Guards] joueur recherche pour PvP (" + alerted + " garde(s) alerte(s))");
    }

    /**
     * Les gardes a moins de ALERT_RANGE du joueur le voient comme hostile jusqu'a la fin de sa recherche. Si un patrouilleur
     * est parmi eux, il appelle aussi tous les gardes a moins de PATROL_CALL de lui, qui accourent.
     */
    private static int alert(Ref<EntityStore> offender, ComponentAccessor<EntityStore> store) {
        if (offender == null || !offender.isValid()) return 0;
        TransformComponent t = store.getComponent(offender, TransformComponent.getComponentType());
        UUIDComponent u = store.getComponent(offender, UUIDComponent.getComponentType());
        if (t == null || u == null) return 0;
        Long until = wanted.get(u.getUuid());
        double seconds = until != null ? Math.max(1, (until - System.currentTimeMillis()) / 1000.0) : WANTED_MS / 1000.0;
        Set<Ref<EntityStore>> alerted = new HashSet<>();
        List<Ref<EntityStore>> near = guardsAround(new Vector3d(t.getPosition()), ALERT_RANGE, store);
        alerted.addAll(near);
        for (Ref<EntityStore> g : near) {
            if (typeOf(g, store) != GuardType.PATROUILLEUR) continue;
            TransformComponent pt = store.getComponent(g, TransformComponent.getComponentType());
            if (pt != null) alerted.addAll(guardsAround(new Vector3d(pt.getPosition()), PATROL_CALL, store));
        }
        for (Ref<EntityStore> g : alerted) {
            com.eldanior.system.util.NpcAttitudes.hostile(g, offender, seconds, store);
            call(g, offender, store);
        }
        return alerted.size();
    }

    /** Fait accourir un garde au repos vers une cible : il la verrouille et passe en alerte (puis en combat a portee). */
    static void call(Ref<EntityStore> guard, Ref<EntityStore> target, ComponentAccessor<EntityStore> store) {
        try {
            StateSupport st = StateSupport.get(guard, store);
            String state = st != null ? st.getStateName() : null;
            if (state == null || !state.startsWith("Idle")) return;                              // deja occupe (combat, retour...)
            MarkedEntitySupport marks = MarkedEntitySupport.get(guard, store);
            if (marks != null) marks.setMarkedEntity(MarkedEntitySupport.DEFAULT_TARGET_SLOT, target);
            st.setState(guard, "Alerted", null, store);
        } catch (Exception e) {
            EldaniorLogger.error("[Guards] appel d'un garde", e);
        }
    }

    private static List<Ref<EntityStore>> guardsAround(Vector3d at, double radius, ComponentAccessor<EntityStore> store) {
        List<Ref<EntityStore>> out = new ArrayList<>();
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(at, radius, store)))
            if (isGuard(e, store)) out.add(e);
        return out;
    }

    private static GuardType typeOf(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        NPCEntity npc = ref != null && ref.isValid() ? store.getComponent(ref, NPCEntity.getComponentType()) : null;
        return npc != null ? GuardType.fromRole(npc.getRoleName()) : null;
    }

    // ===== Deplacements, sentinelles, skins =====

    /**
     * Chaque seconde, pour chaque PNJ de poste charge : deplacement (PostMovement), sentinelle du patrouilleur (un monstre
     * proche : les gardes accourent), et toutes les 5 s skin regenere s'il a ete perdu (rechargement de la zone).
     */
    private static void watchPosts() {
        boolean scan = ticks % 2 == 0, slow = ticks % 5 == 0;
        for (Post p : posts.values()) {
            boolean companion = p.owner != null;
            boolean moves = PostMovement.moves(p.type) && !companion, patrol = p.type == GuardType.PATROUILLEUR;
            boolean stays = !moves && !companion;                                            // garde a son poste
            if (!p.isAlive()) continue;
            World world = Universe.get().getWorld(p.world);
            if (world == null) continue;
            UUID id = p.guard;
            world.execute(() -> {
                try {
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(id);
                    if (ref == null || !ref.isValid()) return;                                // chunk decharge
                    if (slow) {
                        if (p.type.hasRandomSkin() && store.getComponent(ref, PlayerSkinComponent.getComponentType()) == null) applySkin(p, ref, store);
                        Nameplate np = store.getComponent(ref, Nameplate.getComponentType());
                        String name = nameplate(p);
                        if (np == null || !name.equals(np.getText())) store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(name));
                    }
                    NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
                    TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                    if (npc == null || t == null) return;
                    Vector3d at = new Vector3d(t.getPosition());
                    if (companion) { AdventurerSystem.follow(p, ref, npc, at, world, store); return; }
                    if (stays) { if (p.type.isGuard()) faceDuty(p, ref, at, store); return; }   // l'aventurier libre regarde les joueurs
                    PostMovement.tick(p, ref, npc, at, world, store);
                    if (!patrol || !scan) return;
                    Ref<EntityStore> monster = nearestMonster(at, store);
                    if (monster == null) return;
                    for (Ref<EntityStore> g : guardsAround(at, PATROL_CALL, store)) call(g, monster, store);
                } catch (Exception e) {
                    EldaniorLogger.error("[Guards] PNJ " + p.id, e);
                }
            });
        }
    }

    /** Garde revenu a son poste (au repos) : il se remet face a sa direction d'origine. */
    private static void faceDuty(Post p, Ref<EntityStore> ref, Vector3d at, Store<EntityStore> store) {
        if (Math.hypot(at.x - p.x, at.z - p.z) > 1.2) return;
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        if (state == null || !state.startsWith("Idle")) return;
        Vector3d ahead = new Vector3d(p.x + PhysicsMath.headingX(p.yaw) * 6, at.y + 1.6, p.z + PhysicsMath.headingZ(p.yaw) * 6);
        NpcFacing.lookAt(ref, ahead, System.currentTimeMillis() + 1_500);
    }

    /** Monstre (PNJ hostile par defaut aux joueurs) le plus proche, a moins de PATROL_SIGHT. */
    private static Ref<EntityStore> nearestMonster(Vector3d at, ComponentAccessor<EntityStore> store) {
        Ref<EntityStore> best = null;
        double bestD = Double.MAX_VALUE;
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(at, PATROL_SIGHT, store))) {
            NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
            if (npc == null || isGuard(e, store)) continue;
            WorldSupport ws = WorldSupport.get(e, store);
            if (ws == null || ws.getDefaultPlayerAttitude() != Attitude.HOSTILE) continue;
            TransformComponent t = store.getComponent(e, TransformComponent.getComponentType());
            if (t == null) continue;
            double d = t.getPosition().distanceSquared(at);
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    /** Toutes les secondes : un recherche qui s'approche d'autres gardes est aussi pris en chasse par eux. */
    private static void pursueWanted() {
        if (wanted.isEmpty()) return;
        long now = System.currentTimeMillis();
        wanted.entrySet().removeIf(e -> e.getValue() <= now);
        for (UUID id : wanted.keySet()) {
            PlayerRef pr = Universe.get().getPlayer(id);
            Ref<EntityStore> ref = pr != null ? pr.getReference() : null;
            if (ref == null || !ref.isValid()) continue;
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            if (world != null) world.execute(() -> alert(ref, store));
        }
    }

    /** Garde (alerte PvP, appel a l'aide) : gardes, patrouilleur, mage royal. */
    static boolean isGuard(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        GuardType t = typeOf(ref, store);
        return t != null && t.isGuard();
    }

    /** PNJ de poste protege par les gardes (tout sauf l'aventurier recrute, qui suit son joueur). */
    static boolean isProtected(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        NPCEntity npc = ref != null && ref.isValid() ? store.getComponent(ref, NPCEntity.getComponentType()) : null;
        return npc != null && GuardType.fromRole(npc.getRoleName()) != null && !GuardType.COMPANION_ROLE.equals(npc.getRoleName());
    }

    // ===== Persistance =====

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            props.setProperty("nextId", String.valueOf(nextId.get()));
            for (Post p : posts.values()) {
                String k = "post." + p.id + ".";
                props.setProperty(k + "type", p.type.name());
                props.setProperty(k + "world", p.world);
                props.setProperty(k + "pos", p.x + "," + p.y + "," + p.z + "," + p.yaw);
                if (p.guard != null) props.setProperty(k + "guard", p.guard.toString());
                if (p.respawnAt > 0) props.setProperty(k + "respawnAt", String.valueOf(p.respawnAt));
                props.setProperty(k + "skin", String.valueOf(p.skinSeed));
                if (p.level > 0) props.setProperty(k + "level", String.valueOf(p.level));
                if (p.owner != null) props.setProperty(k + "owner", p.owner.toString());
            }
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - postes de garde");
        } catch (Exception e) {
            EldaniorLogger.error("[Guards] sauvegarde", e);
        }
    }

    private static void load() {
        posts.clear();
        if (file == null || !Files.exists(file)) return;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            nextId.set(Integer.parseInt(props.getProperty("nextId", "1")));
            for (String key : props.stringPropertyNames()) {
                if (!key.startsWith("post.") || !key.endsWith(".type")) continue;
                String id = key.substring(5, key.length() - 5), k = "post." + id + ".";
                GuardType type = GuardType.valueOf(props.getProperty(key));
                String[] c = props.getProperty(k + "pos", "0,0,0,0").split(",");
                Post p = new Post(id, type, props.getProperty(k + "world", "default"), Double.parseDouble(c[0]),
                        Double.parseDouble(c[1]), Double.parseDouble(c[2]), Float.parseFloat(c[3]));
                String g = props.getProperty(k + "guard");
                if (g != null) p.guard = UUID.fromString(g);
                p.respawnAt = Long.parseLong(props.getProperty(k + "respawnAt", "0"));
                p.skinSeed = Long.parseLong(props.getProperty(k + "skin", String.valueOf(id.hashCode())));
                p.level = Integer.parseInt(props.getProperty(k + "level", "0"));
                if (type == GuardType.AVENTURIER && p.level <= 0)
                    p.level = ADVENTURER_MIN_LEVEL + (int) Math.floorMod(p.skinSeed, (long) (ADVENTURER_MAX_LEVEL - ADVENTURER_MIN_LEVEL + 1));
                // Aventurier recrute avant l'arret : le compagnon a disparu avec son joueur, il revient a son poste
                if (props.getProperty(k + "owner") != null) p.respawnAt = System.currentTimeMillis() + 10_000;
                posts.put(id, p);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Guards] chargement", e);
        }
    }
}
