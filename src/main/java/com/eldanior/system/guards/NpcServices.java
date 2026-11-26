package com.eldanior.system.guards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.boards.GuildMasterScreen;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.definitions.npc.NpcQuestInteraction;
import com.eldanior.system.territory.ParcelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Systeme central des PNJ de poste : un PNJ = un type (comportement, combat, tenue) + des services, reglables dans la fenetre du
 * bloc de poste (NpcPostPage) :
 *   - Recruter (aventurier libre), Guilde (maitre de guilde) : propres au type ;
 *   - Commercer : boutique du poste (Post.shop : boutique Luo « BOUTIQUE:RANG » ou assortiment « AMBULANT ») ;
 *   - Quetes : quetes d'un donneur (Post.quest = npcGiverId des QuestModel, NpcQuestInteraction) ;
 *   - Discuter : repliques du PNJ (Post.lines, une par ligne), dites l'une apres l'autre.
 * A l'interaction (NpcInteractSystem) : un seul service sans discussion -> il s'ouvre directement ; sinon la fenetre de
 * discussion (NpcTalkPage) salue et propose les services.
 * Seuls les civils se parlent (villageois, pelerin, marchands, aventurier libre, maitre de guilde) : leurs roles sont
 * interactifs (scripts/npcs/gen_roles_civils.py) ; les gardes restent des combattants muets.
 */
public final class NpcServices {

    private NpcServices() {}

    public enum Service {
        RECRUTER("Recruter", "L'engager pour une mission à vos côtés"),
        GUILDE("Guilde", "Fonder, rejoindre ou améliorer une guilde"),
        COMMERCER("Commercer", "Voir ses marchandises, ou lui vendre les vôtres"),
        QUETES("Quêtes", "Ce qu'il attend de vous, et ce qu'il offre en échange"),
        DUEL("Duel", "Le défier en duel d'entraînement, sans mort"),
        DISCUTER("Discuter", "Écouter ce qu'il a à raconter");

        final String label, description;
        Service(String label, String description) { this.label = label; this.description = description; }
    }

    private static final Random RNG = new Random();

    /** Types de PNJ a qui l'on parle (roles interactifs). */
    public static boolean talks(GuardType t) {
        return t == GuardType.VILLAGEOIS || t == GuardType.COMMERCANT || t == GuardType.MARCHAND || t == GuardType.AMBULANT
                || t == GuardType.AVENTURIER || t == GuardType.MAITRE_GUILDE || t == GuardType.LEGENDAIRE || t == GuardType.SENSEI
                || t == GuardType.DISCIPLE;                                                         // 2026-11-13 : duel
    }

    /** Quetes des PNJ desactivees pour l'instant (bugs a revoir) : ni service « Quetes », ni reglage dans le poste.
     *  Les donneurs deja enregistres sur les postes (Post.quest) sont conserves pour la reactivation. */
    static final boolean QUESTS_ENABLED = false;

    public static List<Service> services(GuardManager.Post p) {
        List<Service> out = new ArrayList<>();
        if (p.type == GuardType.AVENTURIER && p.owner == null) out.add(Service.RECRUTER);
        if (p.type == GuardType.MAITRE_GUILDE) out.add(Service.GUILDE);
        if (LuoMerchants.sells(p)) out.add(Service.COMMERCER);
        // quetes en chapitres (quest.chapitres) : Sensei, et tout PNJ dont un admin a active les quetes dans le poste (2026-11-13)
        if (chapterGiver(p) != null) out.add(Service.QUETES);
        if (MartialDuels.duels(p.type)) out.add(Service.DUEL);
        if (!lines(p).isEmpty()) out.add(Service.DISCUTER);
        return out;
    }

    /**
     * Services proposes a ce joueur : comme services(p), mais « Quetes » n'apparait que s'il a au moins une quete a voir
     * (condition de la premiere quete remplie, 2026-11-13 : ex. etre Apprenti en Arts Martiaux pour le Sensei).
     */
    public static List<Service> services(GuardManager.Post p, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        List<Service> out = services(p);
        String giver = chapterGiver(p);
        PlayerRef admin = store.getComponent(playerRef, PlayerRef.getComponentType());
        // disciple : duel seulement s'il est l'adversaire designe d'une quete en cours du joueur (2026-11-13)
        if (p.type == GuardType.DISCIPLE && !com.eldanior.system.quest.chapitres.ChapterQuests.canDuel(uuidOf(admin), p.id))
            out.remove(Service.DUEL);
        boolean isAdmin = admin != null && admin.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        if (giver != null && out.contains(Service.QUETES) && !isAdmin) {     // un admin voit toujours l'onglet (carte « + »)
            PlayerLevelData data = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
            Player player = store.getComponent(playerRef, Player.getComponentType());
            PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
            java.util.UUID me = null;
            try { me = pr != null ? com.eldanior.system.config.UUIDExtractor.getUUID(pr) : null; } catch (Exception ignored) { }
            if (com.eldanior.system.quest.chapitres.ChapterQuests.visibleActs(giver, p.id, me, data, player).isEmpty())
                out.remove(Service.QUETES);
        }
        return out;
    }

