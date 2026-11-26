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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * PNJ de poste (gardes, mage royal, patrouilleur, chevalier errant, villageois, aventurier, chasseur de prime, criminel) : postes persistants, apparition
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
    /** PNJ a poste fixe arrete a moins de cette distance de son poste (sans bouger depuis 1 s) : replace exactement dessus. */
    static final double SNAP_RANGE = 2.5;
    static final long WANTED_MS = 30_000, RESPAWN_MS = 120_000, SPAWN_RETRY_MS = 30_000;
    /** Reapparition : seulement si un joueur est a moins de ce rayon (chunk charge). */
    static final double SPAWN_NEAR = 64.0;
    /** Aventurier : duree du contrat, niveaux possibles (tire au hasard par poste). Prix : priceFor(niveau). */
    static final long CONTRACT_MS = 45 * 60_000;                                  // duree d une mission d aventurier
    static final int MAX_COMPANIONS = 2;                                           // aventuriers par joueur
    static final int ADVENTURER_MIN_LEVEL = 20, ADVENTURER_MAX_LEVEL = 600;

    /** Prix de recrutement d'un aventurier selon son niveau (arrondi a 10 : niveau 20 -> 440 or, 600 -> 6 820 or). */
    static long priceFor(int level) {
        return Math.round((100 + level * 5) * PRICE_FACTOR / 10.0) * 10;
    }

    static long priceOf(Post p) {
        return Math.round(priceFor(p.level) * AdventurerTier.of(p).getPriceFactor() / 10.0) * 10;
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
        long bounty;                                                        // criminel : or ajoute a sa prime au tableau
        String shop;                                                        // commerce : "BOUTIQUE:RANG" (boutique Luo) ou "AMBULANT" (LuoMerchants)
        String lines;                                                       // discussion : repliques, une par ligne (NpcServices)
        String quest;                                                       // quetes : identifiant de donneur (QuestModel.npcGiverId)
        String allegiance;                                                  // famille du PNJ : null = celle du territoire, "neutre", ou un id de famille
        String statut;                                                      // statut commun (NpcStatut) des PNJ sans systeme de rang : maitre de guilde, forgeron legendaire
        final List<UUID> allies = new java.util.concurrent.CopyOnWriteArrayList<>();   // criminel : ses complices (Criminals)
        transient String leader;                                            // complice : poste de son chef
        volatile UUID owner;                                                // aventurier : joueur qui l'a recrute (null : libre)
        volatile long contractEnd;
        transient volatile boolean spawning;
        transient volatile com.eldanior.system.party.Party party;            // aventurier recrute : groupe qu'il a rejoint
        transient volatile float hpRatio = 1f;                              // aventurier recrute : vie (affichage du groupe)
        transient float lastHp;                                             // aventurier recrute : vie a la seconde precedente
        transient volatile boolean potionUsed;                              // aventurier recrute : potion bue pendant cette mission
        transient volatile long retreatUntil;                               // aventurier recrute : repli en cours (fin au plus tard)
        transient volatile long drinkAt;                                    // aventurier recrute : fin de la potion en cours
        transient long lastTalk;                                            // aventurier recrute : dernier message (anti-spam)
        // Deplacements (PostMovement, thread du monde)
        transient List<Vector3d> round;
        transient int waypoint;
        transient Vector3d target, lookAt;
        transient Ref<EntityStore> visited;
        transient long pauseUntil;
        transient double bestDistance = Double.MAX_VALUE;
        transient long progressAt;
        // Clan Luo (LuoNpcs) : poste sur le territoire Luo (releve a chaque apparition) ; geste en cours (inclinaison, kata...), il ne bouge ni ne se retourne ; kata enchaines
        transient volatile boolean luo;
        transient volatile String family;                                     // nom de la famille du territoire (nom affiche)
        /** luo et family releves pour ce poste depuis le demarrage (non sauvegardes : au rechargement, le PNJ ne reapparait pas). */
        transient volatile boolean placeKnown;
        transient volatile long luoBusyUntil;
        transient int luoStep;                                                // disciple : kata de la serie en cours
        transient long luoNext;                                               // disciple : fin du repos entre deux series
        transient long sheatheAt;                                             // katana : rengainage en cours (fin de l'animation)
        transient boolean fought;                                             // katana : a combattu depuis le dernier rengainage
        transient boolean snapped;                                            // poste fixe : deja replace sur son poste depuis son retour
        transient double lastX = Double.NaN, lastZ = Double.NaN;              // poste fixe : position a la seconde precedente (faceDuty)

        Post(String id, GuardType type, String world, double x, double y, double z, float yaw) {
            this.id = id; this.type = type; this.world = world; this.x = x; this.y = y; this.z = z; this.yaw = yaw;
        }

        public String getId() { return id; }
        public GuardType getType() { return type; }
        public String getWorld() { return world; }
        public Vector3d position() { return new Vector3d(x, y, z); }
        public boolean isAlive() { return guard != null && respawnAt == 0; }
        public long getRespawnAt() { return respawnAt; }
        public int getLevel() { return level; }
        public long getSkinSeed() { return skinSeed; }
        public UUID getOwner() { return owner; }
        /** Poste sur le territoire du Clan Luo (tenue et comportement du clan). */
        public boolean isLuo() { return luo; }
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
        return place(type, world, pos, yaw, null, null, 0);
    }

    /**
     * Comme ci-dessus ; pour un aventurier ou un chasseur de prime, type, statut et niveau peuvent etre imposes (null / 0 : au
     * hasard). Type et statut venant de la graine du poste, on tire des graines jusqu'a en trouver une qui les donne (chasseur :
     * jamais mage).
     */
    public static Post place(GuardType type, World world, Vector3d pos, float yaw,
                             AdventurerType wantType, AdventurerTier wantTier, int level) {
        return place(type, world, pos, yaw, wantType, wantTier, level, null, null);
    }

    /** Comme ci-dessus ; marchand Luo : boutique et rang imposes (null : au hasard). */
    public static Post place(GuardType type, World world, Vector3d pos, float yaw,
                             AdventurerType wantType, AdventurerTier wantTier, int level,
                             LuoMerchants.Shop shop, LuoMerchants.Rank rank) {
        return place(type, world, pos, yaw, wantType, wantTier, level, shop, rank, null);
    }

    /** Comme ci-dessus ; commercant : metier impose (null : au hasard). */
    public static Post place(GuardType type, World world, Vector3d pos, float yaw,
                             AdventurerType wantType, AdventurerTier wantTier, int level,
                             LuoMerchants.Shop shop, LuoMerchants.Rank rank, NpcTrades.Metier metier) {
        return place(type, world, pos, yaw, wantType, wantTier, level, shop, rank, metier, null);
    }

    /**
     * Comme ci-dessus ; presetShop (non nul) impose Post.shop AVANT l'apparition : rang du commercant (« METIER:X:RANG »),
     * variante du forgeron legendaire (« LEGENDE:NAIN »). La tenue n'est posee qu'a l'apparition.
     */
    public static Post place(GuardType type, World world, Vector3d pos, float yaw,
                             AdventurerType wantType, AdventurerTier wantTier, int level,
                             LuoMerchants.Shop shop, LuoMerchants.Rank rank, NpcTrades.Metier metier, String presetShop) {
        return place(type, world, pos, yaw, wantType, wantTier, level, shop, rank, metier, presetShop, null);
    }

    /** Comme ci-dessus ; statut (NpcStatut.arg, 2026-11-16) garde pour les PNJ sans systeme de rang, avant l'apparition (nom affiche). */
    public static Post place(GuardType type, World world, Vector3d pos, float yaw,
                             AdventurerType wantType, AdventurerTier wantTier, int level,
                             LuoMerchants.Shop shop, LuoMerchants.Rank rank, NpcTrades.Metier metier, String presetShop, String statut) {
        Post p = new Post("garde_" + nextId.getAndIncrement(), type, world.getName(), pos.x, pos.y, pos.z, yaw);
        java.util.Random rng = new java.util.Random();
        p.skinSeed = rng.nextLong();
        if (type == GuardType.MARCHAND) p.shop = LuoMerchants.draw(shop, rank, rng);
        if (type == GuardType.LEGENDAIRE)                                                 // forgeron legendaire : Humain par defaut
            p.shop = LegendarySmiths.value(LegendarySmiths.Smith.HUMAIN);
        if (type == GuardType.COMMERCANT) {                                               // metier : impose (--type) ou tire au sort
            NpcTrades.Metier m = metier != null ? metier : NpcTrades.Metier.values()[rng.nextInt(NpcTrades.Metier.RANDOM)];
            p.shop = NpcTrades.value(m);
        }
        if (type == GuardType.AVENTURIER || type == GuardType.CHASSEUR_PRIME || type == GuardType.CRIMINEL) {
            boolean hunter = type != GuardType.AVENTURIER;                  // chasseur, criminel : jamais mage
            p.level = level > 0 ? Math.clamp(level, ADVENTURER_MIN_LEVEL, ADVENTURER_MAX_LEVEL)
                    : ADVENTURER_MIN_LEVEL + rng.nextInt(ADVENTURER_MAX_LEVEL - ADVENTURER_MIN_LEVEL + 1);
            for (int i = 0; i < 20_000 && (wantType != null || wantTier != null || hunter); i++) {
                AdventurerType t = AdventurerType.of(p.skinSeed);
                if ((wantType != null ? t == wantType : !hunter || BountyHunter.isHunterType(t))
                        && (wantTier == null || AdventurerTier.of(p) == wantTier)) break;
                p.skinSeed = rng.nextLong();
            }
        }
        if (MartialRanks.applies(type) && level > 0) p.level = Math.clamp(level, 1, 999);   // niveau martial regle au poste
        if (type == GuardType.SENSEI && level > 0) p.level = Math.clamp(level, MartialRanks.SENSEI_MIN, MartialRanks.SENSEI_MAX);
        if (presetShop != null) p.shop = presetShop;
        p.statut = statut;
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
        return removeById(best.id, world);
    }

    /** Retire un poste et son PNJ (et les complices d'un criminel). Appeler dans le thread du monde. */
    public static Post removeById(String id, World world) {
        Post best = id != null ? posts.remove(id) : null;
        if (best == null) return null;
        DojoTraining.forget(best.id);                                                     // libere son mannequin
        if (best.type == GuardType.CRIMINEL) Criminals.removeAllies(best, world);
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
        String role = switch (p.type) {
            case AVENTURIER -> AdventurerType.of(p.skinSeed).getRecruitRole();
            case CHASSEUR_PRIME -> BountyHunter.role(p);
            case CRIMINEL -> Criminals.role(p);
            default -> p.type.getRoleId();
        };
        Ref<EntityStore> ref = spawnAs(p, world, role, p.position());
        if (ref != null && p.type == GuardType.CRIMINEL) Criminals.spawnAllies(p, world);
    }

    /** Fait apparaitre le PNJ du poste avec le role donne (aventurier recrute : role compagnon), a la position donnee. */
    static Ref<EntityStore> spawnAs(Post p, World world, String roleId, Vector3d at) {
        try {
            allegiance(p, world);
            p.placeKnown = true;
            roleId = LuoNpcs.role(p, roleId);                                                   // territoire Luo : katana du clan
            Store<EntityStore> store = world.getEntityStore().getStore();
            Rotation3f rot = new Rotation3f(0, p.yaw, 0);
            var pair = NPCPlugin.get().spawnNPC(store, roleId, null, at, rot);
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) {                                                // nouvel essai plus tard
                EldaniorLogger.warn("[Guards] PNJ non cree : " + roleId);
                if (posts.containsKey(p.id)) p.respawnAt = System.currentTimeMillis() + SPAWN_RETRY_MS;
                return null;
            }
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

    /** Apparence du PNJ : skin de joueur aleatoire (stable par poste), nom affiche, armure et arme (aventurier, chasseur : selon son type). */
    private static void dress(Post p, Ref<EntityStore> ref, String roleId, Store<EntityStore> store) {
        if (randomSkin(p)) applySkin(p, ref, store);
        SKINNED.add(ref);                                                      // habille pour cette session (voir SKINNED)
        store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(nameplate(p)));
        AdventurerType adv = p.type == GuardType.AVENTURIER || p.type == GuardType.CHASSEUR_PRIME || p.type == GuardType.CRIMINEL
                             ? AdventurerType.of(p.skinSeed) : null;
        List<String> armor = armorOf(p);
        NPCEntity npc = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
        var inv = npc != null ? npc.getInventory() : null;
        if (inv == null) return;
        if (p.type == GuardType.LEGENDAIRE && inv.getHotbar() != null) {                 // forgeron legendaire : marteau en main droite
            inv.getHotbar().setItemStackForSlot((short) 0, new ItemStack(SmithBehavior.HAMMER, 1));
            inv.setActiveHotbarSlot(ref, (byte) 0, store);
        }
        if (p.type == GuardType.DISCIPLE && inv.getHotbar() != null) {                   // disciple : Bokken du Dojo (role civil, 2026-11-13)
            inv.getHotbar().setItemStackForSlot((short) 0, new ItemStack(Sensei.BOKKEN + "_Degainee", 1));   // au clair : kata
            inv.setActiveHotbarSlot(ref, (byte) 0, store);
        }
        if (adv != null && inv.getHotbar() != null) {                                    // arme de son type
            String luoWeapon = LuoNpcs.weapon(p, adv);
            inv.getHotbar().setItemStackForSlot((short) 0, new ItemStack(luoWeapon != null ? luoWeapon : adv.getWeapon(p), 1));
            inv.setActiveHotbarSlot(ref, (byte) 0, store);
        }
        String offHand = adv != null && LuoNpcs.weapon(p, adv) == null ? adv.getOffHand(p) : null;   // katana : pas de bouclier
        LuoNpcs.equipSheath(p, ref, inv, store);                                        // katana du clan : fourreau en main gauche
        if (offHand != null && inv.getUtility() != null) {                               // bouclier du guerrier
            inv.getUtility().setItemStackForSlot((short) 0, new ItemStack(offHand, 1));
            inv.setActiveUtilitySlot(ref, (byte) 0, store);
        }
        if (armor.isEmpty() || inv.getArmor() == null) return;
        for (String id : armor) {
            short slot = id.endsWith("_Head") ? 0 : id.endsWith("_Chest") ? (short) 1 : id.endsWith("_Hands") ? (short) 2 : (short) 3;
            inv.getArmor().setItemStackForSlot(slot, new ItemStack(id, 1));
        }
    }

    /**
     * Nom affiche (les systemes de plaques des monstres ignorent les PNJ de poste) : pas de niveau, sauf pour l'aventurier et le
     * chasseur de prime (rang). Villageois : un prenom.
     */
    static String nameplate(Post p) {
        if (p.type == GuardType.MARCHAND || p.type == GuardType.AMBULANT) return LuoMerchants.nameplate(p);
        if (p.type == GuardType.LEGENDAIRE) return LegendarySmiths.nameplate(p);
        if (p.type == GuardType.CRIMINEL) return Criminals.nameplate(p);
        String name = p.family != null ? givenName(p) + " " + p.family : givenName(p);   // « Kaito Luo »
        String role = roleName(p);
        String rank = MartialRanks.rankName(p);                                       // rang martial (gardes, chevaliers...)
        if (rank != null) role = p.type == GuardType.SENSEI ? "Sensei, " + rank : rank;   // « Sensei, Saint Martial »
        if (p.type == GuardType.AVENTURIER || p.type == GuardType.CHASSEUR_PRIME) role += " [" + getAdventureRank(p.level) + "]";
        return name + " - " + role + NpcStatut.suffix(p);                             // « Maitre de guilde · Expert » (2026-11-16)
    }

    /** Role court affiche apres le nom : « Garde », « Pelerin », « Ombre » (assassin du Clan Luo)... */
    static String roleName(Post p) {
        return switch (p.type) {
            case VILLE -> "Garde";
            case ROYAL -> "Garde royal";
            case CHEVALIER -> "Chevalier";
            case PATROUILLEUR -> "Patrouilleur";
            case MAGE_ROYAL -> p.luo ? "Alchimiste royal" : "Mage royal";             // pas de magie dans l'Empire Luo
            case ASSASSIN -> p.luo ? "Ombre" : "Assassin";
            case DISCIPLE -> "Disciple";
            case VILLAGEOIS -> "Habitant";
            case COMMERCANT -> NpcTrades.title(p);
            case CHEVALIER_ERRANT -> "Chevalier errant";
            case MAITRE_GUILDE -> "Maître de guilde";
            case AVENTURIER -> "Aventurier";
            case CHASSEUR_PRIME -> "Chasseur de primes";
            default -> p.type.getLabel();
        };
    }
    /** Prenom du PNJ (tire de sa graine) : du clan sur le territoire Luo, sinon du royaume. */
    static String givenName(Post p) {
        return p.luo ? LuoNpcs.givenName(p) : firstName(p);
    }

    /** Nom de la famille noble du territoire du poste (parcelle de famille), null hors des terres de famille. */
    /** Famille du PNJ (reglage du poste) : celle du territoire, aucune (neutre) ou une famille choisie ; tenue Luo si c'est le Clan Luo. */
    static void allegiance(Post p, World world) {
        String a = p.allegiance;
        if (a == null || a.isBlank()) {
            p.luo = com.eldanior.system.luo.LuoClanSystem.onLuoLand(world.getName(), p.position());
            p.family = familyAt(world, p.position());
            // retour du 2026-11-10 (nuit) : terres d'un clan de l'Empire (Sama...) ou d'une zone de l'Empire = tenue de l'Empire
            // (avant : seulement les terres de la famille Luo ; un chevalier pose chez les Sama gardait l'armure par defaut)
            var here = com.eldanior.system.territory.ParcelManager.getParcelAt(world.getName(), p.x, p.y, p.z);
            if (here != null && (com.eldanior.system.luo.LuoClans.isEmpire(here.getFamilyId())
                    || com.eldanior.system.luo.LuoClanSystem.luoRealm(here) != null)) p.luo = true;
            String clan = randomClan(p, world);                                   // empire Luo : un clan au hasard (sauf gardes)
            if (clan != null) { p.family = clan.isEmpty() ? null : clan; p.luo = true; }
        } else if (a.equals(NEUTRAL)) {
            p.luo = false;
            p.family = null;
        } else {
            var family = com.eldanior.system.titles.nobility.family.FamilyManager.get(a);
            p.family = family != null ? family.getDisplayName() : null;
            p.luo = family != null && com.eldanior.system.luo.LuoClans.isEmpire(family.getId());   // Clan Luo ou un de ses clans
        }
    }

    static final String NEUTRAL = "neutre";

    /**
     * PNJ sur les terres de l'Empire Luo (hors gardes, patrouilleurs, chevaliers) : nom d'un des quatre clans ou des Luo, tire
     * de sa graine (decision du 2026-11-10 : aleatoire, pas un reglage). null hors de l'empire ou pour un garde.
     */
    static String randomClan(Post p, World world) {
        if (p.type == GuardType.VILLE || p.type == GuardType.PATROUILLEUR || p.type == GuardType.CHEVALIER) return null;
        var parcel = com.eldanior.system.territory.ParcelManager.getParcelAt(world.getName(), p.x, p.y, p.z);
        if (com.eldanior.system.luo.LuoClanSystem.luoRealm(parcel) == null) return null;
        if (p.type == GuardType.ROYAL || p.type == GuardType.MAGE_ROYAL) {               // au service de l'Empereur : Luo
            var luo = com.eldanior.system.titles.nobility.family.FamilyManager.get(com.eldanior.system.luo.LuoPlaques.FAMILY);
            return luo != null ? luo.getDisplayName() : null;
        }
        var clans = com.eldanior.system.luo.LuoClans.Clan.values();
        int pick = (int) Math.floorMod(p.skinSeed >> 11, (long) clans.length + 2);
        if (pick == clans.length) return null;                                      // nom des Luo (famille du territoire)
        if (pick == clans.length + 1 && civilian(p)) return NO_CLAN;                // simple citoyen : un prenom seulement
        if (pick == clans.length + 1) return null;
        var family = com.eldanior.system.titles.nobility.family.FamilyManager.get(clans[pick].id);
        return family != null ? family.getDisplayName() : null;
    }

    /** Valeur de randomClan : pas de nom de famille du tout (citoyen de l'empire, prenom seul). */
    static final String NO_CLAN = "";

    private static boolean civilian(Post p) {
        return p.type == GuardType.VILLAGEOIS || p.type == GuardType.COMMERCANT || p.type == GuardType.AMBULANT
                || p.type == GuardType.MARCHAND;
    }

    /** Fait reapparaitre le PNJ du poste (famille changee : role, tenue et nom suivent). Thread du monde. */
    static void respawn(Post p, World world) {
        Ref<EntityStore> ref = p.guard != null ? world.getEntityStore().getRefFromUUID(p.guard) : null;
        if (ref != null && ref.isValid()) world.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
        p.guard = null;
        p.respawnAt = 0;
        spawn(p, world);
        save();
    }

    static String familyAt(World world, Vector3d pos) {
        try {
            var parcel = com.eldanior.system.territory.ParcelManager.getParcelAt(world.getName(), pos.x, pos.y, pos.z);
            String id = parcel != null ? parcel.getFamilyId() : null;
            var family = id != null && !id.isEmpty() ? com.eldanior.system.titles.nobility.family.FamilyManager.get(id) : null;
            return family != null ? family.getDisplayName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Prenom du PNJ de ce poste (tire de sa graine). */
    static String firstName(Post p) {
        return VILLAGER_NAMES[(int) Math.floorMod(p.skinSeed, (long) VILLAGER_NAMES.length)];
    }

    /** Tenues de chevalier des familles hors Empire Luo (id de famille -> pieces) ; les autres gardent l'armure du type. */
    private static final java.util.Map<String, List<String>> KNIGHT_ARMOR = java.util.Map.of(
            "runkandel", List.of("Armor_Runkandel_Capitaine_Head", "Armor_Runkandel_Capitaine_Chest",
                    "Armor_Runkandel_Capitaine_Hands", "Armor_Runkandel_Capitaine_Legs"));

    /** Id de la famille du PNJ (reglage du poste, sinon celle du territoire), null si neutre ou sans famille. */
    static String familyIdOf(Post p) {
        if (p.allegiance != null && !p.allegiance.isBlank()) return p.allegiance.equals(NEUTRAL) ? null : p.allegiance;
        if (p.family == null) return null;
        var f = com.eldanior.system.titles.nobility.family.FamilyManager.getByDisplayName(p.family);
        return f != null ? f.getId() : null;
    }

    /** Armure portee par le PNJ du poste (kimono du clan, tenue d'aventurier, ou armure de son type). */
    static List<String> armorOf(Post p) {
        if (p.type == GuardType.LEGENDAIRE) return LegendarySmiths.of(p).armor();
        AdventurerType adv = p.type == GuardType.AVENTURIER || p.type == GuardType.CHASSEUR_PRIME || p.type == GuardType.CRIMINEL
                             ? AdventurerType.of(p.skinSeed) : null;
        if (LuoNpcs.dressed(p)) return LuoNpcs.armor(p);
        if (adv != null) return adv.getArmor(p);
        if (p.type == GuardType.SENSEI) return Sensei.armor(p);               // tenue noble hors de l'Empire Luo
        if (p.type == GuardType.CHEVALIER) {                                  // tenue de chevalier de sa famille, si elle en a une
            List<String> family = KNIGHT_ARMOR.get(familyIdOf(p) == null ? "" : familyIdOf(p));
            if (family != null) return family;
        }
        NpcTrades.Metier m = p.type == GuardType.COMMERCANT ? NpcTrades.metier(p) : null;
        return m != null ? m.armor() : p.type.getArmor();
    }

    /**
     * Retire du skin les vetements cosmetiques que l'armure recouvre : le modele d'un PNJ est construit avec eux
     * (CosmeticsModule.createModel) et le jeu ne les masque pas sous l'armure comme pour un joueur ; ils traversaient le kimono.
     */
    private static void stripCovered(PlayerSkin skin, List<String> armor) {
        for (String id : armor) {
            if (id.endsWith("_Chest")) {
                strip(skin, () -> skin.overtop, v -> skin.overtop = v);
                strip(skin, () -> skin.cape, v -> skin.cape = v);
                strip(skin, () -> skin.undertop, v -> skin.undertop = v);
            } else if (id.endsWith("_Legs")) {
                strip(skin, () -> skin.overpants, v -> skin.overpants = v);
                strip(skin, () -> skin.shoes, v -> skin.shoes = v);
                strip(skin, () -> skin.pants, v -> skin.pants = v);
            } else if (id.endsWith("_Hands")) {
                strip(skin, () -> skin.gloves, v -> skin.gloves = v);
            } else if (id.endsWith("_Head")) {
                strip(skin, () -> skin.headAccessory, v -> skin.headAccessory = v);
                strip(skin, () -> skin.faceAccessory, v -> skin.faceAccessory = v);
                strip(skin, () -> skin.earAccessory, v -> skin.earAccessory = v);
            }
        }
    }

    /**
     * Retire un vetement du skin s'il reste valide sans lui, sinon le garde. Avant, tout etait retire d'un coup et un skin refuse
     * revenait complet (vetements qui traversent le kimono : retour du 2026-11-10 sur un habitant en Pelerin).
     */
    private static void strip(PlayerSkin skin, java.util.function.Supplier<String> get, java.util.function.Consumer<String> set) {
        String before = get.get();
        if (before == null) return;
        set.accept(null);
        try {
            CosmeticsModule.get().validateSkin(skin);
        } catch (Exception refused) {
            set.accept(before);
        }
    }

    /**
     * Entites habillees par applySkin pendant cette session. Au rechargement (redemarrage, chunk recharge), le moteur
     * (ModelSystems.ApplyRandomSkin, composant ApplyRandomSkinPersistedComponent du role) leur donne un NOUVEAU skin aleatoire,
     * complet (vetements qui traversent l'armure) : le PlayerSkinComponent n'est donc jamais nul et ne suffit pas a savoir
     * s'il faut rhabiller. Une entite rechargee a une nouvelle Ref (egalite par identite) : absente d'ici, elle est rhabillee.
     */
    private static final java.util.Set<Ref<EntityStore>> SKINNED =
            java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>()));

    /**
     * Le PNJ doit-il etre (re)habille : skin, nom, armure, arme ? Oui la premiere fois qu'il est vu dans la session
     * (redemarrage, zone rechargee : le moteur lui a remis un skin aleatoire, l'armure et l'arme peuvent manquer).
     */
    static boolean needsSkin(Post p, Ref<EntityStore> ref) {
        return !SKINNED.contains(ref);
    }

    private static void applySkin(Post p, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerSkin skin = CosmeticsModule.get().generateRandomSkin(new java.util.Random(p.skinSeed));
        if (p.type == GuardType.AVENTURIER && !LuoNpcs.dressed(p)) wearHat(skin, AdventurerType.of(p.skinSeed).getHat(p));
        PlayerSkin full = new PlayerSkin(skin);
        stripCovered(skin, armorOf(p));
        com.hypixel.hytale.server.core.asset.type.model.config.Model model;
        try {
            model = CosmeticsModule.get().createModel(skin);
        } catch (Exception e) {                                                   // skin refuse sans ces vetements : skin complet
            EldaniorLogger.warn("[Guards] skin allege refuse (" + p.id + ") : " + e.getMessage());
            skin = full;
            model = CosmeticsModule.get().createModel(skin);
        }
        store.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin));
        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
        SKINNED.add(ref);
    }

    /** Skin de joueur aleatoire : types qui en ont un, et tout PNJ habille en kimono sur le territoire Luo (modele du garde remplace). */
    static boolean randomSkin(Post p) {
        return p.type.hasRandomSkin() || LuoNpcs.dressed(p);
    }

    /** Chapeau du mage : le premier candidat que le skin accepte (sinon son accessoire de tete d'origine). */
    private static void wearHat(PlayerSkin skin, List<String> candidates) {
        String before = skin.headAccessory;
        for (String hat : candidates) {
            skin.headAccessory = hat;
            try {
                CosmeticsModule.get().validateSkin(skin);
                return;
            } catch (Exception ignored) { }
        }
        skin.headAccessory = before;
    }

    /** Poste d'un PNJ (par son UUID), null si ce n'est pas un PNJ de poste. */
    static Post postOf(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        UUIDComponent u = ref != null && ref.isValid() ? store.getComponent(ref, UUIDComponent.getComponentType()) : null;
        if (u == null) return null;
        for (Post p : posts.values()) if (u.getUuid().equals(p.guard)) return p;
        return null;
    }

    /** Poste d'un aventurier recrute (pour sa magie, AdventurerMagic), null sinon. */
    public static Post companionPost(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Post p = postOf(ref, store);
        return p != null && p.type == GuardType.AVENTURIER && p.owner != null ? p : null;
    }

    /** Poste d'un aventurier, libre ou recrute (pour son aura, AdventurerMagic), null sinon. */
    public static Post adventurerPost(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Post p = postOf(ref, store);
        return p != null && p.type == GuardType.AVENTURIER ? p : null;
    }

    public static Post post(String id) { return id != null ? posts.get(id) : null; }

    /** Poste d'un chasseur de prime (par l'UUID du PNJ), null sinon. */
    static Post hunterPost(UUID npc) {
        if (npc == null) return null;
        for (Post p : posts.values()) if (npc.equals(p.guard) && p.type == GuardType.CHASSEUR_PRIME) return p;
        return null;
    }

    /** PNJ dont les kills sont suivis (CombatTrackerSystem) : aventurier recrute, chasseur de prime. */
    public static boolean tracksKills(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        Post p = postOf(ref, store);
        return p != null && (p.owner != null || p.type == GuardType.CHASSEUR_PRIME);
    }

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
        if (companionA && t != null) return true;                                       // ses runes epargnent gardes, villageois, aventuriers
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

    public static String companionClass(UUID npc) {
        if (npc != null)
            for (Post p : posts.values()) {
                if (!npc.equals(p.guard) || p.owner == null) continue;
                var cls = AdventurerTier.classOf(p);
                return cls != null ? cls.getDisplayName() : "Aventurier " + AdventurerType.of(p.skinSeed).getLabel();
            }
        return "Aventurier";
    }

    /** Joueur qui a recrute cet aventurier (par l'UUID du PNJ), null si ce n'est pas un aventurier recrute. */
    public static UUID ownerOfCompanion(UUID npc) {
        if (npc == null) return null;
        for (Post p : posts.values()) if (npc.equals(p.guard) && p.owner != null) return p.owner;
        return null;
    }

    static void saveLater() { save(); }

    /**
     * Le PNJ d'un poste a ete tue en duel (son combattant est tombe, SenseiDuel) : le PNJ civil cache disparait et son poste le
     * fera reapparaitre comme apres une mort (2026-11-13 : duels de disciples a mort).
     */
    public static void killedInDuel(String postId) {
        Post p = post(postId);
        if (p == null || p.guard == null) return;
        UUID dead = p.guard;
        World world = Universe.get().getWorld(p.world);
        if (world != null) world.execute(() -> {
            Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(dead);
            if (ref != null && ref.isValid()) world.getEntityStore().getStore().removeEntity(ref, com.hypixel.hytale.component.RemoveReason.REMOVE);
        });
        onGuardDeath(dead);
    }

    /** Un garde est mort : son poste le fera reapparaitre apres RESPAWN_MS (criminel : Criminals.RESPAWN_MS, prime remise a sa base). */
    static void onGuardDeath(UUID guard) {
        if (guard == null) return;
        for (Post p : posts.values()) {
            if (guard.equals(p.guard) && p.respawnAt == 0) {
                if (p.owner != null) AdventurerSystem.onCompanionDeath(p);
                boolean criminal = p.type == GuardType.CRIMINEL;
                if (criminal) p.bounty = 0;
                long delay = criminal ? Criminals.RESPAWN_MS : RESPAWN_MS;
                p.respawnAt = System.currentTimeMillis() + delay;
                save();
                EldaniorLogger.info("[Guards] " + p.type.getLabel() + " (" + p.id + ") tombe, retour dans " + delay / 1000 + " s");
            }
        }
    }

    /**
     * Postes dont le PNJ doit reapparaitre, seulement si un joueur est a moins de SPAWN_NEAR blocs : loin des joueurs, le chunk
     * n'est pas charge ; spawnNPC y echoue mais cree quand meme l'entite (sans skin ni stats), et chaque nouvel essai en
     * ajoutait une (des centaines de PNJ fantomes).
     */
    private static void respawnDue() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (Post p : posts.values()) {
            if (p.respawnAt == 0 || now < p.respawnAt || p.spawning) continue;
            World world = Universe.get().getWorld(p.world);
            if (world == null || !playerNear(p.world, p.position(), SPAWN_NEAR)) continue;
            p.spawning = true;
            changed = true;
            world.execute(() -> { spawn(p, world); save(); });
        }
        if (changed) EldaniorLogger.info("[Guards] reapparition des gardes tombes");
    }

    /** Un joueur du monde world a moins de range blocs (positions relevees par PlayerPositionTracker). */
    static boolean playerNear(String world, Vector3d at, double range) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> ref = pr.getReference();
            World w = ref != null && ref.isValid() ? ref.getStore().getExternalData().getWorld() : null;
            Vector3d pos = w != null && w.getName().equals(world)
                           ? com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid()) : null;
            if (pos != null && pos.distanceSquared(at) <= range * range) return true;
        }
        return false;
    }

    /** PNJ d'un poste ou complice d'un criminel (les autres PNJ a role de poste sont orphelins : GuardOrphanSystem). */
    static boolean isKnown(UUID npc) {
        if (npc == null) return false;
        for (Post p : posts.values()) if (npc.equals(p.guard) || p.allies.contains(npc)) return true;
        return false;
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
     * proche : les gardes accourent), chasse aux PK du chasseur de prime et attaque des criminels (toutes les 2 s), et toutes
     * les 5 s skin du poste reapplique apres un rechargement (redemarrage, zone rechargee : voir SKINNED).
     */
    private static void watchPosts() {
        boolean scan = ticks % 2 == 0, slow = ticks % 5 == 0;
        for (Post p : posts.values()) {
            boolean companion = p.owner != null;
            boolean moves = PostMovement.moves(p.type) && !companion, patrol = p.type == GuardType.PATROUILLEUR;
            boolean hunter = p.type == GuardType.CHASSEUR_PRIME, criminal = p.type == GuardType.CRIMINEL;
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
                    if (!p.placeKnown) {                                                    // redemarrage : PNJ recharge, pas reapparu
                        allegiance(p, world);
                        p.placeKnown = true;
                    }
                    if (slow) {
                        if (needsSkin(p, ref)) dress(p, ref, null, store);
                        Nameplate np = store.getComponent(ref, Nameplate.getComponentType());
                        String name = nameplate(p);
                        if (np == null || !name.equals(np.getText())) store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(name));
                    }
                    NPCEntity npc = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
                    TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                    if (npc == null || t == null) return;
                    if (slow && !companion && p.type == GuardType.AVENTURIER
                            && !AdventurerType.of(p.skinSeed).getRecruitRole().equals(npc.getRoleName())) {
                        store.removeEntity(ref, RemoveReason.REMOVE);                    // role de son type
                        p.guard = null;
                        spawn(p, world);
                        save();
                        return;
                    }
                    Vector3d at = new Vector3d(t.getPosition());
                    if (p.luo) LuoNpcs.sheathTick(p, ref, npc, store);                         // rengaine au repos, degaine en combat
                    if (LuoNpcs.animates(p)) {                                                  // gestes ; immobile pendant un geste
                        LuoNpcs.tick(p, ref, store);
                        if (p.luoBusyUntil > System.currentTimeMillis()) {                       // (au poste : sa laisse reste le poste)
                            if (moves) npc.setLeashPoint(new Vector3d(at));
                            return;
                        }
                    }
                    if (companion) { AdventurerSystem.follow(p, ref, npc, at, world, store); return; }
                    if (p.type == GuardType.LEGENDAIRE && !companion && SmithBehavior.tick(p, ref, npc, at, world, store)) return;   // etabli, tabouret
                    if (p.type == GuardType.DISCIPLE && !companion && DojoTraining.tick(p, ref, npc, at, world, store)) return;     // mannequin du dojo
                    if (stays) { faceDuty(p, ref, at, store); return; }                        // au repos : face a sa direction (bloc de poste, commande)
                    PostMovement.tick(p, ref, npc, at, world, store);
                    if (hunter && scan) BountyHunter.hunt(ref, at, store);
                    if (criminal) {
                        if (scan) Criminals.aggress(ref, at, store, p.id);
                        Criminals.lead(p, at, world, store, scan, slow);
                    }
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

    /** Skin et nom d'un PNJ hors des postes enregistres (complice) : regeneres s'ils ont ete perdus (rechargement de la zone). */
    static void refreshLook(Post p, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (needsSkin(p, ref)) dress(p, ref, null, store);
        Nameplate np = store.getComponent(ref, Nameplate.getComponentType());
        String name = nameplate(p);
        if (np == null || !name.equals(np.getText())) store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(name));
    }

    /**
     * PNJ revenu a son poste (au repos, etat Idle) : il se remet face a sa direction d'origine (regard choisi au bloc de poste,
     * ou direction du poseur). Les roles interactifs passent en etat Watching quand un joueur approche : ils le regardent alors.
     */
    private static void faceDuty(Post p, Ref<EntityStore> ref, Vector3d at, Store<EntityStore> store) {
        double d = Math.hypot(at.x - p.x, at.z - p.z);
        boolean still = !Double.isNaN(p.lastX) && Math.hypot(at.x - p.lastX, at.z - p.lastZ) < 0.05;
        p.lastX = at.x;
        p.lastZ = at.z;
        if (d > SNAP_RANGE) { p.snapped = false; return; }
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        if (state == null || !state.startsWith("Idle")) return;
        if (d > 0.35 && still && !p.snapped) {                       // arrete a cote du poste (le bloc de poste occupe sa case) : une fois
            p.snapped = true;
            try {
                store.putComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(),
                        new com.hypixel.hytale.server.core.modules.entity.teleport.Teleport(new Vector3d(p.x, p.y, p.z), new Rotation3f(0, p.yaw, 0)));
            } catch (Exception e) {
                EldaniorLogger.error("[Guards] retour au poste " + p.id, e);
            }
            return;
        }
        if (d > 1.2) return;
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
            if (!GuardType.isOutlaw(npc.getRoleName()) && (ws == null || ws.getDefaultPlayerAttitude() != Attitude.HOSTILE)) continue;
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

    /** PNJ de poste protege par les gardes (tout sauf l'aventurier recrute, qui suit son joueur, et le criminel). */
    static boolean isProtected(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        NPCEntity npc = ref != null && ref.isValid() ? store.getComponent(ref, NPCEntity.getComponentType()) : null;
        return npc != null && GuardType.isAlly(npc.getRoleName()) && !GuardType.isCompanionRole(npc.getRoleName());
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
                if (p.bounty > 0) props.setProperty(k + "bounty", String.valueOf(p.bounty));
                if (p.shop != null) props.setProperty(k + "shop", p.shop);
                if (p.lines != null && !p.lines.isBlank()) props.setProperty(k + "lines", p.lines);
                if (p.quest != null && !p.quest.isBlank()) props.setProperty(k + "quest", p.quest);
                if (p.allegiance != null && !p.allegiance.isBlank()) props.setProperty(k + "allegiance", p.allegiance);
                if (p.statut != null && !p.statut.isBlank()) props.setProperty(k + "statut", p.statut);
                if (!p.allies.isEmpty()) props.setProperty(k + "allies", String.join(",", p.allies.stream().map(UUID::toString).toList()));
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
                GuardType type;
                try { type = "PELERIN".equals(props.getProperty(key)) ? GuardType.VILLAGEOIS : GuardType.valueOf(props.getProperty(key)); }
                catch (IllegalArgumentException unknown) { EldaniorLogger.warn("[Guards] type de poste inconnu ignore : " + props.getProperty(key)); continue; }
                String[] c = props.getProperty(k + "pos", "0,0,0,0").split(",");
                Post p = new Post(id, type, props.getProperty(k + "world", "default"), Double.parseDouble(c[0]),
                        Double.parseDouble(c[1]), Double.parseDouble(c[2]), Float.parseFloat(c[3]));
                String g = props.getProperty(k + "guard");
                if (g != null) p.guard = UUID.fromString(g);
                p.respawnAt = Long.parseLong(props.getProperty(k + "respawnAt", "0"));
                p.skinSeed = Long.parseLong(props.getProperty(k + "skin", String.valueOf(id.hashCode())));
                p.level = Integer.parseInt(props.getProperty(k + "level", "0"));
                p.bounty = Long.parseLong(props.getProperty(k + "bounty", "0"));
                p.shop = props.getProperty(k + "shop");
                p.lines = props.getProperty(k + "lines");
                p.quest = props.getProperty(k + "quest");
                p.allegiance = props.getProperty(k + "allegiance");
                p.statut = props.getProperty(k + "statut");
                if (type == GuardType.MARCHAND && p.shop == null) p.shop = LuoMerchants.draw(null, null, new java.util.Random(p.skinSeed));
                for (String a : props.getProperty(k + "allies", "").split(",")) if (!a.isBlank()) p.allies.add(UUID.fromString(a.trim()));
                if ((type == GuardType.AVENTURIER || type == GuardType.CHASSEUR_PRIME || type == GuardType.CRIMINEL) && p.level <= 0)
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
