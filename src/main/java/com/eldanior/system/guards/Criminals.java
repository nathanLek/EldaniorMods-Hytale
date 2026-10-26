package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * Criminel (GuardType.CRIMINEL) : hors-la-loi pose comme un aventurier (types guerrier, archer, assassin ; equipement selon
 * son rang), qui rode dans {@link #ROAM_RADIUS} blocs autour de son repaire et attaque les joueurs a moins de
 * {@link #AGGRO_RANGE} blocs (cible forcee par le code, comme les gardes : aggress). Il epargne les PK (criminels comme lui),
 * sauf s'ils frappent sa bande (provoke, {@link #PROVOKE_MS}). Selon son rang, des complices ({@link #ALLIES_BY_RANK}, au
 * niveau x {@link #ALLY_LEVEL}) l'accompagnent : ils le suivent, se battent comme lui, n'ont pas de prime et sont remplaces
 * a chaque reapparition du chef. Gardes, chasseurs de
 * prime et aventuriers le combattent (groupe Eldanior_Criminals hostile pour eux) ; le frapper n'est pas un crime.
 * Sa prime (niveau x {@link #BOUNTY_PER_LEVEL}, plus ce que les joueurs y ajoutent au tableau) va a celui qui l'abat ; il
 * reparait a son repaire apres {@link #RESPAWN_MS}, prime remise a sa base. Il figure au tableau de prime.
 */
public final class Criminals {

    private Criminals() {}

    static final long RESPAWN_MS = 30 * 60_000;
    static final double ROAM_RADIUS = 40.0, AGGRO_RANGE = 20.0, ALLY_LEVEL = 0.7;
    /** Complices par rang du chef : E, D, C, B, A, S. */
    static final int[] ALLIES_BY_RANK = {1, 2, 3, 5, 7, 10};
    static final long PROVOKE_MS = 30_000;
    private static final double HOSTILE_SECONDS = 10.0;
    /** Joueurs qui ont frappe une bande (cle : poste du chef) et jusqu'a quand elle leur en veut. */
    private static final java.util.Map<String, java.util.Map<UUID, Long>> provoked = new java.util.concurrent.ConcurrentHashMap<>();
    /** Complice (UUID du PNJ) -> son poste (non enregistre : graine, niveau, chef). */
    private static final java.util.Map<UUID, GuardManager.Post> allies = new java.util.concurrent.ConcurrentHashMap<>();
    static final long BOUNTY_PER_LEVEL = 25;

    private static final String[] EPITHETS = {"le Balafré", "l'Égorgeur", "le Borgne", "Main-Rouge", "Sans-Pitié", "le Corbeau",
            "Lame-Noire", "le Chacal", "Croc-d'Acier", "le Pendu", "Mille-Poches", "le Fourbe", "Brise-Os", "le Vautour"};

    /** Criminel du tableau de prime. alive : en liberte ; sinon de retour a respawnAt. */
    public record Wanted(String postId, String name, int level, long bounty, boolean alive, long respawnAt,
                         String world, Vector3d lair) {}

    /** Role selon son type (guerrier par defaut). */
    static String role(GuardManager.Post p) {
        String base = GuardType.CRIMINEL.getRoleId();
        return switch (AdventurerType.of(p.skinSeed)) {
            case ARCHER -> base + "_Archer";
            case ASSASSIN -> base + "_Assassin";
            default -> base;
        };
    }

    /** Surnom : prenom de son poste et epithete (tous deux tires de sa graine). */
    static String name(GuardManager.Post p) {
        return GuardManager.firstName(p) + " " + EPITHETS[(int) Math.floorMod(p.skinSeed / 7, (long) EPITHETS.length)];
    }

    static String nameplate(GuardManager.Post p) {
        GuardManager.Post chief = p.leader != null ? GuardManager.post(p.leader) : null;
        if (p.leader != null) return "Complice" + (chief != null ? " de " + name(chief) : "") + " [" + getAdventureRank(p.level) + "]";
        return name(p) + " · Criminel [" + getAdventureRank(p.level) + "]";
    }

    /** Prime : base selon le niveau (arrondie a 10) et ajouts des joueurs. */
    static long bounty(GuardManager.Post p) {
        return Math.round(p.level * BOUNTY_PER_LEVEL / 10.0) * 10 + p.bounty;
    }

    /** Criminels poses, pour le tableau de prime. */
    public static List<Wanted> list() {
        List<Wanted> out = new ArrayList<>();
        for (GuardManager.Post p : GuardManager.getPosts())
            if (p.type == GuardType.CRIMINEL)
                out.add(new Wanted(p.id, name(p), p.level, bounty(p), p.isAlive(), p.respawnAt, p.world, p.position()));
        return out;
    }

    /** Ajoute de l'or a la prime d'un criminel (tableau de prime) ; false s'il n'existe plus. */
    public static boolean addBounty(String postId, long add) {
        GuardManager.Post p = GuardManager.post(postId);
        if (p == null || p.type != GuardType.CRIMINEL) return false;
        p.bounty += add;
        GuardManager.saveLater();
        if (add >= 10_000) announce("[Prime] La prime sur le criminel " + name(p) + " passe à " + bounty(p) + " or.");
        return true;
    }

    /** Poste d'un criminel (par l'UUID du PNJ), null sinon. */
    static GuardManager.Post post(UUID npc) {
        if (npc == null) return null;
        for (GuardManager.Post p : GuardManager.getPosts()) if (npc.equals(p.guard) && p.type == GuardType.CRIMINEL) return p;
        return null;
    }

    /**
     * Criminel abattu par un joueur (DeathXPSystem, thread du monde) : sa prime (0 si ce n'est pas un criminel), annonce.
     * La prime revient a sa base pour sa prochaine apparition.
     */
    public static long claim(Ref<EntityStore> victim, ComponentAccessor<EntityStore> store, String killer) {
        GuardManager.Post p = GuardManager.postOf(victim, store);
        if (p == null || p.type != GuardType.CRIMINEL) return 0;
        long reward = bounty(p);
        p.bounty = 0;
        GuardManager.saveLater();
        announce("[Prime] " + killer + " a abattu le criminel " + name(p) + " et touche sa prime de " + reward + " or.");
        EldaniorLogger.info("[Guards] criminel " + p.id + " abattu par " + killer + " (" + reward + " or)");
        return reward;
    }

    /**
     * Toutes les 2 s (thread du monde) : le joueur le plus proche (hors duel et arene) devient sa cible. Son role ignore les
     * joueurs : seul ce code l'agresse. Les PK sont epargnes, sauf ceux qui ont frappe sa bande (gang : poste du chef) ; le
     * gabarit riposte de lui-meme a qui le frappe.
     */
    static void aggress(Ref<EntityStore> me, Vector3d at, com.hypixel.hytale.component.Store<EntityStore> store, String gang) {
        Ref<EntityStore> best = null;
        double bestD = AGGRO_RANGE * AGGRO_RANGE;
        java.util.Map<UUID, Long> angry = provoked.getOrDefault(gang, java.util.Map.of());
        long now = System.currentTimeMillis();
        for (Ref<EntityStore> e : new ArrayList<>(com.hypixel.hytale.server.core.util.TargetUtil.getAllEntitiesInSphere(at, AGGRO_RANGE, store))) {
            if (e == null || !e.isValid() || store.getComponent(e, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()) == null) continue;
            UUID u = PostMovement.uuid(e, store);
            if (u == null || com.eldanior.system.duel.DuelManager.isInDuel(u) || com.eldanior.system.territory.ArenaManager.isInArena(u)) continue;
            var data = store.getComponent(e, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
            if (data != null && data.isPK() && angry.getOrDefault(u, 0L) < now) continue;          // un des leurs
            var t = store.getComponent(e, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
            if (t == null) continue;
            double d = t.getPosition().distanceSquared(at);
            if (d < bestD) { bestD = d; best = e; }
        }
        if (best == null) return;
        com.eldanior.system.util.NpcAttitudes.hostile(me, best, HOSTILE_SECONDS, store);
        GuardManager.call(me, best, store);
    }

    /** Un joueur frappe un criminel ou un complice (GuardAlertSystem) : toute la bande lui en veut pendant PROVOKE_MS. */
    static void provoke(Ref<EntityStore> victim, UUID player, com.hypixel.hytale.component.ComponentAccessor<EntityStore> store) {
        String gang = gangOf(victim, store);
        if (gang == null || player == null) return;
        provoked.computeIfAbsent(gang, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(player, System.currentTimeMillis() + PROVOKE_MS);
    }

    /** Bande d'un PNJ (poste du chef), null si ce n'est ni un criminel ni un complice. */
    private static String gangOf(Ref<EntityStore> ref, com.hypixel.hytale.component.ComponentAccessor<EntityStore> store) {
        GuardManager.Post p = GuardManager.postOf(ref, store);
        if (p != null) return p.type == GuardType.CRIMINEL ? p.id : null;
        UUID u = ref != null && ref.isValid() ? PostMovement.uuid(ref, store) : null;
        GuardManager.Post a = u != null ? allies.get(u) : null;
        return a != null ? a.leader : null;
    }

    // ===== Complices =====

    static int allyCount(GuardManager.Post chief) {
        return ALLIES_BY_RANK[Math.max(0, "EDCBAS".indexOf(getAdventureRank(chief.level)))];
    }

    /** Poste (non enregistre) du complice i : graine tiree de celle du chef (jamais mage), niveau reduit, autour du chef. */
    private static GuardManager.Post allyPost(GuardManager.Post chief, int i, Vector3d at) {
        double a = 2 * Math.PI * i / Math.max(1, allyCount(chief));
        GuardManager.Post ally = new GuardManager.Post(chief.id + "_complice_" + i, GuardType.CRIMINEL, chief.world,
                at.x + Math.cos(a) * 3, at.y, at.z + Math.sin(a) * 3, chief.yaw);
        long seed = chief.skinSeed * 131 + i + 1;
        while (!BountyHunter.isHunterType(AdventurerType.of(seed))) seed++;
        ally.skinSeed = seed;
        ally.level = Math.max(GuardManager.ADVENTURER_MIN_LEVEL, (int) Math.round(chief.level * ALLY_LEVEL));
        ally.leader = chief.id;
        return ally;
    }

    /** Le chef apparait (thread du monde) : ses anciens complices sont retires, de nouveaux l'entourent. */
    static void spawnAllies(GuardManager.Post chief, com.hypixel.hytale.server.core.universe.world.World world) {
        removeAllies(chief, world);
        for (int i = 0; i < allyCount(chief); i++) {
            GuardManager.Post ally = allyPost(chief, i, chief.position());
            Ref<EntityStore> ref = GuardManager.spawnAs(ally, world, role(ally), ally.position());
            if (ref == null || ally.guard == null) continue;
            chief.allies.add(ally.guard);
            allies.put(ally.guard, ally);
        }
        GuardManager.saveLater();
    }

    static void removeAllies(GuardManager.Post chief, com.hypixel.hytale.server.core.universe.world.World world) {
        for (UUID u : chief.allies) {
            allies.remove(u);
            Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(u);
            if (ref != null && ref.isValid()) world.getEntityStore().getStore().removeEntity(ref, com.hypixel.hytale.component.RemoveReason.REMOVE);
        }
        chief.allies.clear();
    }

    /**
     * Chaque seconde (thread du monde) : les complices du chef le suivent en cercle autour de lui ; toutes les 2 s ils
     * cherchent une cible ; toutes les 5 s leur skin est regenere s'il a ete perdu (rechargement de la zone).
     */
    static void lead(GuardManager.Post chief, Vector3d at, com.hypixel.hytale.server.core.universe.world.World world,
                     com.hypixel.hytale.component.Store<EntityStore> store, boolean scan, boolean slow) {
        int i = 0;
        for (UUID u : chief.allies) {
            int index = i++;
            Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(u);
            if (ref == null || !ref.isValid()) continue;
            GuardManager.Post ally = allies.computeIfAbsent(u, k -> { GuardManager.Post a = allyPost(chief, index, at); a.guard = k; return a; });
            com.hypixel.hytale.server.npc.entities.NPCEntity npc = store.getComponent(ref, com.hypixel.hytale.server.npc.entities.NPCEntity.getComponentType());
            var t = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
            if (npc == null || t == null) continue;
            double a = 2 * Math.PI * index / Math.max(1, chief.allies.size());
            npc.setLeashPoint(new Vector3d(at.x + Math.cos(a) * 3.5, at.y, at.z + Math.sin(a) * 3.5));
            if (scan) aggress(ref, new Vector3d(t.getPosition()), store, chief.id);
            if (slow) GuardManager.refreshLook(ally, ref, store);
        }
    }

    static void announce(String text) {
        for (PlayerRef pr : Universe.get().getPlayers()) pr.sendMessage(Message.raw(text).color(Color.ORANGE));
    }
}