    private static java.util.UUID uuidOf(PlayerRef pr) {
        try { return pr != null ? com.eldanior.system.config.UUIDExtractor.getUUID(pr) : null; } catch (Exception e) { return null; }
    }

    public static List<String> lines(GuardManager.Post p) {
        List<String> out = new ArrayList<>();
        if (p.lines == null) return out;
        for (String l : p.lines.split("\n")) if (!l.isBlank()) out.add(l.trim());
        return out;
    }

    /** Salut du PNJ quand on lui parle : une de ses repliques, sinon celle de son type. */
    static String greeting(GuardManager.Post p) { return greeting(p, false); }

    /** challenger : le joueur est l'adversaire que ce PNJ attend (duel de quete, 2026-11-13). */
    static String greeting(GuardManager.Post p, boolean challenger) {
        if (p.type == GuardType.DISCIPLE && challenger) return Sensei.discipleGreeting(p, true);
        List<String> l = lines(p);
        if (!l.isEmpty()) return l.get(RNG.nextInt(l.size()));
        return switch (p.type) {
            case MARCHAND -> "Bienvenue au Comptoir ! Jetez un œil à mes marchandises.";
            case AMBULANT -> "Approchez, approchez ! J'ai un peu de tout, venu des quatre coins du monde.";
            case COMMERCANT -> NpcTrades.metier(p) != null ? NpcTrades.metier(p).pitch : "Bienvenue !";
            case LEGENDAIRE -> LegendarySmiths.of(p).pitch;
            case SENSEI -> Sensei.greeting(p);
            case DISCIPLE -> Sensei.discipleGreeting(p, false);
            case AVENTURIER -> "Ma lame est à louer, voyageur.";
            case MAITRE_GUILDE -> "Une guilde est une famille qu'on se choisit.";
            default -> "Bonjour, voyageur.";
        };
    }

    /** Un joueur parle au PNJ du poste p. Thread du monde. */
    static void interact(GuardManager.Post p, Ref<EntityStore> npc, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        Player player = store.getComponent(playerRef, Player.getComponentType());
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        List<Service> list = services(p, playerRef, store);
        if (list.size() == 1 && list.get(0) != Service.DISCUTER) {
            run(list.get(0), p, npc, playerRef, store);
            return;
        }
        try {
            player.getPageManager().openCustomPage(playerRef, store, new NpcTalkPage(pr, p.id,
                    greeting(p, p.type == GuardType.DISCIPLE && com.eldanior.system.quest.chapitres.ChapterQuests.canDuel(uuidOf(pr), p.id))));
        } catch (Exception e) {
            EldaniorLogger.error("[PNJ] discussion " + p.id, e);
        }
    }

    /** Ouvre le service choisi (sauf Discuter, gere par la fenetre de discussion). */
    static void run(Service s, GuardManager.Post p, Ref<EntityStore> npc, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        Player player = store.getComponent(playerRef, Player.getComponentType());
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        try {
            switch (s) {
                case RECRUTER -> {
                    PlayerLevelData data = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
                    player.getPageManager().openCustomPage(playerRef, store, new RecruitScreen(pr, p.id, p.level,
                            GuardManager.priceOf(p), data != null ? data.getMoney() : 0, GuardManager.nameplate(p)));
                }
                case GUILDE -> player.getPageManager().openCustomPage(playerRef, store, new GuildMasterScreen(pr, place(npc, store)));
                case COMMERCER -> LuoMerchants.open(p, playerRef, store);
                case QUETES -> {
                    String giver = chapterGiver(p);
                    if (giver != null) com.eldanior.system.quest.chapitres.QuestChaptersPage.open(playerRef, store, giver,
                            GuardManager.nameplate(p), p.id, pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION));
                    else NpcQuestInteraction.interact(pr, store, playerRef, p.quest);
                }
                case DUEL -> {                                                              // Sensei et disciples
                    if (p.type == GuardType.DISCIPLE && !com.eldanior.system.quest.chapitres.ChapterQuests.canDuel(uuidOf(pr), p.id))
                        pr.sendMessage(com.hypixel.hytale.server.core.Message.raw(GuardManager.nameplate(p)
                                + " : « Je ne me bats qu'avec ceux que le Sensei m'envoie. »"));
                    else Sensei.duel(p, npc, playerRef, store);
                }
                case DISCUTER -> { }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[PNJ] service " + s + " de " + p.id, e);
        }
    }

    /** Valeur de Post.quest qui active les quetes en chapitres sur un PNJ (reglage du poste, admins). */
    public static final String CHAPTERS = "chapitres";

