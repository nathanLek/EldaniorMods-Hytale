package com.eldanior.system.quest.chapitres;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.shop.CoinWallet;
import com.eldanior.system.quest.chapitres.ChapterQuests.Act;
import com.eldanior.system.quest.chapitres.ChapterQuests.Kind;
import com.eldanior.system.quest.chapitres.ChapterQuests.Objective;
import com.eldanior.system.quest.chapitres.ChapterQuests.Reward;
import com.eldanior.system.quest.chapitres.ChapterQuests.RewardKind;
import com.eldanior.system.quest.chapitres.ChapterQuests.State;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fenetre des quetes d'un PNJ, sur le modele de la fenetre Chapitres du jeu (Quest/Chapitres.ui, genere par
 * scripts/quetes/gen_chapitres_ui.py) : a gauche le detail de l'acte choisi (embleme et rang de mission, type de mission,
 * « Acte I », nom, histoire, explications, attendus, recompenses), a droite le carrousel des cartes (une par acte visible,
 * eclats autour de la carte choisie, rang en haut). Seuls les actes dont la condition est remplie sont montres (2026-11-13).
 * Boutons : Accepter / Trouver un adversaire (le donneur designe un PNJ poste a defier) / Suivre / Rendre, Fermer.
 * Admin (2026-11-13) : il voit tous les actes (meme verrouilles) et une carte « + » en fin de liste pour ajouter des quetes.
 */
public class QuestChaptersPage extends InteractiveCustomUIPage<QuestChaptersPage.ChapEventData> {

    // Doit correspondre a scripts/quetes/gen_chapitres_ui.py
    static final int CARDS = 3, NOTES = 3, OBJECTIVES = 4, REWARDS = 3;

    private final String giver, speaker, postId;
    private final boolean admin;
    private String selectedId;
    /** Adversaire designe par le donneur pour l'acte choisi (poste), et l'acte concerne. */
    private static final String PLUS = "+", LOCKED_RED = "#e0574a";
    private int cardOffset = 0, rewardOffset = 0;

    public QuestChaptersPage(@Nonnull PlayerRef playerRef, String giver, String speaker, String postId, boolean admin) {
        super(playerRef, CustomPageLifetime.CanDismiss, ChapEventData.CODEC);
        this.giver = giver;
        this.speaker = speaker;
        this.postId = postId;
        this.admin = admin;
    }

    /** Tableau de prime (2026-11-13) : lieu, monde et position du bloc, pour la fenetre des mises a prix. */
    private String boardPlace, boardWorld;
    private org.joml.Vector3d boardPos;

    private boolean bountyBoard() { return ChapterQuests.PRIMES.equals(giver) && boardWorld != null; }

    /** Mise a prix (tableau de prime) : montants proposes, montant choisi, premier clic arme. */
    private static final long[] BOUNTY_STEPS = {1_000, 5_000, 10_000, 50_000, 100_000};
    private long bountyAdd = 1_000;
    private boolean bountyArmed;

    private static long bountyCost(long amount) {
        return amount + (long) Math.ceil(amount * com.eldanior.system.boards.BountyBoard.COMMISSION);
    }

    /**
     * Second clic sur « Mettre a prix » / « Racheter ma prime » (ancienne fenetre du tableau de prime, 2026-11-13) : paiement en
     * pieces puis en banque ; commission (mise a prix) ou rachat verses au tresor de la ville du tableau. Renvoie le message.
     */
    private String bountyAction(Ref<EntityStore> ref, Store<EntityStore> store, Act a) {
        com.eldanior.system.boards.BountyBoard.Wanted w = Primes.wantedOf(a.chain.id);
        UUID me = uuid(ref, store);
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (w == null || me == null || data == null || player == null) return "Cet avis n'est plus affiché.";
        boolean self = w.player() != null && w.player().equals(me);
        long cost = self ? w.bounty() * com.eldanior.system.boards.BountyBoard.BUYBACK_FACTOR : bountyCost(bountyAdd);
        if (!Primes.payWithBank(player, data, cost)) return "Il vous faut " + CoinWallet.format(cost) + " (pièces et compte réunis).";
        long toTown;
        String done;
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (self) {
            data.collectBounty();
            QuestManager.unregisterBounty(me);
            com.eldanior.system.boards.BountyBoard.save();
            com.eldanior.system.boards.BountyBoard.announce("[Prime] " + (pr != null ? pr.getUsername() : "Un criminel")
                    + " a racheté sa prime pour " + CoinWallet.format(cost) + ".");
            toTown = cost;
            done = "Votre prime est effacée. Tant que vous êtes PK, les chasseurs de prime vous traquent toujours.";
        } else {
            if (!com.eldanior.system.boards.BountyBoard.addBounty(w, bountyAdd)) {
                data.addMoney(cost);                                                     // plus recherche : rendu au compte
                save(ref, store, type, data, me);
                return "Il n'est plus recherché : votre or a été versé à votre compte.";
            }
            toTown = cost - bountyAdd;
            done = "Prime de " + CoinWallet.format(bountyAdd) + " placée sur " + w.name() + ".";
        }
        save(ref, store, type, data, me);
        var town = com.eldanior.system.guards.BountyHunter.treasuryAt(boardWorld, boardPos);
        if (town != null && toTown > 0) {
            town.addTreasury(toTown);
            com.eldanior.system.territory.ParcelManager.save();
        }
        return done;
    }

    /** Une quete qu'un admin peut retirer : ni affiche du jour, ni prime, ni contrat de monstre, ni carte de guilde. */
    private static boolean removable(Act a) {
        return a.chain.daily == null && a.chain.type != ChapterQuests.MissionType.PRIME && a.chain.type != ChapterQuests.MissionType.CONTRAT
                && a.chain.type != ChapterQuests.MissionType.GUILDE && a.chain.type != ChapterQuests.MissionType.MISSION_GUILDE;
    }

    /** Tableau de guilde (2026-11-19) : quetes et missions de guilde en cartes (GuildQuests). */
    public static void openGuildBoard(Ref<EntityStore> ref, Store<EntityStore> store, String place, boolean admin) {
        open(ref, store, ChapterQuests.GUILDE, "Tableau de guilde" + (place != null ? " · " + place : ""), null, admin);
    }

