package com.eldanior.system.familiers;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.familiers.Espece.Age;
import com.eldanior.system.skills.runes.spells.FamilierSpells;
import com.eldanior.system.util.NpcAttitudes;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.builtin.mounts.MountPlugin;
import com.hypixel.hytale.builtin.mounts.NPCMountComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Familiers : un animal de compagnie par joueur, ne d'un oeuf (bloc Eldanior_Oeuf_<Espece>, vendu par le Dresseur de betes).
 *
 * - Oeuf pose : il se lie au joueur qui le pose et eclot apres 5 min. Le familier attend son maitre au nid, puis le suit.
 *   Touche d'interaction sur l'oeuf (maitre) : il suit / il reste ou il est. L'oeuf reste au sol comme nid (etat "Eclos").
 * - Il grandit avec le temps (Jeune, Adolescent, Adulte : role et modele changent) et gagne des niveaux en combattant
 *   (XP par coup porte et par victoire) : PV et degats montent avec le niveau et l'age.
 * - Il defend son maitre (monstres proches, ce que le maitre frappe et ce qui le frappe) et lance ses capacites
 *   (FamilierSpells). Adolescent ou adulte, son maitre peut le monter (InteractionInstruction du gabarit, reservee a ceux
 *   envers qui il est Friendly : son maitre, attitude reposee chaque seconde).
 * - Rarete et element (2026-11-13) : l'espece a une rarete (Commun ... Legendaire) ; a l'eclosion, la bete tire un element
 *   de cette rarete (Element ; fixe pour les legendaires : Fenrir = Glace). Elle n'a AUCUNE competence avant l'age adulte :
 *   adulte, son maitre la fait evoluer dans la fenetre de gestion (evolve) et elle recoit une competence tiree au hasard
 *   parmi celles de son espece et de son element (Espece.skillPool). Fenrir au niveau maximal : Porte de Niflheim en plus.
 * - Nourriture (feed) : un aliment du sac (FOODS, vendus par le Dresseur de betes) soigne le familier et lui donne un peu
 *   d'XP, une fois par minute.
 * - Mort (2026-11-13, decision de l'utilisateur) : pas de retour. L'oeuf est detruit, le lien rompu, et le maitre subit
 *   -20 % d'experience pendant 7 jours (xpFactor).
 * - Ramasser son oeuf (le casser) : le familier s'y retire ; reposer l'oeuf le fait revenir. Un seul familier par joueur
 *   (fenetre de l'oeuf, « Relacher », pour s'en separer).
 *
 * L'entite n'est jamais sauvegardee (NonSerialized) : elle est recreee pres du maitre connecte. Les donnees sont dans
 * eldanior_data/familiers.json.
 */
public final class FamilierManager {

    private FamilierManager() {}

    // ===== Durees (2026-11-13 : valeurs de test demandees par l'utilisateur ; TEST = false pour les vraies) =====
    static final boolean TEST = true;
    static final long MIN = 60_000L, HOUR = 60 * MIN, DAY = 24 * HOUR;
    static long incubationMs() { return 5 * MIN; }
    static long adolescentAt() { return TEST ? 5 * MIN : DAY; }
    static long adulteAt() { return TEST ? 15 * MIN : 3 * DAY; }
    static final long MALUS_MS = 7 * DAY;
    public static final float MALUS_FACTOR = 0.8f;

    // ===== Reglages =====
    private static final double FOLLOW_BEHIND = 2.4, FOLLOW_SIDE = 1.3, CATCH_UP = 40, STAY_SHOW = 48, STAY_HIDE = 72;
    private static final double GREET_RANGE = 16, DANGER_RANGE = 12, FIGHT_SECONDS = 15, SIT_NEAR = 3.2, SIT_LEAVE = 5;
    private static final long COMBAT_MS = 6_000, GATE_CD = 180_000, FEED_CD = 60_000;
    private static final float REGEN_PER_SECOND = 0.03f;
    private static final String HP_MODIFIER = "Eldanior_Familier_Vie", ROLE_PREFIX = "Eldanior_Familier_", EGG_PREFIX = "Eldanior_Oeuf_";
    private static final String STATE_OPEN = "Eclos", STATE_CLOSED = "default";

    private static final Map<UUID, Familier> PETS = new ConcurrentHashMap<>();       // par maitre
    private static final Map<UUID, UUID> ENTITIES = new ConcurrentHashMap<>();       // entite -> maitre
    private static final Map<UUID, Long> MALUS = new ConcurrentHashMap<>();          // maitre -> fin du malus d'XP
    private static Path file;
    private static volatile boolean dirty;

    // =====================================================================================================================
    // Cycle de vie
    // =====================================================================================================================

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("familiers.json");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { tick(); } catch (Exception e) { EldaniorLogger.error("[Familiers] boucle", e); }
        }, 5, 1, TimeUnit.SECONDS);
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> { if (dirty) save(); }, 20, 20, TimeUnit.SECONDS);
        EldaniorLogger.info("[Familiers] " + PETS.size() + " familier(s) charge(s)" + (TEST ? " (durees de test)" : "") + ".");
    }

    public static void shutdown() { if (file != null) save(); }

    public static void handleDisconnect(UUID uuid) {
        Familier f = uuid != null ? PETS.get(uuid) : null;
        if (f == null || f.entity == null) return;
        World w = Universe.get().getWorld(f.entityWorld);
        if (w != null) w.execute(() -> despawn(w, f));
    }

    public static Familier of(UUID owner) { return owner != null ? PETS.get(owner) : null; }

    public static boolean isPetRole(String role) { return role != null && role.startsWith(ROLE_PREFIX); }

    public static boolean isEgg(BlockType type) {
        Item item = type != null ? type.getItem() : null;
        return item != null && item.getId() != null && item.getId().startsWith(EGG_PREFIX);
    }

    static Espece especeOf(BlockType type) {
        Item item = type != null ? type.getItem() : null;
        return item != null ? Espece.ofEgg(item.getId()) : null;
    }

    /** Familier dont l'oeuf est a cette position. */
    static Familier atEgg(String world, Vector3i at) {
        for (Familier f : PETS.values())
            if (world.equals(f.eggWorld) && f.eggX == at.x() && f.eggY == at.y() && f.eggZ == at.z()) return f;
        return null;
    }

    // ===== Malus d'experience (mort du familier) =====

    /** Multiplicateur de l'XP gagnee par ce joueur (0,8 pendant les 7 jours qui suivent la mort de son familier). */
    public static float xpFactor(UUID player) {
        Long until = player != null ? MALUS.get(player) : null;
        if (until == null || isAdmin(player)) return 1f;                                   // admins : ni malus ni restriction (tests)
        if (System.currentTimeMillis() >= until) { MALUS.remove(player, until); dirty = true; return 1f; }
        return MALUS_FACTOR;
    }

    public static int applyXp(UUID player, int xp) {
        float k = xpFactor(player);
        return k == 1f ? xp : Math.max(xp > 0 ? 1 : 0, Math.round(xp * k));
    }

    /** Administrateur connecte : pas de malus, pas de limite d'un familier (un nouvel oeuf remplace l'ancien lien). */
    static boolean isAdmin(UUID player) {
        PlayerRef pr = player != null ? Universe.get().getPlayer(player) : null;
        return pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
    }

    static long malusUntil(UUID player) { Long u = MALUS.get(player); return u != null ? u : 0; }

    static boolean clearMalus(UUID player) { dirty = true; return MALUS.remove(player) != null; }

    // =====================================================================================================================
    // Boucle (chaque seconde)
    // =====================================================================================================================

    private static void tick() {
        for (Familier f : PETS.values()) {
            if (f.queued) continue;
            PlayerRef pr = Universe.get().getPlayer(f.owner);
            Ref<EntityStore> oref = pr != null ? pr.getReference() : null;
            World ow = oref != null && oref.isValid() ? oref.getStore().getExternalData().getWorld() : null;
            World ew = f.entity != null ? Universe.get().getWorld(f.entityWorld) : null;
            if (ew != null && ew != ow) { queue(f, ew, () -> despawn(ew, f)); continue; }     // maitre deconnecte ou ailleurs
            World eggW = f.hasEgg() ? Universe.get().getWorld(f.eggWorld) : null;
            World target = ow != null ? ow : eggW;
            if (target == null) continue;
            Ref<EntityStore> owner = target == ow ? oref : null;
            queue(f, target, () -> update(target, f, owner, pr));
        }
    }

    private static void queue(Familier f, World world, Runnable r) {
        f.queued = true;
        world.execute(() -> {
            try { r.run(); } catch (Exception e) { EldaniorLogger.error("[Familiers] " + f.name + " de " + f.ownerName, e); }
            finally { f.queued = false; }
        });
    }

    private static void update(World world, Familier f, Ref<EntityStore> ownerRef, PlayerRef pr) {
        long now = System.currentTimeMillis();
        if (f.incubationEnd > 0 && now >= f.incubationEnd) hatch(f, now);
        syncEgg(world, f);
        if (ownerRef == null || !ownerRef.isValid() || PETS.get(f.owner) != f) return;      // maitre absent : minuteurs seulement
        Store<EntityStore> store = world.getEntityStore().getStore();
        Ref<EntityStore> pet = refOf(world, f);
        if (!f.hatched() || !f.hasEgg()) { if (pet != null) despawn(world, f); return; }
        if (pet != null && store.getComponent(pet, DeathComponent.getComponentType()) != null) { die(world, f, pet, store, null); return; }

        Vector3d op = position(ownerRef, store);
        if (op == null) return;
        Vector3d egg = new Vector3d(f.eggX + 0.5, f.eggY, f.eggZ + 0.5);
        boolean eggHere = world.getName().equals(f.eggWorld);
        if (f.waiting && eggHere && op.distance(egg) <= GREET_RANGE) {                          // il reconnait son maitre
            f.waiting = false;
            f.following = true;
            dirty = true;
            tell(f, f.name + " vous a reconnu et vous suit. Interagissez avec son œuf pour qu'il reste quelque part.", "#bfe8ff");
            if (pet != null) FamilierSpells.gesture(pet, "Greet", store);
        }

        Vector3d anchor;
        boolean show;
        Vector3d petAt = pet != null ? position(pet, store) : null;
        if (f.following) {
            // 2026-11-13 (demande de l'utilisateur) : pres de son maitre il s'assoit ; a plus de SIT_LEAVE blocs, il le suit
            double d = petAt != null ? petAt.distance(op) : Double.MAX_VALUE;
            if (f.sitting && d > SIT_LEAVE) f.sitting = false;
            else if (!f.sitting && d <= SIT_NEAR && now - f.lastCombat >= COMBAT_MS) {
                f.sitting = true;
                FamilierSpells.gesture(pet, "Sit", store);
            }
            anchor = f.sitting && petAt != null ? petAt : behind(ownerRef, op, store);
            show = true;
        } else {
            boolean atNest = f.waiting || f.stayWorld == null;
            anchor = atNest ? new Vector3d(egg).add(1.2, 0, 0) : new Vector3d(f.stayX, f.stayY, f.stayZ);
            String w = atNest ? f.eggWorld : f.stayWorld;
            show = world.getName().equals(w) && op.distance(anchor) <= (pet != null ? STAY_HIDE : STAY_SHOW);
        }
        if (!show) { if (pet != null) despawn(world, f); return; }
        if (pet == null) {
            if (now >= f.nextSpawnTry) {
                f.nextSpawnTry = now + 3_000;
                Ref<EntityStore> born = spawn(world, f, anchor, store);
                if (born != null && f.waiting) FamilierSpells.bloom(anchor, 0.8f, store);
            }
            return;
        }

        Vector3d pp = position(pet, store);
        if (pp == null) return;
        NPCMountComponent mount = store.getComponent(pet, NPCMountComponent.getComponentType());
        if (mount != null) { checkRider(f, mount, store); f.hpRatio = hpRatio(pet, store); return; }     // monte : rien a piloter
        Age age = f.age(now);
        if (age != f.spawnedAge) { grow(world, f, pet, pp, age, store); return; }
        if (f.following && pp.distance(op) > CATCH_UP) {                                         // teleportation du maitre
            despawn(world, f);
            spawn(world, f, anchor, store);
            return;
        }
        NPCEntity npc = store.getComponent(pet, NPCEntity.getComponentType());
        if (npc != null) npc.setLeashPoint(anchor);
        NpcAttitudes.force(pet, ownerRef, Attitude.FRIENDLY, 5, store);                            // monture : son maitre seulement
        if (f.appliedLevel != f.level) applyStats(f, pet, store, false);
        boolean fighting = now - f.lastCombat < COMBAT_MS;
        if (!fighting) regen(pet, store);
        guard(f, pet, f.following ? op : pp, store);
        if (fighting) abilities(f, pet, pp, age, now, store);
        f.hpRatio = hpRatio(pet, store);
    }

    // =====================================================================================================================
    // Oeuf : pose, ramassage, interaction
    // =====================================================================================================================

    /** Pose verifiee (le bloc est la) : creation du lien, ou retour du familier dans son oeuf repose. */
    static void placed(World world, Vector3i at, PlayerRef pr, Espece espece) {
        long now = System.currentTimeMillis();
        Familier f = PETS.get(pr.getUuid());
        if (f != null && (f.hasEgg() || f.espece != espece)) {                              // admin : l'ancien lien est rompu
            release(f.owner);
            say(pr, "Administrateur : votre ancien familier (" + f.name + ") est relâché.", "#a9b8c8");
            f = null;
        }
        if (f == null) {
            f = new Familier(pr.getUuid(), pr.getUsername(), espece);
            f.incubationEnd = now + incubationMs();
            PETS.put(f.owner, f);
            setEgg(f, world, at);
            tell(f, "L'œuf de " + espece.label + " est désormais lié à vous. Il éclora dans " + duration(incubationMs())
                    + ". Un seul compagnon par maître.", "#bfe8ff");
        } else {
            setEgg(f, world, at);
            if (!f.hatched()) {
                f.incubationEnd = now + Math.max(1_000, f.incubationLeft);
                f.incubationLeft = 0;
                tell(f, "L'œuf reprend son incubation : éclosion dans " + duration(f.incubationEnd - now) + ".", "#bfe8ff");
            } else {
                f.waiting = true;
                f.following = false;
                tell(f, f.name + " sort de son œuf et vous attend au nid.", "#bfe8ff");
            }
        }
        f.ownerName = pr.getUsername();
        syncEgg(world, f);
        save();
    }

    private static void setEgg(Familier f, World world, Vector3i at) {
        f.eggWorld = world.getName();
        f.eggX = at.x(); f.eggY = at.y(); f.eggZ = at.z();
        f.stayWorld = null;
        dirty = true;
    }

    /** L'oeuf est ramasse (casse par son maitre ou un admin) : le familier s'y retire, l'incubation est suspendue. */
    static void pickedUp(World world, Familier f, PlayerRef by) {
        long now = System.currentTimeMillis();
        if (f.incubationEnd > 0) { f.incubationLeft = Math.max(1_000, f.incubationEnd - now); f.incubationEnd = 0; }
        f.eggWorld = null;
        f.stayWorld = null;
        f.waiting = true;
        f.following = false;
        if (f.entity != null) {
            World ew = Universe.get().getWorld(f.entityWorld);
            if (ew != null) ew.execute(() -> despawn(ew, f));
        }
        dirty = true;
        boolean self = by != null && by.getUuid().equals(f.owner);
        tell(f, (self ? "Vous emportez l'œuf de " : "Un administrateur a ramassé l'œuf de ") + f.name
                + " : il s'y retire. Reposez l'œuf pour qu'il revienne.", "#e8c060");
    }

    /**
     * Touche d'interaction sur un oeuf : fenetre du familier (FamilierPage). Oeuf libre : il se lie au joueur. Oeuf d'un autre :
     * refuse, sauf pour un administrateur (outils de test).
     */
    static void useEgg(World world, Ref<EntityStore> playerRef, PlayerRef pr, Vector3i at) {
        Familier f = atEgg(world.getName(), at);
        boolean admin = isAdmin(pr.getUuid());
        if (f == null) {                                                                     // oeuf libre (relache, pose en creatif)
            Espece e = especeOf(WorldBlockUtil.getBlockType(world, at.x(), at.y(), at.z()));
            Familier mine = PETS.get(pr.getUuid());
            if (e == null) return;
            if (mine != null && !admin) { say(pr, refusal(mine), "#e8c060"); return; }
            placed(world, at, pr, e);
            f = PETS.get(pr.getUuid());
            if (f == null) return;
        }
        if (!f.owner.equals(pr.getUuid()) && !admin) {
            say(pr, "Cet œuf de " + f.espece.label + " est lié à " + f.ownerName + ".", "#e8c060");
            return;
        }
        Player player = playerRef.getStore().getComponent(playerRef, Player.getComponentType());
        if (player != null) player.getPageManager().openCustomPage(playerRef, playerRef.getStore(), new FamilierPage(pr, f.owner, admin));
    }

    /** Il suit son maitre / il reste ou il est. Renvoie le message affiche. */
    static String toggleFollow(Familier f, World current) {
        if (!f.hatched()) return "Il n'a pas encore éclos.";
        World world = current != null && current.getName().equals(f.entityWorld) ? current : null;   // thread du monde courant seulement
        Store<EntityStore> store = world != null ? world.getEntityStore().getStore() : null;
        Ref<EntityStore> pet = world != null ? refOf(world, f) : null;
        dirty = true;
        if (f.following || f.waiting) {
            Vector3d p = pet != null ? position(pet, store) : null;
            f.following = false;
            f.waiting = false;
            if (p == null) {
                f.stayWorld = null;                                                          // pas dehors : il reste au nid
            } else {
                f.stayWorld = world.getName();
                f.stayX = p.x; f.stayY = p.y; f.stayZ = p.z;
                FamilierSpells.gesture(pet, "Sit", store);
            }
            return f.name + " reste ici.";
        }
        f.following = true;
        f.stayWorld = null;
        if (pet != null) FamilierSpells.gesture(pet, "Greet", store);
        return f.name + " vous suit.";
    }

    static String refusal(Familier mine) {
        return mine.hasEgg()
                ? "Vous êtes déjà lié à " + mine.name + " (son œuf est en " + mine.eggCoords() + "). Un seul compagnon par maître."
                : "Vous êtes déjà lié à " + mine.name + " (" + mine.espece.label + "). Reposez son œuf, ou relâchez-le depuis son œuf.";
    }

    /** Ligne d'etat (chat, /es familier info) : age, niveau, XP, PV, degats. */
    static String status(Familier f, long now) {
        if (!f.hatched()) return f.espece.label + " · œuf en incubation (" + (f.hasEgg() ? duration(Math.max(0, f.incubationEnd - now)) + " restantes"
                : "suspendue, œuf dans votre sac") + ")";
        Age age = f.age(now);
        return f.name + " · " + f.espece.rarete.label + (f.element != null ? " · " + f.element.label : "") + " · " + age.label
                + " · niveau " + f.level + "/" + f.maxLevel() + " · " + nextEvolution(f, now);
    }

    /** Prochaine evolution : age suivant et delai, evolution a faire, ou niveau maximal. */
    static String nextEvolution(Familier f, long now) {
        if (!f.hatched()) return "Éclosion dans " + duration(Math.max(0, f.incubationEnd - now));
        Age age = f.age(now);
        if (age != Age.ADULTE) {
            Age next = age == Age.JEUNE ? Age.ADOLESCENT : Age.ADULTE;
            long at = (next == Age.ADOLESCENT ? adolescentAt() : adulteAt()) - (now - f.hatchedAt);
            return next.label + " dans " + duration(Math.max(0, at))
                    + (next == Age.ADULTE ? " (il pourra évoluer)" : f.espece.mountable(next) && !f.espece.mountable(age) ? " (pourra vous porter)" : "");
        }
        if (f.skill == null) return "Prêt à évoluer !";
        if (f.level >= f.maxLevel()) return "Accomplie : niveau maximal atteint";
        return "Niveau maximal " + f.maxLevel() + (f.element == Element.GLACE ? " : Porte de Niflheim" : "");
    }

    /** Ages en fraction (0 a 1) vers l'age suivant, pour la barre de croissance. */
    static float growth(Familier f, long now) {
        if (!f.hatched()) return f.hasEgg() ? 1f - Math.max(0, f.incubationEnd - now) / (float) incubationMs() : 0f;
        long lived = now - f.hatchedAt;
        Age age = f.age(now);
        if (age == Age.ADULTE) return 1f;
        long from = age == Age.JEUNE ? 0 : adolescentAt(), to = age == Age.JEUNE ? adolescentAt() : adulteAt();
        return Math.max(0f, Math.min(1f, (lived - from) / (float) (to - from)));
    }

    /** Competence (fenetre) : aucune avant l'age adulte, a choisir par l'evolution, ou celle recue. */
    static String skills(Familier f, long now) {
        if (f.skill != null) return f.skill.label + (f.element == Element.GLACE ? " · Porte de Niflheim"
                + (f.level >= f.maxLevel() ? "" : " (niveau " + f.maxLevel() + ")") : "");
        return f.age(now) == Age.ADULTE ? "Aucune : faites-le évoluer" : "Aucune (évolution à l'âge adulte)";
    }

    /** Competences possibles a l'evolution (fenetre). */
    static String skillPool(Familier f) {
        List<String> names = new ArrayList<>();
        for (Competence c : f.espece.skillPool(f.element)) names.add(c.label);
        return String.join(", ", names);
    }

    /** Peut evoluer : adulte, sans competence. */
    static boolean canEvolve(Familier f, long now) { return f.hatched() && f.skill == null && f.age(now) == Age.ADULTE; }

    /**
     * Evolution (bouton de la fenetre, 2026-11-13) : le familier adulte recoit une competence tiree au hasard parmi celles de
     * son espece et de son element. force (admin) : nouveau tirage meme s'il en a deja une, a tout age.
     */
    static String evolve(UUID owner, boolean force) {
        Familier f = PETS.get(owner);
        long now = System.currentTimeMillis();
        if (f == null || !f.hatched()) return "Il n'a pas encore éclos.";
        if (!force && f.skill != null) return f.name + " a déjà évolué (" + f.skill.label + ").";
        if (!force && f.age(now) != Age.ADULTE) return f.name + " doit d'abord devenir adulte.";
        List<Competence> pool = f.espece.skillPool(f.element);
        if (force && f.skill != null && pool.size() > 1) pool.remove(f.skill);
        f.skill = pool.get(new java.util.Random().nextInt(pool.size()));
        f.cdElement = 0;
        dirty = true;
        tell(f, f.name + " évolue et apprend « " + f.skill.label + " » : " + f.skill.description, f.skill.color);
        return f.name + " apprend « " + f.skill.label + " » !";
    }

    /** Aliments acceptes : part de la vie rendue, part de l'XP du niveau suivant. Vendus par le Dresseur de betes. */
    record Food(String item, String label, float heal, float xp) {}

    static final List<Food> FOODS = List.of(
            new Food("Food_Pie_Meat", "Tourte à la viande", 0.8f, 0.20f),
            new Food("Food_Kebab_Meat", "Brochette de viande", 0.6f, 0.12f),
            new Food("Food_Wildmeat_Cooked", "Gibier rôti", 0.5f, 0.10f),
            new Food("Food_Wildmeat_Raw", "Gibier cru", 0.3f, 0.05f),
            new Food("Food_Beef_Raw", "Bœuf cru", 0.3f, 0.05f),
            new Food("Food_Pork_Raw", "Porc cru", 0.3f, 0.05f),
            new Food("Food_Chicken_Raw", "Poulet cru", 0.25f, 0.04f),
            new Food("Food_Fish_Raw", "Poisson cru", 0.25f, 0.04f));

    /** Aliment le plus nourrissant present dans le sac (null : aucun). */
    static Food bestFood(Player player) {
        if (player == null) return null;
        for (Food food : FOODS)
            if (com.eldanior.system.quest.QuestManager.countItemInInventory(player, food.item()) > 0) return food;
        return null;
    }

    /** Nourrir son familier (fenetre) : un aliment du sac, vie et un peu d'XP, une fois par minute. */
    static String feed(UUID owner, Player player, World current) {
        Familier f = PETS.get(owner);
        long now = System.currentTimeMillis();
        if (f == null || !f.hatched()) return "Il n'a pas encore éclos.";
        if (now - f.fedAt < FEED_CD) return f.name + " n'a plus faim (encore " + duration(FEED_CD - (now - f.fedAt)) + ").";
        Food food = bestFood(player);
        if (food == null) return "Vous n'avez rien à lui donner : de la viande ou du poisson (le Dresseur de bêtes en vend).";
        if (!com.eldanior.system.quest.QuestManager.removeItemsFromInventory(player, food.item(), 1)) return "Impossible de prendre l'aliment.";
        f.fedAt = now;
        World w = f.entityWorld != null && current != null && current.getName().equals(f.entityWorld) ? current : null;
        Ref<EntityStore> pet = w != null ? refOf(w, f) : null;
        if (pet != null) {
            Store<EntityStore> store = w.getEntityStore().getStore();
            EntityStatMap stats = store.getComponent(pet, EntityStatsModule.get().getEntityStatMapComponentType());
            int health = DefaultEntityStatTypes.getHealth();
            EntityStatValue hp = stats != null ? stats.get(health) : null;
            if (hp != null) stats.setStatValue(health, Math.min(hp.getMax(), hp.get() + hp.getMax() * food.heal()));
            f.hpRatio = hpRatio(pet, store);
            FamilierSpells.gesture(pet, "Eat", store);
        } else {
            f.hpRatio = Math.min(1f, f.hpRatio + food.heal());
        }
        gainXp(f, Math.max(1, Math.round(f.xpToNext() * food.xp())));
        dirty = true;
        return f.name + " dévore : " + food.label().toLowerCase() + ". +" + Math.round(food.heal() * 100) + " % de vie.";
    }

    /** Vie actuelle (fraction) : entite si elle est dehors dans ce monde, sinon la derniere connue. */
    static float hp(Familier f, World current) {
        World w = f.entityWorld != null && current != null && current.getName().equals(f.entityWorld) ? current : null;
        Ref<EntityStore> pet = w != null ? refOf(w, f) : null;
        return pet != null ? hpRatio(pet, w.getEntityStore().getStore()) : f.hpRatio;
    }

    static String capital(String s) { return s == null || s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    /** Ou il est (fenetre) : suit, attend au nid, reste, oeuf dans le sac. */
    static String whereabouts(Familier f, long now) {
        if (!f.hatched()) return f.hasEgg() ? "Œuf en incubation" : "Œuf dans votre sac (incubation suspendue)";
        if (!f.hasEgg()) return "Retiré dans son œuf (dans votre sac)";
        if (f.waiting) return "Vous attend au nid";
        return f.following ? "Vous suit" : "Reste à sa place";
    }

    // ===== Outils d'administration (fenetre de l'oeuf) =====

    /** Age suivant (Jeune -> Adolescent -> Adulte -> Jeune). */
    static String cycleAge(UUID owner) {
        Familier f = PETS.get(owner);
        if (f == null || !f.hatched()) return "Pas encore éclos.";
        Age next = Age.values()[(f.age(System.currentTimeMillis()).ordinal() + 1) % Age.values().length];
        setAge(owner, next);
        return "Âge : " + next.label + ".";
    }

    static String addLevels(UUID owner, int delta) {
        Familier f = PETS.get(owner);
        if (f == null) return "Pas de familier.";
        setLevel(owner, delta == Integer.MAX_VALUE ? f.maxLevel() : f.level + delta);
        return "Niveau " + f.level + ".";
    }

    /** Element suivant (y compris ceux des autres raretes, pour tester leurs competences). */
    static String cycleElement(UUID owner) {
        Familier f = PETS.get(owner);
        if (f == null) return "Pas de familier.";
        Element[] all = Element.values();
        Element next = all[f.element == null ? 0 : (f.element.ordinal() + 1) % all.length];
        setElement(f, next);
        return "Élément : " + next.label + " (" + next.skill + ").";
    }

    static String healPet(UUID owner, World current) {
        Familier f = PETS.get(owner);
        World w = f != null && current != null && current.getName().equals(f.entityWorld) ? current : null;
        Ref<EntityStore> pet = w != null ? refOf(w, f) : null;
        if (pet == null) return "Le familier n'est pas dehors.";
        EntityStatMap stats = w.getEntityStore().getStore().getComponent(pet, EntityStatsModule.get().getEntityStatMapComponentType());
        if (stats != null) stats.maximizeStatValue(DefaultEntityStatTypes.getHealth());
        f.cdElement = f.cdGate = 0;
        return "Vie et recharges remises à neuf.";
    }

    /** Bloc de l'oeuf : ouvert quand le familier est eclos, ferme sinon. Sans effet si le chunk n'est pas charge. */
    private static void syncEgg(World world, Familier f) {
        if (!world.getName().equals(f.eggWorld)) return;
        BlockType type = WorldBlockUtil.getBlockType(world, f.eggX, f.eggY, f.eggZ);
        if (type == null) return;                                                            // chunk decharge
        if (!isEgg(type)) {                                                                  // bloc disparu (explosion...) : rendu au maitre
            lostEgg(f);
            return;
        }
        BlockType base = BlockType.getAssetMap().getAsset(f.espece.eggItem);
        BlockType open = base != null ? base.getBlockForState(STATE_OPEN) : null;
        if (open == null) return;
        boolean want = f.hatched(), is = open.getId().equals(type.getId());
        if (want == is) return;
        ChunkStore chunks = world.getChunkStore();
        Ref<ChunkStore> section = chunks.getChunkSectionReferenceAtBlock(f.eggX, f.eggY, f.eggZ);
        if (section == null || !section.isValid()) return;
        BlockOperations.setBlockInteractionState(chunks, section, f.eggX, f.eggY, f.eggZ, type, want ? STATE_OPEN : STATE_CLOSED, false);
    }

    /** L'oeuf a disparu sans passer par un joueur : le familier s'y retire et l'oeuf revient dans le sac du maitre (connecte). */
    private static void lostEgg(Familier f) {
        pickedUp(null, f, null);
        PlayerRef pr = Universe.get().getPlayer(f.owner);
        Ref<EntityStore> ref = pr != null ? pr.getReference() : null;
        Player player = ref != null && ref.isValid() ? ref.getStore().getComponent(ref, Player.getComponentType()) : null;
        ItemStack egg = new ItemStack(f.espece.eggItem, 1);
        if (player != null && player.getInventory().getCombinedHotbarFirst().canAddItemStack(egg)) {
            player.getInventory().getCombinedHotbarFirst().addItemStack(egg);
            tell(f, "L'œuf de " + f.name + " a été arraché du sol : il est revenu dans votre sac.", "#e8c060");
        } else {
            tell(f, "L'œuf de " + f.name + " a disparu. Un œuf de " + f.espece.label + " posé par vous le fera revenir.", "#e8c060");
        }
    }

    private static void hatch(Familier f, long now) {
        f.incubationEnd = 0;
        f.hatchedAt = now;
        if (f.element == null) f.element = f.espece.rollElement(new java.util.Random());
        f.waiting = true;
        f.following = false;
        dirty = true;
        tell(f, "Votre œuf de " + f.espece.label + " a éclos ! " + capital(f.espece.young) + " vous attend au nid (" + f.eggCoords() + ").", "#9fdcff");
        if (f.element != null)
            tell(f, "Il est né sous l'élément " + f.element.label + ". Adulte, vous pourrez le faire évoluer pour qu'il apprenne une compétence.",
                    f.element.color);
    }

    // =====================================================================================================================
    // Entite : apparition, stats, croissance, retrait
    // =====================================================================================================================

    private static Ref<EntityStore> refOf(World world, Familier f) {
        UUID id = f.entity;
        if (id == null || !world.getName().equals(f.entityWorld)) return null;
        Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(id);
        if (ref == null || !ref.isValid()) { forget(f); return null; }                     // retire par le moteur (chunk decharge...)
        return ref;
    }

    private static Ref<EntityStore> spawn(World world, Familier f, Vector3d at, Store<EntityStore> store) {
        Age age = f.age(System.currentTimeMillis());
        try {
            var pair = NPCPlugin.get().spawnNPC(store, f.espece.role(age), null, new Vector3d(at), new Rotation3f());
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) { EldaniorLogger.warn("[Familiers] apparition ratee : " + f.espece.role(age)); return null; }
            store.ensureComponent(ref, store.getRegistry().getNonSerializedComponentType());
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            if (u == null) { store.removeEntity(ref, RemoveReason.REMOVE); return null; }
            f.entity = u.getUuid();
            f.entityWorld = world.getName();
            f.spawnedAge = age;
            ENTITIES.put(f.entity, f.owner);
            applyStats(f, ref, store, true);
            return ref;
        } catch (Exception e) {
            EldaniorLogger.error("[Familiers] apparition de " + f.name, e);
            return null;
        }
    }

    private static void despawn(World world, Familier f) {
        UUID id = f.entity;
        if (id == null) return;
        Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(id);
        if (ref != null && ref.isValid()) {
            f.hpRatio = hpRatio(ref, world.getEntityStore().getStore());
            world.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
        }
        forget(f);
    }

    private static void forget(Familier f) {
        UUID id = f.entity;
        if (id != null) ENTITIES.remove(id);
        f.entity = null;
        f.entityWorld = null;
        f.spawnedAge = null;
        f.appliedLevel = -1;
    }

    /** Vie maximale (niveau et age), plaque de nom ; fresh : vie a hpRatio du maximum (apparition). */
    private static void applyStats(Familier f, Ref<EntityStore> ref, Store<EntityStore> store, boolean fresh) {
        Age age = f.spawnedAge != null ? f.spawnedAge : f.age(System.currentTimeMillis());
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        int health = DefaultEntityStatTypes.getHealth();
        EntityStatValue hp = stats != null ? stats.get(health) : null;
        if (hp != null) {
            float ratio = fresh ? f.hpRatio : (hp.getMax() > 0 ? hp.get() / hp.getMax() : 1f);
            stats.removeModifier(health, HP_MODIFIER);
            float base = stats.get(health).getMax(), want = f.maxHp(age);
            stats.putModifier(health, HP_MODIFIER, new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, want - base));
            stats.setStatValue(health, Math.max(1f, want * Math.max(0.05f, Math.min(1f, ratio))));
        }
        store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(f.name + (f.element != null ? " [" + f.element.label + "]" : "") + " · Nv " + f.level + " · " + f.ownerName));
        // (la competence n'est pas sur la plaque : elle se lit dans la fenetre de l'oeuf)
        f.appliedLevel = f.level;
    }

    private static void grow(World world, Familier f, Ref<EntityStore> pet, Vector3d at, Age age, Store<EntityStore> store) {
        f.hpRatio = hpRatio(pet, store);
        despawn(world, f);
        Ref<EntityStore> grown = spawn(world, f, at, store);
        if (grown == null) return;
        FamilierSpells.bloom(at, age == Age.ADULTE ? 1.8f : 1.2f, store);
        FamilierSpells.gesture(grown, "Howl", store);
        String mount = f.espece.mountable(age) && (age == Age.JEUNE || !f.espece.mountable(Age.values()[age.ordinal() - 1]))
                ? " Il peut désormais vous porter (touche d'interaction sur lui)." : "";
        if (age == Age.ADOLESCENT)
            tell(f, f.name + " grandit : il est désormais Adolescent !" + mount, "#9fdcff");
        else if (age == Age.ADULTE)
            tell(f, (f.espece == Espece.FENRIR ? "Le Fenrir légendaire s'éveille ! " : "") + f.name + " est adulte !" + mount
                    + " Faites-le évoluer depuis son œuf pour qu'il apprenne une compétence.", "#9fdcff");
    }

    private static void regen(Ref<EntityStore> pet, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(pet, EntityStatsModule.get().getEntityStatMapComponentType());
        int health = DefaultEntityStatTypes.getHealth();
        EntityStatValue hp = stats != null ? stats.get(health) : null;
        if (hp != null && hp.get() < hp.getMax()) stats.setStatValue(health, Math.min(hp.getMax(), hp.get() + hp.getMax() * REGEN_PER_SECOND));
    }

    private static float hpRatio(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        EntityStatValue hp = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
        return hp != null && hp.getMax() > 0 ? hp.get() / hp.getMax() : 1f;
    }

    /**
     * Chevaucher (2026-11-13, choix sur l'animal : FamilierChoicePage) : reprend l'action Mount des roles vanilla
     * (builtin.mounts.npc.ActionMount) : composant de monture (maitre, selle de l'espece a cet age), role vide pendant la monte
     * (le role d'origine revient a la descente), deplacements du cavalier en mode monture. Renvoie le refus, ou null.
     */
    static String mount(UUID owner, Ref<EntityStore> pet, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        Familier f = PETS.get(owner);
        if (f == null || pet == null || !pet.isValid() || pet.getStore() != store) return "Il n'est plus là.";
        Age age = f.spawnedAge != null ? f.spawnedAge : f.age(System.currentTimeMillis());
        if (!f.espece.mountable(age)) return f.name + " est encore trop jeune pour vous porter.";
        if (store.getComponent(pet, NPCMountComponent.getComponentType()) != null) return f.name + " porte déjà quelqu'un.";
        NPCEntity npc = store.getComponent(pet, NPCEntity.getComponentType());
        Player player = store.getComponent(playerRef, Player.getComponentType());
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (npc == null || npc.getRole() == null || player == null || pr == null) return "Impossible de monter maintenant.";
        NPCMountComponent m = store.ensureAndGetComponent(pet, NPCMountComponent.getComponentType());
        m.setOriginalRoleIndex(NPCPlugin.get().getIndex(npc.getRoleName()));
        m.setOwnerPlayerRef(pr);
        m.setAnchor(0f, f.espece.saddle[age.ordinal()], 0f);
        com.hypixel.hytale.server.npc.systems.RoleChangeSystem.requestRoleChange(pet, npc.getRole(),
                NPCPlugin.get().getIndex("Empty_Role"), false, null, null, store);
        var cfg = com.hypixel.hytale.server.core.entity.entities.player.movement.MovementConfig.getAssetMap().getAsset("Mount");
        var mm = store.getComponent(playerRef, com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager.getComponentType());
        var pv = store.getComponent(playerRef, com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues.getComponentType());
        if (cfg != null && mm != null) {
            mm.setDefaultSettings(cfg, pv, player.getGameMode());
            mm.applyDefaultSettings();
            mm.update(pr.getPacketHandler());
        }
        f.sitting = false;
        return null;
    }

    /** Monte par un autre que son maitre (ne devrait pas arriver : attitude) : le cavalier est desarconne. */
    private static void checkRider(Familier f, NPCMountComponent mount, Store<EntityStore> store) {
        PlayerRef rider = mount.getOwnerPlayerRef();
        if (rider == null || rider.getUuid().equals(f.owner)) return;
        Ref<EntityStore> r = rider.getReference();
        Player p = r != null && r.isValid() ? store.getComponent(r, Player.getComponentType()) : null;
        if (p != null) MountPlugin.checkDismountNpc(store, r, p);
        say(rider, f.name + " n'obéit qu'à " + f.ownerName + ".", "#e8c060");
    }

    // =====================================================================================================================
    // Combat
    // =====================================================================================================================

    /** Monstre le plus proche du point garde : le familier l'attaque s'il est au repos. */
    private static void guard(Familier f, Ref<EntityStore> pet, Vector3d center, Store<EntityStore> store) {
        StateSupport st = StateSupport.get(pet, store);
        String state = st != null ? st.getStateName() : null;
        if (state == null || !state.startsWith("Idle")) return;
        Ref<EntityStore> best = null;
        double bestD = Double.MAX_VALUE;
        for (Ref<EntityStore> e : FamilierSpells.monstersAround(center, DANGER_RANGE, store)) {
            if (isPet(e, store)) continue;
            Vector3d p = position(e, store);
            if (p == null) continue;
            double d = p.distanceSquared(center);
            if (d < bestD) { bestD = d; best = e; }
        }
        if (best != null) engage(f, pet, best, store);
    }

    private static void engage(Familier f, Ref<EntityStore> pet, Ref<EntityStore> enemy, ComponentAccessor<EntityStore> store) {
        try {
            NpcAttitudes.hostile(pet, enemy, FIGHT_SECONDS, store);
            StateSupport st = StateSupport.get(pet, store);
            String state = st != null ? st.getStateName() : null;
            if (state == null || !state.startsWith("Idle")) return;
            MarkedEntitySupport marks = MarkedEntitySupport.get(pet, store);
            if (marks != null) marks.setMarkedEntity(MarkedEntitySupport.DEFAULT_TARGET_SLOT, enemy);
            st.setState(pet, "Alerted", null, store);
            f.lastTarget = enemy;
            f.lastCombat = System.currentTimeMillis();
        } catch (Exception e) {
            EldaniorLogger.error("[Familiers] attaque", e);
        }
    }

    private static void abilities(Familier f, Ref<EntityStore> pet, Vector3d at, Age age, long now, Store<EntityStore> store) {
        float dmg = f.damage(age);
        Ref<EntityStore> target = f.lastTarget;
        boolean near = target != null && target.isValid() && store.getComponent(target, DeathComponent.getComponentType()) == null;
        Vector3d tp = near ? position(target, store) : null;
        if (tp == null || tp.distance(at) > 10) return;
        if (f.skill != null && age == Age.ADULTE && now >= f.cdElement && tp.distance(at) <= 7) {   // competence d'evolution
            PlayerRef pr = Universe.get().getPlayer(f.owner);
            Ref<EntityStore> owner = pr != null ? pr.getReference() : null;
            if (FamilierSpells.skill(f.skill.name(), pet, target, owner, dmg, store) > 0) {
                f.cdElement = now + FamilierSpells.cooldown(f.skill.name());
                return;
            }
        }
        if (f.element == Element.GLACE && f.level >= f.maxLevel() && now >= f.cdGate) {          // competence divine du Fenrir
            PlayerRef pr = Universe.get().getPlayer(f.owner);
            Ref<EntityStore> owner = pr != null ? pr.getReference() : null;
            if (owner != null && owner.isValid() && owner.getStore() == store) {
                FamilierSpells.niflheimGate(owner, at, store);
                f.cdGate = now + GATE_CD;
                tell(f, f.name + " ouvre la Porte de Niflheim !", "#9fe8ff");
                return;
            }
        }
    }

    /** Appele par GuardAlertSystem : ce que le maitre frappe, et ce qui frappe le maitre, devient la cible du familier. */
    public static void onOwnerFight(Ref<EntityStore> player, Ref<EntityStore> enemy, ComponentAccessor<EntityStore> store) {
        if (ENTITIES.isEmpty() || enemy == null || !enemy.isValid()) return;
        if (store.getComponent(enemy, Player.getComponentType()) != null || store.getComponent(enemy, NPCEntity.getComponentType()) == null) return;
        UUIDComponent u = store.getComponent(player, UUIDComponent.getComponentType());
        Familier f = u != null ? PETS.get(u.getUuid()) : null;
        if (f == null || f.entity == null || isPet(enemy, store)) return;
        NPCEntity n = store.getComponent(enemy, NPCEntity.getComponentType());
        if (n != null && com.eldanior.system.guards.GuardType.isAlly(n.getRoleName())) return;      // garde, villageois...
        World world = store.getExternalData().getWorld();
        Ref<EntityStore> pet = world != null ? refOf(world, f) : null;
        if (pet != null) engage(f, pet, enemy, store);
    }

    public static boolean isPet(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) { return petOf(ref, store) != null; }

    public static Familier petOf(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (ENTITIES.isEmpty() || ref == null || !ref.isValid()) return null;
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        UUID owner = u != null ? ENTITIES.get(u.getUuid()) : null;
        return owner != null ? PETS.get(owner) : null;
    }

    /** Maitre du familier (UUID d'entite), null si ce n'en est pas un : DeathXPSystem. */
    public static UUID ownerOfPet(UUID entity) { return entity != null ? ENTITIES.get(entity) : null; }

    /**
     * Appele par DivineDamageSystem. True : coup annule. Les joueurs ne blessent pas les familiers (et inversement), les
     * familiers ne se blessent pas entre eux. Coup mortel sur un familier : il meurt (die). Coup porte : XP.
     */
    public static boolean onDamage(Ref<EntityStore> attacker, Ref<EntityStore> target, Damage damage, Store<EntityStore> store,
                                   CommandBuffer<EntityStore> cb) {
        if (ENTITIES.isEmpty()) return false;
        Familier ft = petOf(target, store), fa = attacker != null ? petOf(attacker, store) : null;
        long now = System.currentTimeMillis();
        if (ft != null) {
            if (fa != null || (attacker != null && store.getComponent(attacker, Player.getComponentType()) != null)) return true;
            ft.lastCombat = now;
            if (attacker != null && attacker.isValid()) ft.lastTarget = attacker;
            EntityStatMap stats = store.getComponent(target, EntityStatsModule.get().getEntityStatMapComponentType());
            EntityStatValue hp = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
            if (hp != null && hp.get() - damage.getAmount() <= 1f) {
                World world = store.getExternalData().getWorld();
                die(world, ft, target, store, cb);
                return true;
            }
            return false;
        }
        if (fa != null) {
            if (store.getComponent(target, Player.getComponentType()) != null) return true;
            fa.lastCombat = now;
            fa.lastTarget = target;
            gainXp(fa, Math.max(1, Math.round(damage.getAmount() * 0.3f)));
        }
        return false;
    }

    /** Appele par MobDamageReductionSystem : degats de la morsure (cause physique) selon le niveau et l'age, sinon montant. */
    public static float biteDamage(Familier f, float amount, String cause) {
        if (cause == null || !cause.equals("Physical")) return amount;
        Age age = f.spawnedAge != null ? f.spawnedAge : f.age(System.currentTimeMillis());
        return f.damage(age) * (0.9f + (float) Math.random() * 0.2f);
    }

    /** Victoire du familier (DeathXPSystem) : XP selon le niveau du monstre. */
    public static void onKill(UUID petEntity, int victimLevel) {
        UUID owner = ownerOfPet(petEntity);
        Familier f = owner != null ? PETS.get(owner) : null;
        if (f != null) gainXp(f, 15 + 2L * Math.max(1, victimLevel));
    }

    static void gainXp(Familier f, long amount) {
        if (f.level >= f.maxLevel() || amount <= 0) return;
        f.xp += amount;
        boolean up = false;
        while (f.level < f.maxLevel() && f.xp >= f.xpToNext()) { f.xp -= f.xpToNext(); f.level++; up = true; }
        if (f.level >= f.maxLevel()) f.xp = 0;
        if (up) tell(f, f.name + " passe au niveau " + f.level + " !", "#9fdcff");
        dirty = true;
    }

    /**
     * Mort du familier (2026-11-13) : pas de retour. Il se dissout, son oeuf vole en eclats, le lien est rompu et le maitre
     * subit -20 % d'experience pendant 7 jours. cb non nul : appele pendant le traitement des degats (retrait differe).
     */
    private static void die(World world, Familier f, Ref<EntityStore> pet, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (PETS.get(f.owner) != f) return;
        Vector3d at = position(pet, store);
        if (at != null) FamilierSpells.shatter(at, cb != null ? cb : store, store);
        if (cb != null) cb.removeEntity(pet, RemoveReason.REMOVE);
        else if (store.getComponent(pet, DeathComponent.getComponentType()) == null) store.removeEntity(pet, RemoveReason.REMOVE);
        forget(f);
        PETS.remove(f.owner, f);
        boolean admin = isAdmin(f.owner);
        if (!admin) MALUS.put(f.owner, System.currentTimeMillis() + MALUS_MS);
        if (f.hasEgg()) {
            World eggWorld = Universe.get().getWorld(f.eggWorld);
            int x = f.eggX, y = f.eggY, z = f.eggZ;
            if (eggWorld != null) eggWorld.execute(() -> {
                if (!isEgg(WorldBlockUtil.getBlockType(eggWorld, x, y, z))) return;
                WorldBlockUtil.setBlock(eggWorld, x, y, z, "Empty");
                FamilierSpells.shatter(new Vector3d(x + 0.5, y, z + 0.5), eggWorld.getEntityStore().getStore(), eggWorld.getEntityStore().getStore());
            });
        }
        tell(f, f.name + " est mort au combat. Son œuf s'est brisé et votre lien est rompu.", "#ff7a6a");
        tell(f, admin ? "Administrateur : pas de malus de deuil." : "Le deuil vous pèse : -20 % d'expérience pendant 7 jours.", "#e8a060");
        EldaniorLogger.info("[Familiers] " + f.name + " (niveau " + f.level + ") de " + f.ownerName + " est mort.");
        save();
    }

    /** Relacher son familier : le lien est rompu sans malus ; l'oeuf reste au sol, libre. */
    static boolean release(UUID owner) {
        Familier f = PETS.remove(owner);
        if (f == null) return false;
        if (f.entity != null) {
            World ew = Universe.get().getWorld(f.entityWorld);
            if (ew != null) ew.execute(() -> despawn(ew, f));
        }
        if (f.hasEgg()) {
            World w = Universe.get().getWorld(f.eggWorld);
            if (w != null) w.execute(() -> {
                f.hatchedAt = 0;                                                              // oeuf libre : ferme
                f.eggWorld = w.getName();
                syncEggClosed(w, f);
            });
        }
        save();
        return true;
    }

    private static void syncEggClosed(World world, Familier f) {
        BlockType type = WorldBlockUtil.getBlockType(world, f.eggX, f.eggY, f.eggZ);
        if (!isEgg(type)) return;
        ChunkStore chunks = world.getChunkStore();
        Ref<ChunkStore> section = chunks.getChunkSectionReferenceAtBlock(f.eggX, f.eggY, f.eggZ);
        if (section != null && section.isValid())
            BlockOperations.setBlockInteractionState(chunks, section, f.eggX, f.eggY, f.eggZ, type, STATE_CLOSED, false);
    }

    // ===== Commandes de test (admin) =====

    /** Fin immediate de l'incubation. */
    static boolean hatchNow(UUID owner) {
        Familier f = PETS.get(owner);
        if (f == null || f.hatched()) return false;
        if (f.incubationEnd > 0) f.incubationEnd = System.currentTimeMillis();
        else { f.incubationLeft = 1_000; }
        return true;
    }

    static boolean setAge(UUID owner, Age age) {
        Familier f = PETS.get(owner);
        if (f == null || !f.hatched()) return false;
        long now = System.currentTimeMillis();
        f.hatchedAt = now - (age == Age.ADULTE ? adulteAt() : age == Age.ADOLESCENT ? adolescentAt() : 0) - 1_000;
        dirty = true;
        return true;
    }

    static void setElement(Familier f, Element e) {
        f.element = e;
        f.appliedLevel = -1;                                                                 // plaque de nom a refaire
        f.cdElement = 0;
        dirty = true;
    }

    static boolean setLevel(UUID owner, int level) {
        Familier f = PETS.get(owner);
        if (f == null) return false;
        f.level = Math.max(1, Math.min(f.maxLevel(), level));
        f.xp = 0;
        dirty = true;
        return true;
    }

    // =====================================================================================================================
    // Outils
    // =====================================================================================================================

    private static Vector3d behind(Ref<EntityStore> owner, Vector3d op, Store<EntityStore> store) {
        TransformComponent t = store.getComponent(owner, TransformComponent.getComponentType());
        float yaw = t != null ? t.getRotation().yaw() : 0f;
        double fx = PhysicsMath.headingX(yaw), fz = PhysicsMath.headingZ(yaw);
        return new Vector3d(op.x - fx * FOLLOW_BEHIND + fz * FOLLOW_SIDE, op.y, op.z - fz * FOLLOW_BEHIND - fx * FOLLOW_SIDE);
    }

    private static Vector3d position(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        if (ref == null || !ref.isValid()) return null;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        return t != null ? new Vector3d(t.getPosition()) : null;
    }

    static String duration(long ms) {
        long s = Math.max(0, ms / 1000);
        if (s >= 86_400) return (s / 86_400) + " j " + (s % 86_400) / 3600 + " h";
        if (s >= 3600) return (s / 3600) + " h " + String.format("%02d", (s % 3600) / 60) + " min";
        return (s / 60) + " min " + String.format("%02d", s % 60) + " s";
    }

    private static void tell(Familier f, String text, String color) {
        PlayerRef pr = Universe.get().getPlayer(f.owner);
        if (pr != null) say(pr, text, color);
    }

    static void say(PlayerRef pr, String text, String color) {
        if (pr != null) pr.sendMessage(Message.raw("[" + "Familier" + "] ").color("#9fdcff").insert(Message.raw(text).color(color)));
    }

    // =====================================================================================================================
    // Familiers orphelins : entites de role Eldanior_Familier_* qui ne sont le familier de personne (sauvegarde ratee)
    // =====================================================================================================================

    public static final class Orphans extends EntityTickingSystem<EntityStore> {
        private final Set<UUID> checked = ConcurrentHashMap.newKeySet();

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                         @Nonnull CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            if (ref == null || !ref.isValid()) return;
            NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
            if (npc == null || !isPetRole(npc.getRoleName())) return;
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            if (u == null || !checked.add(u.getUuid()) || ENTITIES.containsKey(u.getUuid())) return;
            cb.removeEntity(ref, RemoveReason.REMOVE);
            EldaniorLogger.info("[Familiers] familier orphelin retire (" + npc.getRoleName() + ")");
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() { return NPCEntity.getComponentType(); }
    }

    // =====================================================================================================================
    // Sauvegarde
    // =====================================================================================================================

    private static synchronized void save() {
        if (file == null) return;
        dirty = false;
        BsonArray all = new BsonArray();
        for (Familier f : new ArrayList<>(PETS.values())) {
            BsonDocument d = new BsonDocument();
            d.put("Owner", new BsonString(f.owner.toString()));
            d.put("OwnerName", new BsonString(f.ownerName != null ? f.ownerName : ""));
            d.put("Espece", new BsonString(f.espece.name()));
            d.put("Name", new BsonString(f.name));
            d.put("Level", new BsonInt32(f.level));
            d.put("Xp", new BsonInt64(f.xp));
            d.put("Element", new BsonString(f.element != null ? f.element.name() : ""));
            d.put("Skill", new BsonString(f.skill != null ? f.skill.name() : ""));
            d.put("IncubationEnd", new BsonInt64(f.incubationEnd));
            d.put("IncubationLeft", new BsonInt64(f.incubationLeft));
            d.put("HatchedAt", new BsonInt64(f.hatchedAt));
            d.put("EggWorld", new BsonString(f.eggWorld != null ? f.eggWorld : ""));
            d.put("EggX", new BsonInt32(f.eggX));
            d.put("EggY", new BsonInt32(f.eggY));
            d.put("EggZ", new BsonInt32(f.eggZ));
            d.put("Following", new BsonBoolean(f.following));
            d.put("Waiting", new BsonBoolean(f.waiting));
            d.put("StayWorld", new BsonString(f.stayWorld != null ? f.stayWorld : ""));
            d.put("StayX", new BsonDouble(f.stayX));
            d.put("StayY", new BsonDouble(f.stayY));
            d.put("StayZ", new BsonDouble(f.stayZ));
            d.put("HpRatio", new BsonDouble(f.hpRatio));
            all.add(d);
        }
        BsonArray malus = new BsonArray();
        MALUS.forEach((u, until) -> malus.add(new BsonDocument("Player", new BsonString(u.toString())).append("Until", new BsonInt64(until))));
        BsonDocument root = new BsonDocument("Familiers", all).append("Malus", malus);
        try {
            Files.createDirectories(file.getParent());
            BsonUtil.writeDocument(file, root).join();
        } catch (Exception e) {
            EldaniorLogger.error("[Familiers] sauvegarde", e);
        }
    }

    private static void load() {
        if (!Files.exists(file)) return;
        try {
            BsonDocument root = BsonUtil.readDocumentNow(file);
            if (root == null) return;
            if (root.containsKey("Familiers")) for (BsonValue v : root.getArray("Familiers")) {
                BsonDocument d = v.asDocument();
                Espece e = Espece.of(d.getString("Espece").getValue());
                if (e == null) continue;
                Familier f = new Familier(UUID.fromString(d.getString("Owner").getValue()), d.getString("OwnerName").getValue(), e);
                f.name = d.getString("Name").getValue();
                f.level = d.getInt32("Level").getValue();
                f.xp = d.getInt64("Xp").getValue();
                if (d.containsKey("Element")) f.element = Element.of(d.getString("Element").getValue());
                if (d.containsKey("Skill")) f.skill = Competence.of(d.getString("Skill").getValue());
                f.incubationEnd = d.getInt64("IncubationEnd").getValue();
                f.incubationLeft = d.getInt64("IncubationLeft").getValue();
                f.hatchedAt = d.getInt64("HatchedAt").getValue();
                String ew = d.getString("EggWorld").getValue();
                f.eggWorld = ew.isEmpty() ? null : ew;
                f.eggX = d.getInt32("EggX").getValue();
                f.eggY = d.getInt32("EggY").getValue();
                f.eggZ = d.getInt32("EggZ").getValue();
                f.following = d.getBoolean("Following").getValue();
                f.waiting = d.getBoolean("Waiting").getValue();
                String sw = d.getString("StayWorld").getValue();
                f.stayWorld = sw.isEmpty() ? null : sw;
                f.stayX = d.getDouble("StayX").getValue();
                f.stayY = d.getDouble("StayY").getValue();
                f.stayZ = d.getDouble("StayZ").getValue();
                f.hpRatio = (float) d.getDouble("HpRatio").getValue();
                PETS.put(f.owner, f);
            }
            if (root.containsKey("Malus")) for (BsonValue v : root.getArray("Malus")) {
                BsonDocument d = v.asDocument();
                MALUS.put(UUID.fromString(d.getString("Player").getValue()), d.getInt64("Until").getValue());
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Familiers] chargement", e);
        }
    }
}