    /**
     * Donneur des quetes en chapitres de ce PNJ (ChapterQuests), null si ses quetes ne sont pas actives : le Sensei a toujours les
     * siennes (« sensei ») ; un autre PNJ parlant les a si un admin les a activees dans le poste (donneur « poste:<id> », sans
     * quete au depart : l'admin voit la carte « + »).
     */
    public static String chapterGiver(GuardManager.Post p) {
        if (p == null || !talks(p.type)) return null;
        if (p.type == GuardType.SENSEI) return "sensei";
        if (p.type == GuardType.COMMERCANT && NpcTrades.metier(p) == NpcTrades.Metier.DRESSEUR)   // 2026-11-13 : quete de l'oeuf
            return com.eldanior.system.quest.chapitres.ChapterQuests.DRESSEUR;
        // 2026-11-14 : quetes des PNJ de metier (QuetesMetiers) ; donneur seulement s'il a des quetes
        String own = switch (p.type) {
            case COMMERCANT -> NpcTrades.metier(p) != null ? NpcTrades.metier(p).name().toLowerCase() : null;
            case MARCHAND -> "marchand_luo";
            case AMBULANT -> "ambulant";
            case MAITRE_GUILDE -> "maitre_guilde";
            case LEGENDAIRE -> "forgeron_legendaire";
            default -> null;
        };
        if (own != null && com.eldanior.system.quest.chapitres.ChapterQuests.hasGiver(own)) return own;
        return p.quest != null && p.quest.startsWith(CHAPTERS) ? "poste:" + p.id : null;
    }

    /**
     * Quetes de la bibliotheque ajoutees a ce PNJ par un admin (carte « + ») : Post.quest = « chapitres:id1,id2 » (sauvegarde du
     * poste). Liste vide si aucune.
     */
    /** Lieu-dit du PNJ a qui un admin a ajoute cette quete (carte « + »), null si personne (2026-11-14, temoins du Roi). */
    public static String placeOfChain(String chainId) {
        for (GuardManager.Post p : GuardManager.getPosts())
            if (assignedChains(p.id).contains(chainId)) return MartialDuels.placeName(p.world, p.position());
        return null;
    }

    public static List<String> assignedChains(String postId) {
        List<String> out = new ArrayList<>();
        for (String id : chapterEntries(postId)) if (!id.startsWith("-")) out.add(id);
        return out;
    }

    /**
     * Quetes propres au donneur (celles du Sensei) qu'un admin a retirees de ce PNJ (2026-11-13) : notees « -id » dans
     * Post.quest, a cote des quetes ajoutees.
     */
    public static List<String> removedChains(String postId) {
        List<String> out = new ArrayList<>();
        for (String id : chapterEntries(postId)) if (id.startsWith("-") && id.length() > 1) out.add(id.substring(1));
        return out;
    }

    private static List<String> chapterEntries(String postId) {
        GuardManager.Post p = postId != null ? GuardManager.post(postId) : null;
        String quest = com.eldanior.system.boards.BoardQuests.isBoard(postId) ? com.eldanior.system.boards.BoardQuests.get(postId)
                : p != null ? p.quest : null;                                                 // tableau des quetes (2026-11-13) ou PNJ
        List<String> out = new ArrayList<>();
        if (quest == null || !quest.startsWith(CHAPTERS + ":")) return out;
        for (String id : quest.substring(CHAPTERS.length() + 1).split(",")) if (!id.isBlank()) out.add(id.trim());
        return out;
    }

    private static boolean known(String postId) {
        return com.eldanior.system.boards.BoardQuests.isBoard(postId) || postId != null && GuardManager.post(postId) != null;
    }

    private static void writeEntries(String postId, List<String> entries) {
        if (com.eldanior.system.boards.BoardQuests.isBoard(postId)) {
            com.eldanior.system.boards.BoardQuests.set(postId, entries.isEmpty() ? null : CHAPTERS + ":" + String.join(",", entries));
            return;
        }
        GuardManager.Post p = postId != null ? GuardManager.post(postId) : null;
        if (p == null) return;
        p.quest = CHAPTERS + ":" + String.join(",", entries);
        GuardManager.saveLater();
    }

    /** Ajoute une quete a ce PNJ (et active ses quetes) : quete de la bibliotheque, ou quete propre retiree puis remise. */
    public static void assignChain(String postId, String chainId) {
        if (chainId == null || !known(postId)) return;
        List<String> entries = chapterEntries(postId);
        if (!entries.remove("-" + chainId) && !entries.contains(chainId)) entries.add(chainId);
        writeEntries(postId, entries);
    }

    /**
     * Retire une quete de ce PNJ (2026-11-13, admin) : toute la serie part avec elle. Quete ajoutee par « + » : rayee de la
     * liste ; quete propre au donneur (own = true, le Sensei) : notee « -id » pour ne plus etre proposee.
     */
    public static void unassignChain(String postId, String chainId, boolean own) {
        if (chainId == null || !known(postId)) return;
        List<String> entries = chapterEntries(postId);
        entries.remove(chainId);
        if (own && !entries.contains("-" + chainId)) entries.add("-" + chainId);
        writeEntries(postId, entries);
    }

    /** Ville (ou territoire, royaume) du PNJ, null hors des villes. */
    private static String place(Ref<EntityStore> npc, Store<EntityStore> store) {
        if (npc == null || !npc.isValid()) return null;
        TransformComponent tr = store.getComponent(npc, TransformComponent.getComponentType());
        World world = store.getExternalData().getWorld();
        ParcelData town = tr != null && world != null ? BountyHunter.treasuryAt(world.getName(), new Vector3d(tr.getPosition())) : null;
        return town != null ? town.getName() : null;
    }
}
