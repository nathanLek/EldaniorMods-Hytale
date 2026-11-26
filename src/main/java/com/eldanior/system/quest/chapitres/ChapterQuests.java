package com.eldanior.system.quest.chapitres;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.luo.LuoClans;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.QuestType;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Quetes de PNJ presentees comme la fenetre Chapitres du jeu (2026-11-12, demande de l'utilisateur, test sur le Sensei).
 *
 * Une quete evolutive (Chain) se decoupe en actes (Act) : chaque acte est une carte du carrousel, avec son histoire, ses
 * explications, ses attendus (Objective), ses recompenses (Reward), son rang de mission (F a S) et sa condition (rang martial
 * minimal). Un acte n'est montre que si sa condition est remplie et l'acte precedent rendu (2026-11-13 : pas de carte
 * verrouillee) ; un PNJ sans acte a montrer ne propose pas de quetes.
 *
 * Chaque acte est aussi enregistre comme QuestModel (ActQuest) dans QuestManager : son etat vit dans les PlayerQuest du
 * joueur (sauvegarde existante : PlayerLevelData.questData), il apparait dans « Quetes en cours » et dans le HUD quand il
 * est suivi. Il n'apparait ni dans les quetes disponibles ni dans les quetes a rendre du menu : il se rend au PNJ.
 * Un acte n'a qu'un compteur (KILL, DUEL_SENSEI ou DUEL_RANK) : PlayerQuest.progress ; ITEM et LEVEL se lisent en direct.
 */
public final class ChapterQuests {

    private ChapterQuests() {}

    // ==================== MODELE ====================

    /** Theme d'illustration d'une carte (images Quest/ChapArt<Theme>*.png, scripts/quetes/gen_chapitres_ui.py). */
    /**
     * TRONE (2026-11-14) : salle du trone du Roi Fou, quete principale. DOJO, VENT, BRIGANDS, TRORK, SENSEI (2026-11-14) :
     * une illustration par etape des Epreuves du Dojo. Nouvelle quete : skill quest-creation (une image par etape).
     */
    public enum Art { AUBE, NUIT, DUEL, TRONE, DOJO, VENT, BRIGANDS, TRORK, SENSEI,
        /** Quetes des PNJ de metier (QuetesMetiers, 2026-11-14), une par etape. */
        LISIERE, MARAIS, FORGE, DUNES, OUTLANDERS, ARENE, VIDE, OMBRE,
        /** Quete du Roi (QueteRoi, 2026-11-14) : rumeurs, hameau envahi, foret d'azur, mine. */
        RUMEURS, HAMEAU, AZUR, MINE }

    /** Type de quete : titre des cartes, « Quete secondaire 1 » (2026-11-13). */
    public enum MissionType {
        PRINCIPALE("Quête principale", "#f2c45e"),
        SECONDAIRE("Quête secondaire", "#6fa8e8"),                         // 2026-11-13 : couleurs des contours de carte
        EXCLUSIVE("Quête exclusive", "#b47be8"),
        /**
         * 2026-11-16 : quete reservee a une classe (Chain.forClass) : seuls les joueurs de cette classe la voient et la prennent.
         * Une seule fois par joueur, comme l'exclusive. Cartes au contour cyan (TINTS de gen_chapitres_ui.py).
         */
        CLASSE("Quête de classe", "#5fd0e0"),
        JOURNALIERE("Quête journalière", "#7ed47e"),
        /** 2026-11-13 : contrats de prime (Primes), crees a la volee depuis les avis de recherche. */
        PRIME("Prime", "#e0574a"),
        /** 2026-11-13 : contrats de monstres d'elite du jour (boards.MonsterContracts), au tableau de prime. */
        CONTRAT("Contrat", "#e8a050"),
        /** 2026-11-18 : quetes publiees par le Patriarche d'une maison chez son intendant (HouseQuests), recompense payee par lui. */
        MAISON("Quête de la maison", "#e8a050"),
        /**
         * 2026-11-19 : tableau de guilde en cartes (GuildQuests) : contrat de la semaine commun a la guilde, et mission
         * individuelle du jour. Cartes au contour rose (teinte « Rose » de gen_chapitres_ui.py).
         */
        GUILDE("Quête de guilde", "#e46fb4"),
        MISSION_GUILDE("Mission de guilde", "#ec95c8");

        public final String label, color;
        MissionType(String label, String color) { this.label = label; this.color = color; }
    }

    /**
     * ITEM : objets apportes (retires au rendu). LEVEL : niveau. KILL : monstres. DUEL_SENSEI : vaincre le Sensei.
     * DUEL_RANK : vaincre en duel `amount` PNJ postes differents, du type `target` (GuardType : DISCIPLE, SENSEI) et du rang
     * martial `rank` (2 Disciple ... 6 Saint) ; le Sensei en designe un (propose), le joueur va le defier (service Duel).
     * progress = nombre de vaincus, PlayerQuest.extra = leurs postes.
     */
    /** DAILY : quete journaliere du tirage du jour (QuestManager), avancement et rendu par son propre systeme. */
    /** DUEL_PLAYER (2026-11-14) : duels gagnes contre des JOUEURS (duel/DuelManager), a ne pas confondre avec DUEL_RANK/SENSEI. */
    /** GUILDE (2026-11-19) : avancement de la guilde du joueur sur un contrat de la semaine (GuildQuests), target = son index. */
    public enum Kind { ITEM, LEVEL, KILL, DUEL_SENSEI, DUEL_RANK, DUEL_PLAYER, PRIME, DAILY, GUILDE }

    public record Objective(Kind kind, String target, int amount, String label, int rank) {
        public Objective(Kind kind, String target, int amount, String label) { this(kind, target, amount, label, 0); }
        public boolean counted() {
            return kind == Kind.KILL || kind == Kind.DUEL_SENSEI || kind == Kind.DUEL_RANK || kind == Kind.DUEL_PLAYER || kind == Kind.PRIME;
        }
    }

    /** OEUF : oeuf de familier tire au hasard au rendu (rare, epique ou legendaire : familiers.Espece.rollHighRarity). */
    public enum RewardKind { ITEM, XP, MONEY, OEUF }

    /** Recompense : objet (id, quantite), XP, ou argent (en cuivres, donne en pieces). */
    public record Reward(RewardKind kind, String itemId, long amount, String label) {
        public static Reward item(String id, int qty, String label) { return new Reward(RewardKind.ITEM, id, qty, label); }
        public static Reward egg() {
            return new Reward(RewardKind.OEUF, "Eldanior_Oeuf_Mystere", 1, "Un œuf de bête rare, épique ou légendaire, tiré au hasard");
        }
        public static Reward xp(int xp) { return new Reward(RewardKind.XP, null, xp, xp + " points d'expérience"); }
        public static Reward money(long copper) {
            return new Reward(RewardKind.MONEY, com.eldanior.system.shop.CoinWallet.coinFor(copper), copper,
                    com.eldanior.system.shop.CoinWallet.format(copper));
        }
    }

    public static final class Act {
        public final Chain chain;
        public final int number;
        public final String id, name, story;
        public final List<String> notes;
        public final List<Objective> objectives;
        public final List<Reward> rewards;
        public final Art art;
        public final QuestDifficulty rank;
        /** Condition pour voir l'acte : rang martial minimal (1 Apprenti ... 8 Dieu Martial), 0 = aucune. */
        public final int minTier;
        ActQuest model;

        Act(Chain chain, int number, String name, Art art, QuestDifficulty rank, int minTier, String story, List<String> notes,
            List<Objective> objectives, List<Reward> rewards) {
            this.chain = chain;
            this.number = number;
            this.id = chain.daily != null ? chain.daily.getId() : "chap_" + chain.id + "_" + number;   // journaliere : son id
            this.name = name;
            this.art = art;
            this.rank = rank;
            this.minTier = minTier;
            this.story = story;
            this.notes = notes;
            this.objectives = objectives;
            this.rewards = rewards;
        }

        public String actLabel() { return "Acte " + roman(number); }

        /** Titre de la carte et du detail (2026-11-13) : type de quete et numero, « Quete secondaire 1 ». */
        public String questLabel() { return chain.evolutive() ? chain.type.label + " " + number : chain.type.label; }

        public Objective counter() {
            for (Objective o : objectives) if (o.counted()) return o;
            return null;
        }

        /** Condition remplie (rang martial). */
        public boolean eligible(PlayerLevelData data) {
            return minTier <= 0 || VoieMartiale.tier(data) >= minTier;
        }
    }

    public static final class Chain {
        public final String id, giver, title, emblemItem;
        public final MissionType type;
        public final List<Act> acts = new ArrayList<>();
        /**
         * Minutes avant de pouvoir la refaire une fois rendue ; 0 = jamais (exclusive, principale). Principale (rappel du
         * 2026-11-13) : une fois par joueur, mais un seul Roi au bout ; regle de la couronne a definir avec l'utilisateur.
         */
        int cooldownMinutes;
        /** Quete journaliere du tirage du jour presentee en carte (Journalieres) : l'acte porte son identifiant. */
        QuestModel daily;

        Chain(String id, String giver, String title, String emblemItem, MissionType type) {
            this.id = id;
            this.giver = giver;
            this.title = title;
            this.emblemItem = emblemItem;
            this.type = type;
        }

        Chain repeat(int minutes) { this.cooldownMinutes = minutes; return this; }

        /** Niveau minimum pour voir et prendre la quete (2026-11-14 : quete du Roi, niveau 250). */
        int minLevel;
        /** Quete (chaine) qu'il faut avoir commencee ou terminee pour voir celle-ci (2026-11-14 : temoins de la quete du Roi). */
        String requires;

        Chain minLevel(int level) { this.minLevel = level; return this; }

        /**
         * Quete de classe (2026-11-16) : identifiants de classe (ClassModel.getId : "archer", "mousquetaire"...) qui y ont
         * droit ; vide = toutes. Les autres joueurs ne la voient pas (State.LOCKED).
         */
        final java.util.Set<String> classes = new java.util.LinkedHashSet<>();

        Chain forClass(String... classIds) {
            for (String c : classIds) if (c != null && !c.isBlank()) classes.add(c.toLowerCase(java.util.Locale.ROOT));
            return this;
        }

        /** Nom du groupe de classes affiche a la place de la liste (« artistes martiaux (voie martiale) »). */
        String classGroup;

        Chain forClassGroup(String label, String... classIds) {
            this.classGroup = label;
            return forClass(classIds);
        }

        /** Le joueur a-t-il la classe qu'il faut (toujours vrai hors quete de classe) ? */
        public boolean classAllowed(PlayerLevelData data) {
            if (classes.isEmpty()) return true;
            String id = data != null ? data.getPlayerClassId() : null;
            return id != null && classes.contains(id.toLowerCase(java.util.Locale.ROOT));
        }

        /** Noms des classes de la quete (« Archer, Mousquetaire »), pour la fiche. */
        public String classLabel() {
            if (classGroup != null) return classGroup;
            List<String> names = new ArrayList<>();
            for (String id : classes) {
                var m = com.eldanior.system.classes.ClassManager.get(id);
                names.add(m != null ? m.getDisplayName() : id);
            }
            return String.join(", ", names);
        }

        Chain requires(String chainId) { this.requires = chainId; return this; }

        public int cooldownMinutes() { return cooldownMinutes; }

        /** Quete evolutive = plus d'un acte (titre « Acte I » + nom en sous-titre) ; sinon le nom est le titre. */
        public boolean evolutive() { return acts.size() > 1; }

        Chain act(String name, Art art, QuestDifficulty rank, int minTier, String story, List<String> notes,
                  List<Objective> objectives, List<Reward> rewards) {
            acts.add(new Act(this, acts.size() + 1, name, art, rank, minTier, story, notes, objectives, rewards));
            return this;
        }
    }

    /** QuestModel d'un acte, pour la sauvegarde, le HUD et la liste des quetes en cours. RECOLTE : aucun crochet automatique. */
    public static final class ActQuest extends QuestModel {
        public final Act act;

        ActQuest(Act act) {
            super(act.id, act.chain.title + " — " + act.name, act.story, QuestType.RECOLTE,
                    act.chain.type == MissionType.PRINCIPALE ? QuestCategory.PRINCIPAL
                            : act.chain.type == MissionType.PRIME ? QuestCategory.PRIME : QuestCategory.SECONDAIRE,
                    act.rank, hudTarget(act), hudAmount(act), xpOf(act), 0, null, null, act.chain.giver);
            this.act = act;
        }

        @Override
        public String getObjectiveText() {
            Objective o = act.counter();
            if (o == null && !act.objectives.isEmpty()) o = act.objectives.get(0);
            return o != null ? o.label() : "Parler à " + act.chain.giver;
        }

        @Override
        public String getRewardText() {
            List<String> parts = new ArrayList<>();
            for (Reward r : act.rewards) parts.add(r.label());
            return String.join(" + ", parts);
        }

        private static String hudTarget(Act a) {
            for (Objective o : a.objectives) if (o.kind() == Kind.ITEM) return a.counter() == null ? o.target() : null;
            return null;
        }

        private static int hudAmount(Act a) {
            Objective c = a.counter();
            if (c != null) return c.amount();
            for (Objective o : a.objectives) if (o.kind() == Kind.ITEM) return o.amount();
            return 1;
        }

        private static int xpOf(Act a) {
            for (Reward r : a.rewards) if (r.kind() == RewardKind.XP) return (int) r.amount();
            return 0;
        }
    }

    // ==================== REGISTRE ====================

    private static final Map<String, Chain> CHAINS = new LinkedHashMap<>();
    private static final Map<String, Act> ACTS = new LinkedHashMap<>();

    /** Donneur des quetes de la bibliotheque : sans PNJ fixe, un admin les ajoute a un PNJ (carte « + »). */
    public static final String LIBRARY = "*";
    /** Donneur du tableau des quetes (2026-11-13) : quetes ajoutees au tableau par un admin, et tous les contrats de prime. */
    public static final String TABLEAU = "tableau";
    /** Donneur du tableau de prime (2026-11-13) : contrats de prime et contrats de monstres seulement (Primes). */
    public static final String PRIMES = "primes";
    /** Donneur du tableau de guilde (2026-11-19) : quetes et missions de guilde seulement (GuildQuests). */
    public static final String GUILDE = "guilde";

    static {
        register(senseiEpreuves());
        register(dresseurOeuf());
        for (Chain c : library()) register(c);
        for (Chain c : QuetesMetiers.all()) register(c);                             // PNJ de metier (2026-11-14)
    }

    private static void register(Chain c) {
        CHAINS.put(c.id, c);
        for (Act a : c.acts) {
            a.model = new ActQuest(a);
            ACTS.put(a.id, a);
        }
    }

    /** Modeles des actes, enregistres dans QuestManager.registerQuests. */
    public static List<QuestModel> models() {
        List<QuestModel> out = new ArrayList<>();
        for (Act a : ACTS.values()) out.add(a.model);
        return out;
    }

    public static boolean hasGiver(String giver) { return !chainsOf(giver).isEmpty(); }

    public static List<Chain> chainsOf(String giver) {
        List<Chain> out = new ArrayList<>();
        for (Chain c : CHAINS.values()) if (c.giver.equals(giver)) out.add(c);
        return out;
    }

    /** Tous les actes des quetes d'un donneur, plus celles qu'un admin a ajoutees a ce PNJ (postId), dans l'ordre. */
    public static List<Act> actsOf(String giver, String postId) {
        List<Act> out = new ArrayList<>();
        List<String> removed = com.eldanior.system.guards.NpcServices.removedChains(postId);   // retirees par un admin
        for (Chain c : chainsOf(giver)) if (!removed.contains(c.id) && !closed(c)) out.addAll(c.acts);
        for (String id : com.eldanior.system.guards.NpcServices.assignedChains(postId)) {
            Chain c = CHAINS.get(id);
            if (c != null && !c.giver.equals(giver) && !closed(c)) out.addAll(c.acts);
        }
        out.addAll(HouseQuests.acts(giver));                                          // quetes de la maison (2026-11-18)
        if (PRIMES.equals(giver)) out.addAll(Primes.acts());                          // primes et contrats (2026-11-13)
        return out;
    }

    // ==================== QUETE PRINCIPALE ET ROI (2026-11-13) ====================

    /**
     * La quete principale mene au trone : chaque joueur peut la faire une fois, mais il n'y a qu'un Roi. Une fois le Roi elu,
     * elle est fermee pour tout le monde (demande de l'utilisateur) : plus proposee par aucun PNJ ni par la carte « + », et
     * retiree des quetes en cours des joueurs. Elle rouvre si le trone redevient vacant.
     */
    public static boolean mainClosed() { return com.eldanior.system.titles.nobility.NobilityManager.getCurrentKingUUID() != null; }

    static boolean closed(Chain c) { return c.type == MissionType.PRINCIPALE && mainClosed(); }

    /** Retire les quetes principales non rendues de ce joueur (trone pris). Renvoie le nombre retire. */
    public static int purgeMain(UUID me) {
        if (me == null || !mainClosed()) return 0;
        List<PlayerQuest> list = QuestManager.getPlayerQuests(me);
        int before = list.size();
        list.removeIf(pq -> {
            Act a = ACTS.get(pq.getQuestId());
            return a != null && a.chain.type == MissionType.PRINCIPALE && !pq.isCompleted();
        });
        return before - list.size();
    }

    /** Roi elu : la quete principale disparait des quetes en cours de tous les joueurs en ligne (sauvegardees aussitot). */
    public static void onKingElected() {
        var type = com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType();
        for (var pr : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            try {
                var ref = pr.getReference();
                if (ref == null || !ref.isValid()) continue;
                var world = ref.getStore().getExternalData().getWorld();
                UUID me = com.eldanior.system.config.UUIDExtractor.getUUID(pr);
                Runnable job = () -> {
                    if (!ref.isValid() || purgeMain(me) == 0) return;
                    PlayerLevelData d = ref.getStore().getComponent(ref, type);
                    if (d == null) return;
                    d.setQuestData(QuestManager.serializePlayerQuests(me));
                    ref.getStore().putComponent(ref, type, d);
                    pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("L'Épopée d'un Roi est retirée de vos quêtes : le trône a trouvé son maître."));
                };
                if (world != null) world.execute(job); else job.run();
            } catch (Exception e) {
                com.eldanior.system.config.EldaniorLogger.error("[Chapitres] quete principale fermee", e);
            }
        }
    }

    /** Cartes du carrousel pour ce joueur : les actes qu'il voit (condition remplie, acte precedent rendu). */
    public static List<Act> visibleActs(String giver, String postId, UUID me, PlayerLevelData data, Player player) {
        List<Act> out = new ArrayList<>();
        for (Act a : actsOf(giver, postId)) if (state(me, a, data, player) != State.LOCKED) out.add(a);
        return out;
    }

    /**
     * Quetes qu'un admin peut encore ajouter a ce PNJ : toutes celles qu'il n'a pas deja (2026-11-14, demande de l'utilisateur :
     * secondaires et exclusives des autres donneurs comprises, pas seulement la bibliotheque), et ses propres quetes retirees.
     */
    public static List<Chain> addable(String giver, String postId) {
        List<String> have = com.eldanior.system.guards.NpcServices.assignedChains(postId);
        List<String> removed = com.eldanior.system.guards.NpcServices.removedChains(postId);
        List<Chain> out = new ArrayList<>();
        for (Chain c : CHAINS.values()) {
            boolean own = c.giver.equals(giver);
            if ((own ? removed.contains(c.id) : !have.contains(c.id)) && !closed(c)) out.add(c);
        }
        return out;
    }

    public static Chain chain(String id) { return CHAINS.get(id); }

    private static final java.util.regex.Pattern PLACE = java.util.regex.Pattern.compile("\\{lieu:([A-Za-z0-9_]+)\\}");

    /**
     * Textes d'une quete : « {lieu:<chaine>} » devient le lieu-dit du PNJ qui donne cette chaine (2026-11-14 : le donneur dit
     * pres de quel lieu vit le temoin suivant ; pas de distance ni de direction). Le PNJ est celui a qui un admin l'a ajoutee
     * (carte « + »).
     */
    public static String withPlaces(String text) {
        if (text == null || !text.contains("{lieu:")) return text;
        java.util.regex.Matcher m = PLACE.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(placeOf(m.group(1))));
        m.appendTail(sb);
        return sb.toString();
    }

    private static String placeOf(String chainId) {
        String place = com.eldanior.system.guards.NpcServices.placeOfChain(chainId);
        return place != null ? place : "un endroit que plus personne ne sait situer";
    }

    public static Act act(String id) {
        Act a = ACTS.get(id);
        if (a == null) a = HouseQuests.act(id);                                      // quetes des maisons (2026-11-18)
        if (a == null) a = GuildQuests.act(id);                                      // tableau de guilde (2026-11-19)
        return a != null ? a : Primes.act(id);                                       // contrats de prime (a la volee)
    }

    // ==================== QUETE DU SENSEI : LES EPREUVES DU DOJO ====================

    static final String BOKKEN = "Eldanior_Weapon_Sword_Katana_Bokken";

    /**
     * Epreuves du Dojo (2026-11-13, demandees par l'utilisateur) : une quete par palier de la voie martiale, recompensee par le
     * manuel du palier suivant (les manuels ne sont plus donnes aux promotions, RuneManager.grantClassRunes) :
     *   1 Apprenti : vaincre 3 disciples differents (rang Disciple) ;  2 Disciple : chasser des betes rapides qui fuient ;
     *   3 Expert : abattre une bande de criminels ;  4 Maitre : vaincre un disciple Grand Maitre et rapporter un butin de monstre
     *   du niveau de la promotion suivante (400 : Trork) ;  5 Grand Maitre : vaincre le Sensei en duel.
     * Textes dits par le Sensei.
     */
    /** Donneur des quetes du Dresseur de betes (NpcServices.chapterGiver : commercant de metier DRESSEUR). */
    public static final String DRESSEUR = "dresseur";
    /** Delai avant de refaire « Un Oeuf venu d'ailleurs » : 3 mois (test : 5 min, demande de l'utilisateur, 2026-11-13). */
    static final boolean DRESSEUR_TEST = true;
    static final int THREE_MONTHS_MINUTES = 90 * 24 * 60;

    /**
     * Quete secondaire du Dresseur de betes (2026-11-13) : rapporter une couronne de Trork, 3 seves spirituelles et 5 coeurs
     * d'ours contre un oeuf de bete rare, epique ou legendaire tire au hasard (Reward.egg). Refaisable tous les 3 mois.
     */
    private static Chain dresseurOeuf() {
        return new Chain("dresseur_oeuf", DRESSEUR, "Un Œuf venu d'ailleurs", "Eldanior_Oeuf_Mystere", MissionType.SECONDAIRE)
                .repeat(DRESSEUR_TEST ? REPEAT_TEST_MINUTES : THREE_MONTHS_MINUTES)
                .act("Un Œuf venu d'ailleurs", Art.AUBE, QuestDifficulty.B, 0,
                        "« Mes bêtes à vendre ? Des renards, des loups... de braves compagnons. Mais les vraies merveilles, "
                                + "les ours, les léopards des neiges, les tigres à dents de sabre, et même, murmure-t-on, le "
                                + "Fenrir, ne naissent pas dans un enclos. Leurs œufs, je les obtiens par des chemins que je ne "
                                + "dévoile pas, et ces chemins ont un prix. Rapporte-moi la couronne d'un chef trork, trois "
                                + "sèves spirituelles et cinq cœurs d'ours. Je te confierai un œuf. Lequel ? Même moi, je ne le "
                                + "saurai qu'en le sortant de sa caisse. »",
                        List.of("« Une bête rare le plus souvent, parfois épique... et, plus rarement encore, une légende. »",
                                "« Je ne reçois un tel œuf qu'une fois par saison : reviens dans trois mois. »"),
                        List.of(new Objective(Kind.ITEM, "Butin_Couronne_Trork", 1, "Rapporter une couronne de Trork"),
                                new Objective(Kind.ITEM, "Butin_Seve_Spirituelle", 3, "Rapporter 3 sèves spirituelles"),
                                new Objective(Kind.ITEM, "Butin_Coeur_Ours", 5, "Rapporter 5 cœurs d'ours")),
                        List.of(Reward.egg(), Reward.xp(1500)));
    }

    private static Chain senseiEpreuves() {
        // 2026-11-16 (demande de l'utilisateur) : la voie martiale est la quete de classe des artistes martiaux
        return new Chain("sensei_epreuves", "sensei", "Les Épreuves du Dojo", BOKKEN, MissionType.CLASSE)
                .forClassGroup("artistes martiaux (voie martiale)", VoieMartiale.classIds())
                .act("Les Disciples du Dojo", Art.DOJO, QuestDifficulty.F, 1,
                        "« Tu portes le titre d'apprenti, mais un titre ne pare aucun coup. Trois disciples s'entraînent "
                                + "dans ce dojo et sur les routes de l'Empire. Ils ont un rang d'avance sur toi. Bats-les, "
                                + "chacun à son tour, et je t'ouvrirai le manuel du corps renforcé. »",
                        List.of("« Demande-moi un adversaire : je te dirai où le trouver. Va le saluer et propose-lui un duel. »",
                                "« Trois disciples différents. Perdre n'est pas une honte : relève-toi et recommence. »"),
                        List.of(new Objective(Kind.DUEL_RANK, "DISCIPLE", 3, "Disciples Martiaux", 2)),
                        List.of(manual("RenforcementCorps", "Manuel : Renforcement corporel"), Reward.xp(300), Reward.money(100)))
                .act("La Chasse du Vent", Art.VENT, QuestDifficulty.E, 2,
                        "« Ton corps est plus solide, mais il est lent. Les bêtes des plaines, elles, ne t'attendront pas. "
                                + "Lièvres, cerfs, antilopes, renards : ils fuient au premier bruit. Rattrape-les. Le jour où "
                                + "ta lame les touchera avant qu'ils ne détalent, tu seras prêt pour le Pas léger. »",
                        List.of("« Approche contre le vent, et frappe avant qu'ils ne t'aient senti. »",
                                "« Huit bêtes. Peu m'importe lesquelles, tant qu'elles couraient plus vite que toi. »"),
                        List.of(new Objective(Kind.KILL, "rabbit|antelope|deer|fox", 8, "Abattre 8 bêtes rapides (lièvres, cerfs, antilopes, renards)")),
                        List.of(manual("PasLeger", "Manuel : Pas léger"), Reward.xp(800), Reward.money(300)))
                .act("La Bande des Collines", Art.BRIGANDS, QuestDifficulty.D, 3,
                        "« Une bande de brigands rançonne les voyageurs. Les gardes les cherchent depuis des semaines ; toi, "
                                + "tu sauras les sentir avant qu'ils ne te voient. Ce ne sont pas des disciples : ils ne "
                                + "retiendront pas leurs coups. Ne retiens pas les tiens. »",
                        List.of("« Les criminels vont rarement seuls : quand tu en vois un, cherche ses complices. »",
                                "« Cinq d'entre eux suffiront à briser la bande. »"),
                        List.of(new Objective(Kind.KILL, "criminel", 5, "Abattre 5 criminels")),
                        List.of(manual("SensMartial", "Manuel : Sens martial"), Reward.xp(1500), Reward.money(800)))
                .act("Le Grand Maître et le Trork", Art.TRORK, QuestDifficulty.C, 4,
                        "« Un Grand Maître ne se contente pas de vaincre des hommes. Va défier un disciple qui a atteint ce "
                                + "rang, puis prouve que tu tiens tête aux bêtes de ce monde : rapporte-moi dix défenses "
                                + "de Trork. Celui qui revient avec les deux mérite de lancer son épée d'aura. »",
                        List.of("« Les Trorks rôdent en bandes dans les terres sauvages ; arrache-leur leurs défenses. »",
                                "« Le Grand Maître ne te fera pas de cadeau. Moi non plus. »"),
                        List.of(new Objective(Kind.DUEL_RANK, "DISCIPLE", 1, "Disciple Grand Maître Martial", 5),
                                new Objective(Kind.ITEM, "Butin_Defense_Trork", 10, "Rapporter 10 défenses de Trork")),
                        List.of(manual("ProjectionEpeeAura", "Manuel : Projection d'épée d'Aura"), Reward.xp(3000), Reward.money(2000)))
                .act("L'Épreuve du Maître", Art.SENSEI, QuestDifficulty.B, 5,
                        "« Il ne reste qu'un adversaire que tu n'as pas affronté : moi. Prends le Bokken, et ne me ménage "
                                + "pas : je ne te ménagerai pas. Si tu me fais plier le genou, le manuel du vol sur l'épée de "
                                + "Ki est à toi. »",
                        List.of("« Demande-moi un duel quand tu te sentiras prêt. »",
                                "« Un duel loyal : celui qui tombe sous le dernier souffle a perdu. »"),
                        List.of(new Objective(Kind.DUEL_SENSEI, null, 1, "Vaincre le Sensei en duel")),
                        List.of(manual("VolEpeeKi", "Manuel : Vol sur épée de Ki"), Reward.xp(6000), Reward.money(5000)));
    }

    /**
     * Bibliotheque (2026-11-13, pour tester l'ajout par la carte « + ») : la 1re etape de la quete principale (les trois quetes
     * de test, loups / bois / gobelins, retirees le 2026-11-14). Principale et exclusive : une seule fois ; secondaire et
     * journaliere : refaisables apres un delai (24 h ou 48 h prevus, 5 min pour le test : REPEAT_TEST_MINUTES).
     */
    static final int REPEAT_TEST_MINUTES = 5;

    /** Bibliotheque : la quete du Roi et ses temoins (QueteRoi, 2026-11-14), ajoutes aux PNJ par un admin (carte « + »). */
    private static List<Chain> library() {
        return QueteRoi.all();
    }

    private static Reward manual(String key, String label) {
        return Reward.item(com.eldanior.system.skills.runes.spells.LuoManuels.PREFIX + key, 1, label);
    }

    // ==================== ADVERSAIRES (PNJ POSTES) ====================

    /** Type de PNJ vise par un attendu DUEL_RANK. */
    public static com.eldanior.system.guards.GuardType duelType(Objective o) {
        try { return com.eldanior.system.guards.GuardType.valueOf(o.target()); } catch (Exception e) { return null; }
    }

    /** Postes des adversaires deja vaincus pour cet acte (PlayerQuest.extra = « vaincus,... ;designe »). */
    public static List<String> beaten(PlayerQuest pq) {
        List<String> out = new ArrayList<>();
        String e = pq != null && pq.getExtra() != null ? pq.getExtra() : "";
        int cut = e.indexOf(';');
        for (String id : (cut >= 0 ? e.substring(0, cut) : e).split(",")) if (!id.isBlank()) out.add(id);
        return out;
    }

    /** Adversaire designe par le donneur pour cet acte (poste), null s'il n'y en a pas. */
    public static String designated(PlayerQuest pq) {
        String e = pq != null && pq.getExtra() != null ? pq.getExtra() : "";
        int cut = e.indexOf(';');
        return cut >= 0 && cut + 1 < e.length() ? e.substring(cut + 1) : null;
    }

    private static void store(PlayerQuest pq, List<String> beaten, String designated) {
        pq.setExtra(String.join(",", beaten) + (designated != null ? ";" + designated : ""));
    }

    public static void designate(PlayerQuest pq, String postId) { if (pq != null) store(pq, beaten(pq), postId); }

    /**
     * Duel avec ce PNJ (disciple) permis ? Seulement s'il est l'adversaire designe d'une quete en cours du joueur (2026-11-13 :
     * « activer la demande de duel avec le PNJ vise par la quete seulement si le joueur possede la quete »).
     */
    public static boolean canDuel(UUID me, String postId) {
        if (me == null || postId == null) return false;
        for (PlayerQuest pq : QuestManager.getPlayerQuests(me))
            if (!pq.isCompleted() && ACTS.containsKey(pq.getQuestId()) && postId.equals(designated(pq))) return true;
        return false;
    }

    /** Sous cette distance du joueur, un adversaire n'est designe que s'il n'y en a aucun plus loin (2026-11-13). */
    static final double NEAR_BLOCKS = 80.0;

    /**
     * Adversaire que le donneur designe : un PNJ poste du type et du rang demandes, pas encore vaincu, dans le meme monde
     * (null s'il n'y en a aucun). 2026-11-13 (demande de l'utilisateur) : plus le plus proche, mais un au hasard, de preference
     * a plus de NEAR_BLOCKS du joueur, pour lui faire parcourir la carte. Redemander garde le meme tant qu'il est valable.
     */
    public static com.eldanior.system.guards.MartialDuels.Candidate propose(UUID me, Act a, String world, org.joml.Vector3d from) {
        Objective o = a.counter();
        PlayerQuest pq = owned(me, a);
        if (o == null || o.kind() != Kind.DUEL_RANK || pq == null || pq.isCompleted()) return null;
        List<String> done = beaten(pq);
        List<com.eldanior.system.guards.MartialDuels.Candidate> far = new ArrayList<>(), near = new ArrayList<>();
        String kept = designated(pq);
        for (var c : com.eldanior.system.guards.MartialDuels.candidates(duelType(o), o.rank())) {
            if (done.contains(c.postId()) || (world != null && !world.equals(c.world()))) continue;
            if (c.postId().equals(kept)) return c;                       // deja designe et toujours valable
            (from != null && c.position().distance(from) < NEAR_BLOCKS ? near : far).add(c);
        }
        List<com.eldanior.system.guards.MartialDuels.Candidate> pool = far.isEmpty() ? near : far;
        return pool.isEmpty() ? null : pool.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(pool.size()));
    }

    // ==================== ETAT D'UN JOUEUR ====================

    public enum State { LOCKED, AVAILABLE, IN_PROGRESS, READY, DONE }

    public static PlayerQuest owned(UUID me, Act a) {
        if (me == null) return null;
        for (PlayerQuest pq : QuestManager.getPlayerQuests(me)) if (pq.getQuestId().equals(a.id)) return pq;
        return null;
    }

    /** LOCKED = non montre : acte precedent pas rendu, ou condition non remplie (un acte deja pris reste visible). */
    public static State state(UUID me, Act a, PlayerLevelData data, Player player) {
        State guild = GuildQuests.state(me, a);                                       // quete de guilde : etat de la guilde
        if (guild != null) return guild;
        PlayerQuest pq = owned(me, a);
        if (a.chain.daily != null) {                                                  // journaliere : regles de QuestManager
            if (pq == null) return me != null && QuestManager.getCooldownEnd(me, a.id) > System.currentTimeMillis() ? State.DONE : State.AVAILABLE;
            return pq.isCompleted() || allMet(a, pq, data, player) ? State.READY : State.IN_PROGRESS;
        }
        if (pq != null && pq.isCompleted()) return cooldownLeft(pq, a) > 0 || a.chain.cooldownMinutes <= 0 ? State.DONE : State.AVAILABLE;
        if (pq != null) return allMet(a, pq, data, player) ? State.READY : State.IN_PROGRESS;
        if (a.number > 1) {
            PlayerQuest prev = owned(me, a.chain.acts.get(a.number - 2));
            if (prev == null || !prev.isCompleted()) return State.LOCKED;
        }
        if (a.chain.minLevel > 0 && (data == null || data.getLevel() < a.chain.minLevel)) return State.LOCKED;
        if (!a.chain.classAllowed(data)) return State.LOCKED;                         // quete d'une autre classe (2026-11-16)
        if (a.chain.requires != null) {                                               // quete precedente commencee ou rendue
            Chain need = CHAINS.get(a.chain.requires);
            boolean started = false;
            if (need != null) for (Act n : need.acts) if (owned(me, n) != null) { started = true; break; }
            if (!started) return State.LOCKED;
        }
        return a.eligible(data) ? State.AVAILABLE : State.LOCKED;
    }

    /** Millisecondes avant de pouvoir refaire une quete rendue (0 : refaisable, ou jamais si cooldownMinutes = 0). */
    public static long cooldownLeft(PlayerQuest pq, Act a) {
        if (pq == null || !pq.isCompleted() || a.chain.cooldownMinutes <= 0) return 0;
        return Math.max(0, pq.getCompletedTime() + a.chain.cooldownMinutes * 60_000L - System.currentTimeMillis());
    }

    /** Avancement d'un attendu : [fait, requis]. */
    public static int[] progress(Objective o, PlayerQuest pq, PlayerLevelData data, Player player) {
        int have = switch (o.kind()) {
            case ITEM -> player != null ? QuestManager.countItemInInventory(player, o.target()) : 0;
            case LEVEL -> data != null ? data.getLevel() : 0;
            case KILL, DUEL_SENSEI, DUEL_RANK, DUEL_PLAYER, PRIME -> pq != null ? pq.getProgress() : 0;
            case GUILDE -> player != null ? GuildQuests.progress(player.getPlayerRef().getUuid(), o) : 0;   // avancement de sa guilde
            case DAILY -> {                                                             // minage, recolte : objets du sac
                QuestModel m = QuestManager.getQuest(o.target());
                if (pq != null && pq.isCompleted()) yield o.amount();
                if (m != null && (m.getType() == com.eldanior.system.quest.QuestType.MINAGE || m.getType() == com.eldanior.system.quest.QuestType.RECOLTE)
                        && player != null && m.getTargetId() != null) yield QuestManager.countItemInInventory(player, m.getTargetId());
                yield pq != null ? pq.getProgress() : 0;
            }
        };
        return new int[]{Math.min(have, o.amount()), o.amount()};
    }

    static boolean allMet(Act a, PlayerQuest pq, PlayerLevelData data, Player player) {
        for (Objective o : a.objectives) {
            int[] p = progress(o, pq, data, player);
            if (p[0] < p[1]) return false;
        }
        return true;
    }

    // ==================== CROCHETS ====================

    /** Un joueur a tue un monstre (QuestManager.onMobKill) : avance les actes KILL acceptes qui le visent. */
    public static void onMobKill(UUID me, String mobTypeId) {
        if (me == null || mobTypeId == null) return;
        String mob = mobTypeId.toLowerCase();
        bump(me, o -> {
            if (o.kind() != Kind.KILL || o.target() == null) return false;
            for (String t : o.target().toLowerCase().split("\\|"))
                if (t.equals("*") || !t.isBlank() && mob.contains(t)) return true;      // « * » : tout monstre (missions de guilde)
            return false;
        });
    }

    /** Un joueur a gagne un duel d'entrainement contre un Sensei (SenseiDuel). */
    /** Un joueur a gagne un duel contre un autre JOUEUR (DuelManager -> QuestManager.onDuelWin, 2026-11-14). */
    public static void onPlayerDuelWon(UUID me) {
        if (me == null) return;
        bump(me, o -> o.kind() == Kind.DUEL_PLAYER);
    }

    public static void onSenseiDuelWon(UUID me) {
        if (me == null) return;
        bump(me, o -> o.kind() == Kind.DUEL_SENSEI);
    }

    /**
     * Un joueur a gagne un duel contre le PNJ du poste postId (SenseiDuel) : les actes en cours qui demandent ce type et ce
     * rang, et ou il n'a pas encore ete vaincu, avancent. Renvoie un message d'avancement (null si rien n'a compte).
     */
    public static String onMartialDuelWon(UUID me, String postId) {
        if (me == null || postId == null) return null;
        var c = com.eldanior.system.guards.MartialDuels.of(postId);
        if (c == null) return null;
        var type = com.eldanior.system.guards.MartialDuels.typeOf(postId);
        String msg = null;
        for (PlayerQuest pq : QuestManager.getPlayerQuests(me)) {
            if (pq.isCompleted()) continue;
            Act a = ACTS.get(pq.getQuestId());
            Objective o = a != null ? a.counter() : null;
            if (o == null || o.kind() != Kind.DUEL_RANK || duelType(o) != type || o.rank() != c.tier()) continue;
            List<String> done = beaten(pq);
            if (done.contains(postId) || pq.getProgress() >= o.amount() || !postId.equals(designated(pq))) continue;   // l'adversaire designe
            done.add(postId);
            store(pq, done, null);                                     // vaincu : la designation est levee
            pq.addProgress(1);
            msg = pq.getProgress() + " / " + o.amount() + " pour « " + a.name + " »"
                    + (pq.getProgress() >= o.amount() ? " : retournez voir le Sensei" : "");
        }
        return msg;
    }

    private static void bump(UUID me, java.util.function.Predicate<Objective> match) {
        for (PlayerQuest pq : QuestManager.getPlayerQuests(me)) {
            if (pq.isCompleted()) continue;
            Act a = act(pq.getQuestId());                                            // quetes a la volee comprises (2026-11-18)
            Objective c = a != null ? a.counter() : null;
            if (c != null && match.test(c) && pq.getProgress() < c.amount()) {
                pq.addProgress(1);
                if (pq.getProgress() >= c.amount()) GuildQuests.onFilled(me, a);       // mission de guilde remplie : message
            }
        }
    }

    // ==================== OUTILS ====================

    public static String roman(int n) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n > 0 && n < r.length ? r[n] : String.valueOf(n);
    }
}
