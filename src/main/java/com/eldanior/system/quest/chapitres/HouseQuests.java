package com.eldanior.system.quest.chapitres;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.chapitres.ChapterQuests.Act;
import com.eldanior.system.quest.chapitres.ChapterQuests.Art;
import com.eldanior.system.quest.chapitres.ChapterQuests.Chain;
import com.eldanior.system.quest.chapitres.ChapterQuests.Kind;
import com.eldanior.system.quest.chapitres.ChapterQuests.MissionType;
import com.eldanior.system.quest.chapitres.ChapterQuests.Objective;
import com.eldanior.system.quest.chapitres.ChapterQuests.Reward;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRegistry;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quetes de la maison (2026-11-18, demande de l'utilisateur : « depuis l'intendant le Patriarche peut creer des quetes pour
 * les joueurs ; c'est le Patriarche qui paie la recompense ») :
 *  - le Patriarche choisit une chasse (monstres d'une liste) ou une collecte (l'objet qu'il tient en main), la quantite, la
 *    recompense par joueur et le nombre de places ; la maison seule ou tout le monde ;
 *  - la recompense de toutes les places est retiree de son compte a la creation ; les places non prises lui sont rendues quand
 *    il retire la quete ;
 *  - les joueurs la prennent au TABLEAU DES QUETES (2026-11-18, 2e passe : « pas dans l'intendant ») ; une quete reservee a
 *    la maison n'y est montree qu'a ses membres ; les objets d'une collecte sont gardes pour le Patriarche, qui les recupere
 *    chez l'intendant.
 * Chaines creees a la volee (comme Primes) : ChapterQuests.act et QuestManager.getQuest les cherchent ici en repli.
 * Fichier eldanior_data/house_quests.properties.
 */
public final class HouseQuests {

    private HouseQuests() {}

    public static final String GIVER_PREFIX = "maison:";

    /** Chasses proposees au Patriarche : libelle, motif des identifiants de monstres (QuestManager.onMobKill), icone. */
    public static final List<String[]> HUNTS = List.of(
            new String[]{"Loups", "wolf", "Butin_Fourrure_Loup_Garou"}, new String[]{"Ours", "bear", "Butin_Coeur_Ours"},
            new String[]{"Sangliers", "boar", "Ingredient_Hide_Heavy"}, new String[]{"Squelettes", "skeleton", "Deco_Bone_Skulls"},
            new String[]{"Zombies", "zombie", "Deco_Bone_Pile"}, new String[]{"Araignées", "spider", "Butin_Patte_Araignee"},
            new String[]{"Gobelins", "goblin", "Butin_Dent_Gobelin"}, new String[]{"Trorks", "trork", "Butin_Couronne_Trork"},
            new String[]{"Scaraks", "scarak", "Butin_Mandibule_Scarak"}, new String[]{"Outlanders", "outlander", "Butin_Fourrure_Outlander"},
            new String[]{"Criminels", "criminel", "Objective_Bounty"}, new String[]{"Cerfs", "deer", "Ingredient_Hide_Light"});


    /** Une quete de la maison. kind : KILL ou ITEM ; remaining : places encore libres ; delivered : objets a recuperer. */
    public static final class HouseQuest {
        public final String id, family, title, target, targetLabel, message;
        public final UUID creator;
        public final String creatorName;
        public final Kind kind;
        public final int amount, places;
        public final long reward, createdAt;
        public final boolean membersOnly;
        public int remaining, delivered;
        final Chain chain;

        HouseQuest(String id, String family, UUID creator, String creatorName, String title, Kind kind, String target, String targetLabel,
                   int amount, long reward, int places, int remaining, boolean membersOnly, long createdAt, int delivered, String message) {
            this.id = id;
            this.family = family;
            this.creator = creator;
            this.creatorName = creatorName;
            this.title = title;
            this.kind = kind;
            this.target = target;
            this.targetLabel = targetLabel;
            this.amount = amount;
            this.reward = reward;
            this.places = places;
            this.remaining = remaining;
            this.membersOnly = membersOnly;
            this.createdAt = createdAt;
            this.delivered = delivered;
            this.message = message == null ? "" : message;
            this.chain = build();
        }

        private Chain build() {
            var f = FamilyManager.get(family);
            String house = f != null ? f.getDisplayName() : family;
            String what = kind == Kind.KILL ? "Abattre " + amount + " " + targetLabel.toLowerCase() : "Rapporter " + amount + " × " + targetLabel;
            Chain c = new Chain("maison_" + id, GIVER_PREFIX + family, title, null, MissionType.MAISON);
            c.act(title, kind == Kind.KILL ? Art.LISIERE : Art.HAMEAU, rank(reward), 0,
                    (message == null || message.isBlank() ? "" : "« " + message + " »\n\n") + "Le Patriarche " + creatorName + " fait appel "
                            + (membersOnly ? "aux membres de la maison " : "à tous, au nom de la maison ") + house
                            + ". La récompense est déjà consignée chez l'intendant.",
                    List.of(membersOnly ? "Réservée aux membres de la maison." : "Ouverte à tous.",
                            "Places restantes : " + remaining + " sur " + places + ".",
                            kind == Kind.ITEM ? "Les objets rapportés reviennent au Patriarche." : "Seules les mises à mort après l'acceptation comptent."),
                    List.of(new Objective(kind, target, amount, what)),
                    List.of(Reward.money(reward)));
            for (Act a : c.acts) a.model = new ChapterQuests.ActQuest(a);
            return c;
        }

        public Act act() { return chain.acts.get(0); }
    }

    private static final Map<String, HouseQuest> QUESTS = new ConcurrentHashMap<>();
    private static Path file;

    public static QuestDifficulty rankOf(long reward) { return rank(reward); }

    /** Rang de la quete selon la recompense par joueur (cuivres). */
    static QuestDifficulty rank(long reward) {
        if (reward < 1_000) return QuestDifficulty.F;
        if (reward < 5_000) return QuestDifficulty.E;
        if (reward < 20_000) return QuestDifficulty.D;
        if (reward < 100_000) return QuestDifficulty.C;
        if (reward < 500_000) return QuestDifficulty.B;
        if (reward < 2_000_000) return QuestDifficulty.A;
        return QuestDifficulty.S;
    }

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("house_quests.properties");
        load();
    }

    // ==================== LECTURE ====================

    public static List<HouseQuest> of(String familyId) {
        String fam = familyId == null ? "" : FamilyManager.migrateId(familyId);
        List<HouseQuest> out = new ArrayList<>();
        for (HouseQuest q : QUESTS.values()) if (q.family.equals(fam)) out.add(q);
        out.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
        return out;
    }

    public static HouseQuest get(String id) { return id == null ? null : QUESTS.get(id); }

    /** Actes d'une maison, pour la fenetre Chapitres (donneur « maison:<famille> »). */
    public static List<Act> acts(String giver) {
        if (giver == null || !giver.startsWith(GIVER_PREFIX)) return List.of();
        List<Act> out = new ArrayList<>();
        for (HouseQuest q : of(giver.substring(GIVER_PREFIX.length()))) out.add(q.act());
        return out;
    }

    /**
     * Quetes de maison affichees au tableau des quetes pour ce joueur : celles ouvertes a tous, et celles de sa maison ; sans
     * place libre, seulement s'il l'a deja prise (pour la rendre).
     */
    public static List<Act> boardActs(UUID me, PlayerLevelData data) {
        String fam = data != null && data.getNobleFamilyId() != null ? FamilyManager.migrateId(data.getNobleFamilyId()) : "";
        List<HouseQuest> all = new ArrayList<>(QUESTS.values());
        all.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
        List<Act> out = new ArrayList<>();
        for (HouseQuest q : all) {
            if (q.membersOnly && !q.family.equals(fam)) continue;
            if (q.remaining <= 0 && ChapterQuests.owned(me, q.act()) == null) continue;
            out.add(q.act());
        }
        return out;
    }

    static Act act(String actId) {
        if (actId == null || !actId.startsWith("chap_maison_")) return null;
        for (HouseQuest q : QUESTS.values()) if (q.act().id.equals(actId)) return q.act();
        return null;
    }

    public static com.eldanior.system.quest.QuestModel model(String questId) {
        Act a = act(questId);
        return a != null ? a.model : null;
    }

    static HouseQuest ofAct(Act a) {
        if (a == null || !a.chain.id.startsWith("maison_")) return null;
        return QUESTS.get(a.chain.id.substring("maison_".length()));
    }

    /** Pourquoi ce joueur ne peut pas prendre cette quete (null : il peut). */
    static String refusal(UUID me, PlayerLevelData data, Act a) {
        HouseQuest q = ofAct(a);
        if (q == null) return null;
        if (q.remaining <= 0) return "Toutes les places de cette quête sont prises.";
        if (me != null && me.equals(q.creator)) return "Vous ne pouvez pas prendre votre propre quête.";
        if (q.membersOnly && (data == null || !q.family.equals(FamilyManager.migrateId(data.getNobleFamilyId()))))
            return "Cette quête est réservée aux membres de la maison.";
        return null;
    }

    /** Rendu : une place de moins, objets gardes pour le Patriarche ; false s'il ne reste plus de place. */
    static boolean onTurnIn(Act a, String playerName) {
        HouseQuest q = ofAct(a);
        if (q == null) return true;
        synchronized (HouseQuests.class) {
            if (q.remaining <= 0) return false;
            q.remaining--;
            if (q.kind == Kind.ITEM) q.delivered += q.amount;
        }
        FamilyRegistry.log(q.family, playerName + " achève « " + q.title + " » : " + gold(q.reward) + " or versés");
        save();
        return true;
    }

    // ==================== GESTES DU PATRIARCHE ====================

    /** Cree une quete ; l'appelant a deja retire reward × places du compte du Patriarche. */
    public static HouseQuest create(String familyId, UUID creator, String creatorName, String title, Kind kind, String target, String targetLabel,
                                    int amount, long reward, int places, boolean membersOnly, String message) {
        String fam = FamilyManager.migrateId(familyId);
        String id = Long.toString(System.currentTimeMillis(), 36) + Integer.toString((int) (Math.random() * 1296), 36);
        HouseQuest q = new HouseQuest(id, fam, creator, creatorName, title, kind, target, targetLabel, amount, reward, places, places,
                membersOnly, System.currentTimeMillis(), 0, message);
        QUESTS.put(id, q);
        FamilyRegistry.log(fam, creatorName + " publie la quête « " + title + " » (" + places + " × " + gold(reward) + " or)");
        save();
        var f = FamilyManager.get(fam);                                                // annonce aux joueurs concernes
        String text = "[Quête de la maison " + (f != null ? f.getDisplayName() : fam) + "] « " + title + " » : " + gold(reward)
                + " or, au tableau des quêtes.";
        for (var pr : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            if (q.membersOnly) {
                var r = pr.getReference();
                PlayerLevelData d = r != null ? r.getStore().getComponent(r, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType()) : null;
                if (d == null || !fam.equals(FamilyManager.migrateId(d.getNobleFamilyId()))) continue;
            }
            pr.sendMessage(com.hypixel.hytale.server.core.Message.raw(text).color("#e8a050"));
        }
        return q;
    }

    /**
     * Retire la quete : rend le montant des places libres (renvoye, a verser au compte du Patriarche). Les joueurs qui l'avaient
     * acceptee ne pourront plus la rendre.
     */
    public static long withdraw(String id) {
        HouseQuest q = QUESTS.remove(id);
        if (q == null) return 0;
        long refund = q.reward * Math.max(0, q.remaining);
        FamilyRegistry.log(q.family, "La quête « " + q.title + " » est retirée" + (refund > 0 ? " : " + gold(refund) + " or rendus" : ""));
        save();
        return refund;
    }

    /** Objets livres recuperes par le Patriarche : rend le nombre a remettre et remet le compteur a zero. */
    public static int takeDelivered(String id) {
        HouseQuest q = QUESTS.get(id);
        if (q == null) return 0;
        int n;
        synchronized (HouseQuests.class) { n = q.delivered; q.delivered = 0; }
        if (n > 0) save();
        return n;
    }

    public static void restoreDelivered(String id, int n) {
        HouseQuest q = QUESTS.get(id);
        if (q == null || n <= 0) return;
        synchronized (HouseQuests.class) { q.delivered += n; }
        save();
    }

    static String gold(long v) {
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace(' ', ' ').replace(' ', ' ');
    }

    // ==================== FICHIER ====================

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties p = new Properties();
            for (HouseQuest q : QUESTS.values())
                p.setProperty("q." + q.id, String.join("\u0001", q.family, q.creator.toString(), q.creatorName, q.title, q.kind.name(), q.target,
                        q.targetLabel, String.valueOf(q.amount), String.valueOf(q.reward), String.valueOf(q.places), String.valueOf(q.remaining),
                        String.valueOf(q.membersOnly), String.valueOf(q.createdAt), String.valueOf(q.delivered), q.message));
            PersistenceUtils.writeAtomicWithBackup(file, p, "Eldanior - quetes des maisons (intendants)");
        } catch (Exception e) {
            EldaniorLogger.error("[QuetesMaison] sauvegarde", e);
        }
    }

    private static void load() {
        QUESTS.clear();
        if (file == null || !Files.exists(file)) return;
        try {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file)) { p.load(in); }
            for (String k : p.stringPropertyNames()) {
                if (!k.startsWith("q.")) continue;
                try {
                    String[] f = p.getProperty(k).split("\u0001", -1);
                    String id = k.substring(2);
                    QUESTS.put(id, new HouseQuest(id, f[0], UUID.fromString(f[1]), f[2], f[3], Kind.valueOf(f[4]), f[5], f[6],
                            Integer.parseInt(f[7]), Long.parseLong(f[8]), Integer.parseInt(f[9]), Integer.parseInt(f[10]),
                            Boolean.parseBoolean(f[11]), Long.parseLong(f[12]), Integer.parseInt(f[13]), f.length > 14 ? f[14] : ""));
                } catch (Exception e) {
                    EldaniorLogger.warn("[QuetesMaison] entree illisible : " + k);
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[QuetesMaison] chargement", e);
        }
    }
}