    /** Tableau de prime : primes et contrats en cartes, bouton « Mettre a prix » vers l'ancienne fenetre (BountyBoardScreen). */
    public static void openBountyBoard(Ref<EntityStore> ref, Store<EntityStore> store, String place, String world,
                                       org.joml.Vector3d pos, boolean admin) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        QuestChaptersPage page = new QuestChaptersPage(pr, ChapterQuests.PRIMES, "Tableau de prime" + (place != null ? " · " + place : ""),
                null, admin);
        page.boardPlace = place;
        page.boardWorld = world;
        page.boardPos = pos;
        player.getPageManager().openCustomPage(ref, store, page);
    }

    /** Ouvre la fenetre des quetes d'un donneur (ex. « sensei ») ; postId = poste du PNJ (duels), peut etre null. */
    public static void open(Ref<EntityStore> ref, Store<EntityStore> store, String giver, String speaker, String postId, boolean admin) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        player.getPageManager().openCustomPage(ref, store, new QuestChaptersPage(pr, giver, speaker, postId, admin));
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Quest/Chapitres.ui");
        for (int i = 0; i < CARDS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapCard" + i, EventData.of("Action", "card").append("Index", String.valueOf(i)), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapPrev", EventData.of("Action", "prev"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapNext", EventData.of("Action", "next"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapRewPrev", EventData.of("Action", "rprev"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapRewNext", EventData.of("Action", "rnext"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapAction", EventData.of("Action", "main"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapClose", EventData.of("Action", "close"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapAlt", EventData.of("Action", "alt"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChapFilter", EventData.of("Action", "filter"), false);
        fill(ui, ref, store, null);
    }

    // ==================== AFFICHAGE ====================

    /** Une ligne d'attendu affichee : texte, avancement, rempli. */
    /** Ligne d'attendu (texte, avancement, rempli) : aussi lue par le HUD des quetes (QuestHud, 2026-11-14). */
    public record Row(String text, String count, boolean ok) {}

    /** Cartes : actes visibles du joueur ; un admin voit tous les actes, plus la carte « + » (null) a la fin. */
    private List<Act> acts(UUID me, PlayerLevelData data, Player player) {
        // 2026-11-13 : les quetes non accessibles sont montrees verrouillees (rouge, cadenas) ; l'onglet n'existe que si au moins
        // une est accessible (NpcServices)
        List<Act> out = new ArrayList<>();
        if (ChapterQuests.TABLEAU.equals(giver)) out.addAll(Journalieres.acts(data != null && data.isPK()));   // affiches du jour
        if (ChapterQuests.TABLEAU.equals(giver)) out.addAll(HouseQuests.boardActs(me, data));   // quetes des maisons (2026-11-18)
        if (ChapterQuests.GUILDE.equals(giver)) return GuildQuests.acts(me);          // tableau de guilde (2026-11-19)
        out.addAll(admin || !ChapterQuests.visibleActs(giver, postId, me, data, player).isEmpty()
                ? ChapterQuests.actsOf(giver, postId) : List.of());
        if (admin && !ChapterQuests.PRIMES.equals(giver)) out.add(null);             // tableau de prime : avis seulement
        return out;
    }

    private static String id(Act a) { return a == null ? PLUS : a.id; }

    /** « Quete exclusive 2 » : numero parmi les quetes du meme type de ce PNJ, seulement pour une quete a suite (2026-11-13). */
    private static String label(Act a, List<Act> acts) {
        if (!a.chain.evolutive()) return a.chain.type.label;            // pas de suite : pas de numero (2026-11-13)
        int n = 0;
        for (Act x : acts) {
            if (x == null || x.chain.type != a.chain.type) continue;
            n++;
            if (x == a) break;
        }
        return a.chain.type.label + " " + Math.max(1, n);
    }

    /** Carte « + » : quete de la bibliotheque proposee a l'ajout (index dans cands()). */
    private int plusChoice = 0;
    /** Carte « + » : categorie affichee (null : toutes), 2026-11-13 « trier les quetes par categorie a l'ajout ». */
    private ChapterQuests.MissionType plusFilter;

    /** Quetes ajoutables, de la categorie choisie, rangees par categorie puis par titre. */
    private List<ChapterQuests.Chain> cands() {
        List<ChapterQuests.Chain> all = new ArrayList<>(ChapterQuests.addable(giver, postId));
        all.sort(java.util.Comparator.comparing((ChapterQuests.Chain c) -> c.type.ordinal()).thenComparing(c -> c.title, String.CASE_INSENSITIVE_ORDER));
        if (plusFilter != null && all.stream().noneMatch(c -> c.type == plusFilter)) plusFilter = null;   // categorie videe
        if (plusFilter == null) return all;
        all.removeIf(c -> c.type != plusFilter);
        return all;
    }

    /** Categorie suivante qui a au moins une quete a ajouter (puis « toutes »). */
    private void nextFilter() {
        List<ChapterQuests.Chain> all = ChapterQuests.addable(giver, postId);
        ChapterQuests.MissionType[] types = ChapterQuests.MissionType.values();
        int start = plusFilter == null ? 0 : plusFilter.ordinal() + 1;
        for (int i = start; i < types.length; i++) {
            ChapterQuests.MissionType t = types[i];
            if (all.stream().anyMatch(c -> c.type == t)) { plusFilter = t; plusChoice = 0; return; }
        }
        plusFilter = null;
        plusChoice = 0;
    }

    private String filterLabel() {
        return "Catégorie : " + (plusFilter == null ? "toutes" : plusFilter.label.replace("Quête ", ""));
    }
    /** Admin : quete dont le retrait attend la confirmation (second clic). */
    private String pendingRemoval;

    private int selectedIndex(List<Act> acts, UUID me, PlayerLevelData data, Player player) {
        for (int i = 0; i < acts.size(); i++) if (id(acts.get(i)).equals(selectedId)) return i;
        int sel = 0;                                                     // premiere ouverture : l'acte en cours, sinon le dernier
        for (int i = 0; i < acts.size(); i++) {
            if (acts.get(i) == null) break;
            sel = i;
            State st = ChapterQuests.state(me, acts.get(i), data, player);
            if (st != State.DONE && st != State.LOCKED) break;
        }
        if (!acts.isEmpty()) selectedId = id(acts.get(sel));
        cardOffset = Math.max(0, Math.min(sel - CARDS + 1, acts.size() - CARDS));
        return sel;
    }

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store, String hint) {
        UUID me = uuid(ref, store);
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        Player player = store.getComponent(ref, Player.getComponentType());
        List<Act> acts = acts(me, data, player);
        int selected = selectedIndex(acts, me, data, player);
        cardOffset = Math.max(0, Math.min(cardOffset, Math.max(0, acts.size() - CARDS)));

        // ---- carrousel ----
        for (int i = 0; i < CARDS; i++) {
            int n = cardOffset + i;
            boolean shown = n < acts.size();
            ui.set("#ChapCard" + i + ".Visible", shown);
            if (!shown) continue;
            Act a = acts.get(n);
            boolean sel = n == selected, plus = a == null;
            State s = plus ? State.LOCKED : ChapterQuests.state(me, a, data, player);
            boolean dark = plus || s == State.LOCKED;                    // carte « + » ou acte verrouille (admin) : cadre sombre
            for (ChapterQuests.Art art : ChapterQuests.Art.values()) {
                String t = artId(art);
                ui.set("#ChapArt" + t + "C" + i + ".Visible", !dark && sel && a.art == art);
                ui.set("#ChapArt" + t + "G" + i + ".Visible", !dark && !sel && a.art == art);
            }
            // contour selon le type (2026-11-13) : principale ou rang S en or, secondaire en bleu, journaliere en vert,
            // exclusive en violet ; cadres teintes generes par gen_chapitres_ui.py (TINTS)
            String tint = dark ? null : tint(a);
            ui.set("#ChapFrameD" + i + ".Visible", !dark && !sel && tint == null);
            ui.set("#ChapFrameS" + i + ".Visible", !dark && sel && tint == null);
            for (String t : TINTS) {
                ui.set("#ChapFrameD" + i + t + ".Visible", !sel && t.equals(tint));
                ui.set("#ChapFrameS" + i + t + ".Visible", sel && t.equals(tint));
            }
            ui.set("#ChapFrameL" + i + ".Visible", dark);
            ui.set("#ChapFrameLS" + i + ".Visible", dark && sel);
            ui.set("#ChapCardLock" + i + ".Visible", !plus && dark);
            ui.set("#ChapCardPlus" + i + ".Visible", plus);
            ui.set("#ChapCardShade" + i + ".Visible", !dark);
            ui.set("#ChapCardShards" + i + ".Visible", sel && tint == null);   // eclats de la couleur du contour (2026-11-13)
            for (String t : TINTS) ui.set("#ChapCardShards" + i + t + ".Visible", sel && t.equals(tint));
            ui.set("#ChapCardBadge" + i + ".Visible", !plus);
            ui.set("#ChapCardRank" + i + ".Visible", !plus);
            if (plus) {
                ui.set("#ChapCardTitle" + i + ".Text", "Ajouter");
                ui.set("#ChapCardTitle" + i + ".Style.TextColor", "#f2c45e");
                ui.set("#ChapCardState" + i + ".Text", "Nouvelle quête");
                ui.set("#ChapCardState" + i + ".Style.TextColor", "#96a9be");
                continue;
            }
            ui.set("#ChapCardRank" + i + ".Text", a.rank.getDisplayName());
            ui.set("#ChapCardRank" + i + ".Style.TextColor", a.rank.getColor());
            ui.set("#ChapCardTitle" + i + ".Text", label(a, acts));
            ui.set("#ChapCardTitle" + i + ".Style.TextColor", s == State.LOCKED ? LOCKED_RED : "#f2c45e");
            ui.set("#ChapCardState" + i + ".Text", stateLabel(s, me, a));
            ui.set("#ChapCardState" + i + ".Style.TextColor", stateColor(s));
        }
        ui.set("#ChapPrev.Visible", cardOffset > 0);
        ui.set("#ChapNext.Visible", cardOffset + CARDS < acts.size());

        // ---- detail ----
        boolean any = !acts.isEmpty();
        ui.set("#ChapDetail.Visible", any);
        ui.set("#ChapGlows.Visible", any);
        ui.set("#ChapEmpty.Visible", !any);
        if (!any) { action(ui, null, null, null); ui.set("#ChapHint.Text", ""); return; }
        Act a = acts.get(selected);
        boolean plus = a == null;
        List<ChapterQuests.Chain> cands = plus ? cands() : List.of();
        if (plus) {                                                      // carte « + » : apercu de la quete a ajouter
            if (cands.isEmpty()) { fillPlus(ui); return; }
            plusChoice = Math.floorMod(plusChoice, cands.size());
            a = cands.get(plusChoice).acts.get(0);
        }
        State s = plus ? State.AVAILABLE : ChapterQuests.state(me, a, data, player);
        PlayerQuest pq = plus ? null : ChapterQuests.owned(me, a);
        boolean locked = s == State.LOCKED;
        ui.set("#ChapRank.Visible", true);                               // rang de la quete a la place de l'embleme (2026-11-13)
        ui.set("#ChapRank.Text", a.rank.getDisplayName());
        ui.set("#ChapRank.Style.TextColor", a.rank.getColor());
        ui.set("#ChapMission.Visible", true);
        ui.set("#ChapMission.Text", plus ? "Ajouter une quête  ·  " + (plusChoice + 1) + " / " + cands.size()
                : locked ? "Verrouillée" : a.chain.title);
        ui.set("#ChapMission.Style.TextColor", locked ? LOCKED_RED : a.chain.type.color);
        ui.set("#ChapChain.Text", speaker);
        ui.set("#ChapTitle.Text", plus ? a.chain.type.label : label(a, acts));
        ui.set("#ChapTitle.Style.TextColor", locked ? LOCKED_RED : "#f2c45e");
        // 2026-11-13 : une quete verrouillee ne revele rien (ni nom, ni histoire, ni attendus, ni recompenses) ; le donneur
        // dit seulement qu'il faut d'abord finir la precedente, ou progresser sur la voie
        ui.set("#ChapTagline.Text", locked ? "???" : a.name);
        ui.set("#ChapStory.Text", !locked ? ChapterQuests.withPlaces(a.story) : previousDone(me, a)
                ? "« Tu n'es pas encore prêt pour cette épreuve. Progresse sur la voie, puis reviens me voir. »"
                : "« Chaque chose en son temps. Termine d'abord ce que je t'ai confié. »");

        for (int i = 0; i < NOTES; i++) {
            boolean shown = !locked && i < a.notes.size();
            ui.set("#ChapNote" + i + ".Visible", shown);
            if (shown) ui.set("#ChapNoteText" + i + ".Text", ChapterQuests.withPlaces(a.notes.get(i)));
        }

        List<Row> rows = locked ? List.of() : rows(a, s, pq, data, player);
        ui.set("#ChapObjHead.Visible", !rows.isEmpty());
        for (int i = 0; i < OBJECTIVES; i++) {
            boolean shown = i < rows.size();
            ui.set("#ChapObj" + i + ".Visible", shown);
            if (!shown) continue;
            Row r = rows.get(i);
            ui.set("#ChapObjOn" + i + ".Visible", r.ok());
            ui.set("#ChapObjOff" + i + ".Visible", !r.ok());
            ui.set("#ChapObjText" + i + ".Text", r.text());
            ui.set("#ChapObjText" + i + ".Style.TextColor", r.ok() ? "#d9c58a" : "#b8c2d0");
            ui.set("#ChapObjCount" + i + ".Text", r.count());
            ui.set("#ChapObjCount" + i + ".Style.TextColor", r.ok() ? "#f0c868" : "#77879d");
        }

        // ---- recompenses ----
        ui.set("#ChapRewards.Visible", !locked && !a.rewards.isEmpty());
        rewardOffset = Math.max(0, Math.min(rewardOffset, a.rewards.size() - REWARDS));
        for (int j = 0; j < REWARDS; j++) {
            int n = rewardOffset + j;
            boolean shown = n < a.rewards.size();
            ui.set("#ChapRew" + j + ".Visible", shown);
            if (!shown) continue;
            Reward r = a.rewards.get(n);
            boolean xp = r.kind() == RewardKind.XP;
            ui.set("#ChapRewIcon" + j + ".Visible", !xp);
            if (!xp) ui.set("#ChapRewIcon" + j + ".ItemId", r.itemId());
            ui.set("#ChapRewXp" + j + ".Visible", xp);
            ui.set("#ChapRewQty" + j + ".Text", switch (r.kind()) {
                case XP -> String.valueOf(r.amount());
                case MONEY -> moneyShort(r.amount());
                case ITEM -> r.amount() > 1 ? "x" + r.amount() : "";
                case OEUF -> "?";
            });
            ui.set("#ChapRew" + j + ".TooltipText", r.label());
            ui.set("#ChapRewCheck" + j + ".Visible", s == State.DONE);
        }
        ui.set("#ChapTick0.Visible", a.rewards.size() >= 2);
        ui.set("#ChapTick1.Visible", a.rewards.size() >= 3);
        ui.set("#ChapRewPrev.Visible", rewardOffset > 0);
        ui.set("#ChapRewNext.Visible", rewardOffset + REWARDS < a.rewards.size());

        action(ui, s, a, me);
        // admin (2026-11-13) : retirer une quete de ce PNJ ; une quete a suite se retire depuis sa premiere quete (toute la serie)
        if (bountyBoard() && !plus) {                                    // tableau de prime : mise a prix et rachat (2026-11-13)
            com.eldanior.system.boards.BountyBoard.Wanted w = Primes.wantedOf(a.chain.id);
            if (w != null) {
                boolean self = w.player() != null && w.player().equals(me);
                ui.set("#ChapActionBox.Visible", true);
                ui.set("#ChapAlt.Visible", true);
                if (self) {                                              // sa propre tete : rachat (2 fois la prime)
                    long cost = w.bounty() * com.eldanior.system.boards.BountyBoard.BUYBACK_FACTOR;
                    ui.set("#ChapAction.Visible", false);
                    ui.set("#ChapAlt.Text", bountyArmed ? "Confirmer : payer " + CoinWallet.format(cost) : "Racheter ma prime");
                    if (hint == null) hint = "C'est votre tête. Racheter votre prime coûte " + CoinWallet.format(cost)
                            + " (deux fois son montant), pris dans vos pièces puis votre compte.";
                } else {                                                 // ajouter de l'or a sa prime
                    long cost = bountyCost(bountyAdd);
                    ui.set("#ChapAlt.Text", bountyArmed ? "Confirmer (" + CoinWallet.format(cost) + ")" : "Mettre à prix");
                    ui.set("#ChapFilter.Visible", true);
                    ui.set("#ChapFilter.Text", "Montant : " + CoinWallet.format(bountyAdd));
                    if (hint == null && bountyArmed) hint = "Ajouter " + CoinWallet.format(bountyAdd) + " à sa prime coûte "
                            + CoinWallet.format(cost) + " (dont 10 % au trésor de la ville). Cliquez encore pour confirmer.";
                }
            }
        } else if (GuildQuests.isMission(a) && s == State.IN_PROGRESS) {          // mission de guilde : abandon (2026-11-19)
            ui.set("#ChapActionBox.Visible", true);
            ui.set("#ChapAlt.Visible", true);
            ui.set("#ChapAlt.Text", "Abandonner");
        } else if (admin && !plus) {
            boolean first = a.number == 1 && removable(a),                         // affiches du jour, primes : pas de retrait
                    confirm = a.chain.id.equals(pendingRemoval);
            ui.set("#ChapActionBox.Visible", true);
            ui.set("#ChapAlt.Visible", first);
            if (first) ui.set("#ChapAlt.Text", confirm ? "Confirmer le retrait" : "Retirer cette quête");
            if (hint == null && confirm) hint = "Cliquez encore pour retirer « " + a.chain.title + " »"
                    + (a.chain.evolutive() ? " et ses " + a.chain.acts.size() + " quêtes" : "") + " de ce PNJ.";
            if (hint == null && !first) hint = "Pour retirer cette série, sélectionnez sa première quête.";
        }
        if (plus) {
            ui.set("#ChapActionBox.Visible", true);
            ui.set("#ChapAction.Text", "Ajouter cette quête");
            ui.set("#ChapAlt.Visible", cands.size() > 1);
            ui.set("#ChapAlt.Text", "Autre quête");
            ui.set("#ChapFilter.Visible", true);
            ui.set("#ChapFilter.Text", filterLabel());
            if (hint == null) hint = "Carte visible des seuls admins. "
                    + (a.chain.classes.isEmpty() ? "" : "Réservée à la classe " + a.chain.classLabel() + ". ")   // quete de classe
                    + (a.chain.cooldownMinutes() > 0 ? "Refaisable " + a.chain.cooldownMinutes() + " min après l'avoir rendue."
                    : a.chain.type == ChapterQuests.MissionType.PRINCIPALE ? PRINCIPALE_RULE : "Ne se fait qu'une fois.");
        }
        if (hint == null) hint = defaultHint(s, me, a);
        ui.set("#ChapHint.Text", hint != null ? hint : "");
    }

    /** Carte « + » (admin) : ajouter une quete a ce PNJ. */
    private void fillPlus(UICommandBuilder ui) {
        ui.set("#ChapRank.Visible", false);
        ui.set("#ChapMission.Visible", false);
        ui.set("#ChapTitle.Style.TextColor", "#f2c45e");
        ui.set("#ChapChain.Text", speaker);
        ui.set("#ChapTitle.Text", "Nouvelle quête");
        ui.set("#ChapTagline.Text", "Réservé aux admins");
        ui.set("#ChapStory.Text", "Les joueurs ne voient pas cette carte. Toutes les quêtes de la bibliothèque sont déjà "
                + "données par ce PNJ.");
        for (int i = 0; i < NOTES; i++) ui.set("#ChapNote" + i + ".Visible", false);
        ui.set("#ChapObjHead.Visible", false);
        for (int i = 0; i < OBJECTIVES; i++) ui.set("#ChapObj" + i + ".Visible", false);
        ui.set("#ChapRewards.Visible", false);
        action(ui, null, null, null);
        ui.set("#ChapHint.Text", "Toutes les quêtes de la bibliothèque sont déjà données par ce PNJ.");
    }

    /** Condition d'un acte, lisible (« Expert Martial »). */
    private static String condition(Act a) {
        return a.minTier > 0 ? com.eldanior.system.classes.definitions.luo.VoieMartiale.className(a.minTier) : "la quête précédente rendue";
    }

    /** « Disciple, rang Expert Martial » : l'adversaire demande par un attendu de duel. */
    /**
     * Quete principale (2026-11-13, rappel de l'utilisateur) : differente d'une exclusive, chaque joueur peut la faire une fois,
     * mais il n'y aura qu'un seul Roi.
     */
    static final String PRINCIPALE_RULE = "Chaque joueur peut la faire une fois, mais il n'y aura qu'un seul Roi.";

    /** Teintes des cadres de carte (= TINTS de gen_chapitres_ui.py). */
    private static final String[] TINTS = {"Or", "Bleu", "Vert", "Violet", "Rouge", "Orange", "Cyan", "Rose"};

    /** Teinte du contour d'une carte : principale ou rang S = or, secondaire = bleu, journaliere = vert, exclusive = violet. */
    private static String tint(Act a) {
        if (a == null) return null;
        if (a.chain.type == ChapterQuests.MissionType.PRIME) return "Rouge";            // contrats de prime (2026-11-13)
        if (a.chain.type == ChapterQuests.MissionType.CONTRAT) return "Orange";         // contrats de monstres
        if (a.chain.type == ChapterQuests.MissionType.GUILDE || a.chain.type == ChapterQuests.MissionType.MISSION_GUILDE)
            return "Rose";                                                             // tableau de guilde (2026-11-19)
        if (a.chain.type == ChapterQuests.MissionType.PRINCIPALE || a.rank.atLeastS()) return "Or";
        return switch (a.chain.type) {
            case SECONDAIRE -> "Bleu";
            case JOURNALIERE -> "Vert";
            case EXCLUSIVE -> "Violet";
            case MAISON -> "Orange";                                                   // quetes de la maison (2026-11-18)
            case CLASSE -> "Cyan";                                                    // quetes de classe (2026-11-16)
            default -> "Or";
        };
    }

    /** Nom du PNJ sans son rang (« Kenji Sama - Expert Martial » -> « Kenji Sama »). */
    private static String bare(String name) {
        int cut = name != null ? name.indexOf(" - ") : -1;
        return cut > 0 ? name.substring(0, cut) : name;
    }

    /** « Disciple » ou « Sensei », sans le rang (lignes d'attendus). */
    private static String noun(Objective o) { return "SENSEI".equals(o.target()) ? "Sensei" : "Disciple"; }

    private static String wanted(Objective o) {
        String rank = com.eldanior.system.classes.definitions.luo.VoieMartiale.className(o.rank());
        return ("SENSEI".equals(o.target()) ? "Sensei" : "Disciple") + " de rang " + rank;
    }

    /**
     * Lignes d'attendus : un duel contre plusieurs adversaires donne une ligne par adversaire (vaincu, designe, a trouver).
     * 2026-11-14 : le HUD des quetes affiche exactement ces lignes (QuestHud), comme la fenetre de quete.
     */
    public static List<Row> rows(Act a, State s, PlayerQuest pq, PlayerLevelData data, Player player) {
        String proposed = ChapterQuests.designated(pq);
        List<Row> out = new ArrayList<>();
        boolean done = s == State.DONE;
        for (Objective o : a.objectives) {
            if (o.kind() == Kind.DUEL_RANK) {
                List<String> beaten = ChapterQuests.beaten(pq);
                for (int k = 0; k < o.amount(); k++) {
                    if (done || k < beaten.size()) {
                        var c = k < beaten.size() ? com.eldanior.system.guards.MartialDuels.of(beaten.get(k)) : null;
                        out.add(new Row("Vaincu : " + (c != null ? bare(c.name()) : noun(o)), "Vaincu", true));
                    } else if (k == beaten.size() && proposed != null) {
                        var c = com.eldanior.system.guards.MartialDuels.of(proposed);
                        // 2026-11-13 : ni lieu ni rang dans les attendus (le lieu est dit par le donneur)
                        out.add(new Row("Défier " + (c != null ? bare(c.name()) : noun(o)), "Désigné", false));
                    } else out.add(new Row("Vaincre un " + noun(o).toLowerCase(), "—", false));
                }
                continue;
            }
            int[] p = done ? new int[]{o.amount(), o.amount()} : ChapterQuests.progress(o, pq, data, player);
            boolean live = pq != null || o.kind() == Kind.ITEM || o.kind() == Kind.LEVEL || o.kind() == Kind.GUILDE;
            boolean ok = p[0] >= p[1] && (done || live);
            String count = done ? "Fait" : o.kind() == Kind.LEVEL ? "Niv. " + (data != null ? data.getLevel() : 0) + " / " + p[1]
                    : (live ? p[0] : 0) + " / " + p[1];
            out.add(new Row(o.label(), count, ok));
        }
        return out;
    }

    private void action(UICommandBuilder ui, State s, Act a, UUID me) {
        String label = null;
        if (s == State.AVAILABLE) label = "Accepter";
        else if (s == State.READY) label = "Rendre la quête";
        else if (s == State.IN_PROGRESS) {
            Objective c = a.counter();
            label = c != null && c.kind() == Kind.DUEL_RANK && (pq(me, a) == null || ChapterQuests.beaten(pq(me, a)).size() < c.amount())
                    ? "Trouver un adversaire" : c != null && c.kind() == Kind.DUEL_SENSEI ? "Défier le " + speaker.split(" ")[0] : "Suivre";
        }
        ui.set("#ChapActionBox.Visible", label != null);
        ui.set("#ChapAction.Visible", label != null || a == null);
        ui.set("#ChapAlt.Visible", false);
        ui.set("#ChapFilter.Visible", false);
        if (label != null) ui.set("#ChapAction.Text", label);
    }

    private String defaultHint(State s, UUID me, Act a) {
        String guild = GuildQuests.hint(me, a, s);                                     // tableau de guilde (2026-11-19)
        if (guild != null) return guild;
        String who = speaker.split(" ")[0];
        return switch (s) {
            case LOCKED -> "Quête verrouillée.";
            case AVAILABLE -> a.chain.type == ChapterQuests.MissionType.PRINCIPALE
                    ? "Acceptez la quête pour que vos efforts comptent. " + PRINCIPALE_RULE
                    : "Acceptez la quête pour que vos efforts comptent.";
            case IN_PROGRESS -> {
                Objective c = a.counter();
                if (c != null && c.kind() == Kind.DUEL_RANK)
                    yield "Demandez au " + who + " de vous désigner un adversaire, puis allez le défier en duel.";
                yield isTracked(me, a) ? "Quête suivie : vos attendus s'affichent à l'écran." : "Quête en cours.";
            }
            case READY -> "Tous les attendus sont remplis : rendez la quête au " + who + ".";
            case DONE -> a.chain.cooldownMinutes() > 0
                    ? "Quête rendue. Elle pourra être refaite dans " + minutes(ChapterQuests.cooldownLeft(ChapterQuests.owned(me, a), a)) + "."
                    : a.number < a.chain.acts.size() ? "Quête terminée. La suivante se débloque avec le rang demandé." : "Quête achevée.";
        };
    }

    private static PlayerQuest pq(UUID me, Act a) { return ChapterQuests.owned(me, a); }

    private static String minutes(long ms) {
        long m = (ms + 59_999) / 60_000;
        return m >= 120 ? (m / 60) + " h" : m + " min";
    }

    /** La quete precedente est-elle rendue (ou n'y en a-t-il pas) ? */
    private static boolean previousDone(UUID me, Act a) {
        if (a.number <= 1) return true;
        PlayerQuest prev = ChapterQuests.owned(me, a.chain.acts.get(a.number - 2));
        return prev != null && prev.isCompleted();
    }

    private static boolean isTracked(UUID me, Act a) {
        PlayerQuest pq = ChapterQuests.owned(me, a);
        return pq != null && pq.isActive();
    }

    private static String stateLabel(State s, UUID me, Act a) {
        return switch (s) {
            case LOCKED -> "Verrouillée";
            case AVAILABLE -> "Disponible";
            case IN_PROGRESS -> isTracked(me, a) ? "Suivi" : "En cours";
            case READY -> "À rendre";
            case DONE -> ChapterQuests.cooldownLeft(ChapterQuests.owned(me, a), a) > 0
                    ? "De nouveau dans " + minutes(ChapterQuests.cooldownLeft(ChapterQuests.owned(me, a), a)) : "Terminée";
        };
    }

    private static String stateColor(State s) {
        return switch (s) {
            case LOCKED -> LOCKED_RED;
            case AVAILABLE -> "#d1dae6";
            case IN_PROGRESS -> "#96a9be";
            case READY -> "#ffe98a";
            case DONE -> "#7ed47e";
        };
    }

    static String artId(ChapterQuests.Art art) {
        String n = art.name().toLowerCase();
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }

    /** « 150 » cuivres -> « x2 » sous l'icone de la plus grosse piece. */
    private static String moneyShort(long copper) {
        long unit = com.eldanior.system.shop.CoinWallet.COINS.getOrDefault(
                com.eldanior.system.shop.CoinWallet.coinFor(copper), 1L);
        return "x" + (copper / unit);
    }

    // ==================== ACTIONS ====================

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ChapEventData data) {
        try {
            handle(ref, store, data);
        } catch (Throwable e) {
            EldaniorLogger.error("[Chapitres] action " + data.action, e);
        }
    }

    private void handle(Ref<EntityStore> ref, Store<EntityStore> store, ChapEventData d) {
        if ("close".equals(d.action)) { close(); return; }
        UUID me = uuid(ref, store);
        PlayerLevelData pdata = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        Player player = store.getComponent(ref, Player.getComponentType());
        List<Act> acts = acts(me, pdata, player);
        int selected = selectedIndex(acts, me, pdata, player);
        String hint = null;
        if (!"alt".equals(d.action)) pendingRemoval = null;          // toute autre action annule la confirmation du retrait
        switch (d.action == null ? "" : d.action) {
            case "card" -> {
                bountyArmed = false;                                                  // confirmation de mise a prix : par carte
                int i;
                try { i = Integer.parseInt(d.index); } catch (Exception e) { return; }
                int n = cardOffset + i;
                if (n < acts.size() && n != selected) { selectedId = id(acts.get(n)); rewardOffset = 0; }
            }
            case "prev" -> { if (cardOffset > 0) cardOffset--; }
            case "next" -> { if (cardOffset + CARDS < acts.size()) cardOffset++; }
            case "rprev" -> { if (rewardOffset > 0) rewardOffset--; }
            case "rnext" -> rewardOffset++;
            case "filter" -> {
                if (bountyBoard()) {                                             // montant de la mise a prix suivant
                    int i = 0;
                    while (i < BOUNTY_STEPS.length && BOUNTY_STEPS[i] != bountyAdd) i++;
                    bountyAdd = BOUNTY_STEPS[(i + 1) % BOUNTY_STEPS.length];
                    bountyArmed = false;
                } else if (admin) nextFilter();
            }
            case "alt" -> {
                if (selected < 0 || selected >= acts.size()) return;
                Act cur = acts.get(selected);
                if (cur == null) { plusChoice++; break; }
                if (GuildQuests.isMission(cur) && ChapterQuests.state(me, cur, pdata, player) == State.IN_PROGRESS) {
                    QuestManager.abandonQuest(me, cur.id);                                // mission de guilde abandonnee
                    if (pdata != null) save(ref, store, EldaniorSystem.get().getPlayerLevelDataType(), pdata, me);
                    hint = "Mission abandonnée : « " + cur.name + " ».";
                    break;
                }
                if (bountyBoard()) {                                                  // mise a prix ou rachat (deux clics)
                    if (!bountyArmed) { bountyArmed = true; break; }
                    bountyArmed = false;
                    hint = bountyAction(ref, store, cur);
                    break;
                }
                if (!removable(cur)) return;
                if (!admin || cur.number != 1 || postId == null || cur.chain.type == ChapterQuests.MissionType.PRIME) return;   // retrait : admin, 1re quete
                if (!cur.chain.id.equals(pendingRemoval)) { pendingRemoval = cur.chain.id; break; }
                pendingRemoval = null;
                com.eldanior.system.guards.NpcServices.unassignChain(postId, cur.chain.id, cur.chain.giver.equals(giver));
                selectedId = null;
                cardOffset = 0;
                rewardOffset = 0;
                hint = "« " + cur.chain.title + " » retirée de ce PNJ" + (cur.chain.evolutive() ? ", avec toute sa série." : ".");
            }
            case "main" -> {
                if (selected < 0 || selected >= acts.size()) return;
                if (acts.get(selected) == null) {                          // carte « + » : ajoute la quete proposee a ce PNJ
                    if (!admin) return;
                    List<ChapterQuests.Chain> cands = cands();
                    if (cands.isEmpty() || postId == null) return;
                    ChapterQuests.Chain c = cands.get(Math.floorMod(plusChoice, cands.size()));
                    com.eldanior.system.guards.NpcServices.assignChain(postId, c.id);
                    plusChoice = 0;
                    hint = "« " + c.title + " » ajoutée à ce PNJ.";
                } else hint = main(ref, store, acts.get(selected));
            }
            default -> { return; }
        }
        UICommandBuilder ui = new UICommandBuilder();
        fill(ui, ref, store, hint);
        sendUpdate(ui);
    }

    /** Bouton principal : accepter, defier, suivre ou rendre l'acte choisi. Renvoie un message pour la ligne d'aide. */
    private String main(Ref<EntityStore> ref, Store<EntityStore> store, Act a) {
        UUID me = uuid(ref, store);
        if (me == null) return null;
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (data == null || player == null) return null;
        State s = ChapterQuests.state(me, a, data, player);
        switch (s) {
            case AVAILABLE -> {
                String refused = QuestManager.refusal(me, a.id);               // une seule quete a la fois (hors principale)
                if (refused == null) refused = HouseQuests.refusal(me, data, a);  // quete de la maison : places, membres
                if (refused == null) refused = GuildQuests.refusal(me, a);         // tableau de guilde : guilde, 2 missions
                if (refused != null) return refused;
                if (a.chain.type == ChapterQuests.MissionType.PRIME) {           // contrat : 5 % de la prime, prime +20 %
                    String no = Primes.take(me, player, data, a);
                    if (no != null) return no;
                }
                PlayerQuest old = ChapterQuests.owned(me, a);                 // quete refaisable : on repart de zero
                if (old != null) QuestManager.getPlayerQuests(me).remove(old);
                QuestManager.acceptQuest(me, a.id);
                QuestManager.activateQuest(me, a.id);
                save(ref, store, type, data, me);
                return "« " + a.name + " » acceptée.";
            }
            case IN_PROGRESS -> {
                if (ChapterQuests.owned(me, a) == null) QuestManager.acceptQuest(me, a.id);   // quete de guilde : suivie sans l'avoir prise
                QuestManager.activateQuest(me, a.id);
                save(ref, store, type, data, me);
                Objective c = a.counter();
                if (c != null && c.kind() == Kind.DUEL_SENSEI) {                 // « Vaincre le Sensei » : duel tout de suite
                    String refused = com.eldanior.system.guards.Sensei.duelFromQuest(postId, ref, store);
                    if (refused != null) return refused;
                    close();
                    return null;
                }
                if (c == null || c.kind() != Kind.DUEL_RANK || ChapterQuests.beaten(pq(me, a)).size() >= c.amount())
                    return "Quête suivie : vos attendus s'affichent à l'écran.";
                return designate(ref, store, me, a, c);
            }
            case READY -> { return turnIn(ref, store, type, data, player, me, a); }
            default -> { return null; }
        }
    }

    /** Le donneur designe le PNJ poste le plus proche qui correspond (type, rang, pas encore vaincu). */
    private String designate(Ref<EntityStore> ref, Store<EntityStore> store, UUID me, Act a, Objective c) {
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        Vector3d from = t != null ? new Vector3d(t.getPosition()) : null;
        String world = store.getExternalData().getWorld() != null ? store.getExternalData().getWorld().getName() : null;
        var cand = ChapterQuests.propose(me, a, world, from);
        String who = speaker.split(" ")[0];
        PlayerQuest pq = ChapterQuests.owned(me, a);
        if (cand == null) {
            ChapterQuests.designate(pq, null);
            return who + " : « Je ne connais aucun " + wanted(c).toLowerCase() + " que tu n'aies pas déjà vaincu. Reviens plus tard. »";
        }
        ChapterQuests.designate(pq, cand.postId());                    // seul ce PNJ acceptera le duel (NpcServices)
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data != null) save(ref, store, EldaniorSystem.get().getPlayerLevelDataType(), data, me);
        return who + " : « Va trouver " + cand.name() + ", " + com.eldanior.system.guards.MartialDuels.placeName(cand.world(), cand.position())
                + ". Salue-le et demande-lui un duel. »";
    }

    private String turnIn(Ref<EntityStore> ref, Store<EntityStore> store, ComponentType<EntityStore, PlayerLevelData> type,
                          PlayerLevelData data, Player player, UUID me, Act a) {
        PlayerQuest pq = ChapterQuests.owned(me, a);
        if (a.chain.daily != null) {                                                    // affiche du jour : rendu de QuestManager
            boolean got = pq != null && com.eldanior.system.gui.tabs.QuestTab.claimQuest(a.id, ref, store);
            return got ? "Récompense reçue : " + a.name + "." : "Il vous manque des objets dans le sac.";
        }
        if (pq == null || !ChapterQuests.allMet(a, pq, data, player)) return "Il manque encore des attendus.";
        if (HouseQuests.ofAct(a) == null && a.chain.id.startsWith("maison_")) return "Le Patriarche a retiré cette quête.";
        var house = HouseQuests.ofAct(a);
        if (house != null && house.remaining <= 0) return "Toutes les places de cette quête ont déjà été rendues.";
        for (Objective o : a.objectives)                                               // objets apportes : retires
            if (o.kind() == Kind.ITEM && !QuestManager.removeItemsFromInventory(player, o.target(), o.amount()))
                return "Impossible de reprendre les objets demandés.";
        if (house != null && !HouseQuests.onTurnIn(a, player.getPlayerRef().getUsername())) return "Toutes les places de cette quête ont déjà été rendues.";
        pq.setCompleted();

        boolean guild = GuildQuests.isMission(a);                                      // mission de guilde : bonus de la guilde
        int oldLevel = data.getLevel();
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        for (Reward r : a.rewards) {
            switch (r.kind()) {
                case XP -> data.addExperience(guild ? GuildQuests.xp(me, (int) r.amount()) : (int) r.amount());
                case MONEY -> com.eldanior.system.shop.CoinWallet.receive(player, guild ? GuildQuests.gold(me, r.amount()) : r.amount());
                case ITEM -> give(ref, store, player, new ItemStack(r.itemId(), (int) r.amount()));
                case OEUF -> {                                                          // oeuf tire au hasard (rare, epique, legendaire)
                    com.eldanior.system.familiers.Espece sp = com.eldanior.system.familiers.Espece.rollHighRarity(new java.util.Random());
                    give(ref, store, player, new ItemStack(sp.eggItem, 1));
                    if (pr != null) pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("Le Dresseur vous confie un œuf de " + sp.label
                            + " (" + sp.rarete.label + ") !").color(sp.rarete.color));
                }
            }
        }
        if (data.getLevel() > oldLevel) data.setAttributePoints(data.getAttributePoints() + (data.getLevel() - oldLevel) * 3);
        save(ref, store, type, data, me);

        if (pr != null) {
            com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr,
                    a.questLabel().toUpperCase() + " TERMINÉE", a.name, true);
            if (data.getLevel() > oldLevel)
                com.eldanior.system.Leveling.utils.NotificationHelper.showLevelUpTitle(pr, data.getLevel());
        }
        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, data);
        com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, data, pr);
        if (guild) return "« " + a.name + " » rendue." + GuildQuests.onTurnIn(me, a);

        // l'acte suivant, s'il est deja visible (rang atteint), devient la carte choisie
        if (a.number < a.chain.acts.size()) {
            Act next = a.chain.acts.get(a.number);
            if (ChapterQuests.state(me, next, data, player) != State.LOCKED) {
                selectedId = next.id;
                rewardOffset = 0;
                return "« " + a.name + " » rendue. " + next.questLabel() + " débloquée.";
            }
            return "« " + a.name + " » rendue. La suite se dévoilera quand vous en aurez le rang.";
        }
        return "« " + a.chain.title + " » achevée. Le " + speaker.split(" ")[0] + " vous salue.";
    }

    private static void save(Ref<EntityStore> ref, Store<EntityStore> store, ComponentType<EntityStore, PlayerLevelData> type,
                             PlayerLevelData data, UUID me) {
        data.setQuestData(QuestManager.serializePlayerQuests(me));
        store.putComponent(ref, type, data);
    }

    /** Donne un objet ; ce qui ne rentre pas tombe aux pieds du joueur. */
    private static void give(Ref<EntityStore> ref, Store<EntityStore> store, Player player, ItemStack stack) {
        var result = player.getInventory().getCombinedHotbarFirst().addItemStack(stack);
        ItemStack rest = result.getRemainder();
        if (rest == null || rest.isEmpty()) return;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return;
        var holder = ItemComponent.generateItemDrop(store, rest, new Vector3d(t.getPosition()), new Rotation3f(), 0, 0.2f, 0);
        if (holder != null) store.addEntity(holder, AddReason.SPAWN);
    }

    private static UUID uuid(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr == null) return null;
        try { return UUIDExtractor.getUUID(pr); } catch (Exception e) { return null; }
    }

    public static class ChapEventData {
        public static final BuilderCodec<ChapEventData> CODEC = BuilderCodec.builder(ChapEventData.class, ChapEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .build();
        public String action, index;
    }
}
