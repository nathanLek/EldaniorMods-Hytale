package com.eldanior.system.quest;

import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestManager {

    private static final Map<String, QuestModel> allQuests = new LinkedHashMap<>();

    // Quetes actives par joueur (UUID -> liste de PlayerQuest)
    private static final Map<UUID, List<PlayerQuest>> playerQuests = new ConcurrentHashMap<>();

    // ==================== BOUNTY REGISTRY ====================

    public static class BountyInfo {
        public final UUID uuid;
        public final String name;
        public final int level;
        public final long bounty;

        public BountyInfo(UUID uuid, String name, int level, long bounty) {
            this.uuid = uuid;
            this.name = name;
            this.level = level;
            this.bounty = bounty;
        }
    }

    private static final Map<UUID, BountyInfo> activeBounties = new ConcurrentHashMap<>();

    public static void registerBounty(UUID uuid, String name, int level, long bounty) {
        if (bounty > 0) {
            activeBounties.put(uuid, new BountyInfo(uuid, name, level, bounty));
        } else {
            activeBounties.remove(uuid);
        }
    }

    public static void unregisterBounty(UUID uuid) {
        activeBounties.remove(uuid);
    }

    public static Collection<BountyInfo> getActiveBounties() {
        return activeBounties.values();
    }

    public static QuestDifficulty difficultyFromLevel(int level) {
        if (level >= 190) return QuestDifficulty.S;
        if (level >= 150) return QuestDifficulty.A;
        if (level >= 110) return QuestDifficulty.B;
        if (level >= 70) return QuestDifficulty.C;
        if (level >= 40) return QuestDifficulty.D;
        return QuestDifficulty.E;
    }

    public static long calculateBountyBonus(QuestDifficulty diff) {
        return switch (diff) {
            case EX -> 20000;
            case SS -> 15000;
            case S -> 12000;
            case A -> 5000;
            case B -> 2000;
            case C -> 500;
            case D -> 200;
            case E -> 100;
            case F -> 50;
        };
    }

    /** Retourne les quêtes filtrées par catégorie pour un joueur */
    public static List<QuestModel> getQuestsByCategory(UUID playerUUID, QuestCategory category, boolean isPK) {
        List<QuestModel> result = new ArrayList<>();
        List<PlayerQuest> owned = getPlayerQuests(playerUUID);
        Set<String> ownedIds = new HashSet<>();
        for (PlayerQuest pq : owned) ownedIds.add(pq.getQuestId());

        // Quêtes possédées de cette catégorie (en cours, pas complétées)
        for (PlayerQuest pq : owned) {
            if (pq.isCompleted()) continue;
            QuestModel model = getQuest(pq.getQuestId());
            if (model == null) continue;
            if (model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.isInfoOnly()) continue;
            if (model.getCategory() == category) result.add(model);
        }

        // Quêtes disponibles de cette catégorie
        List<QuestModel> available = getAvailableQuests(playerUUID, isPK);
        for (QuestModel quest : available) {
            if (quest.getCategory() == category) result.add(quest);
        }

        return result;
    }

    /** Toutes les quêtes en cours (active ou accepted) toutes catégories */
    public static List<PlayerQuest> getInProgressQuests(UUID playerUUID) {
        List<PlayerQuest> result = new ArrayList<>();
        for (PlayerQuest pq : getPlayerQuests(playerUUID)) {
            if (!pq.isCompleted()) {
                QuestModel model = getQuest(pq.getQuestId());
                if (model != null && !(model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.isInfoOnly())) {
                    result.add(pq);
                }
            }
        }
        return result;
    }

    /** Quêtes terminées en attente de claim */
    public static List<PlayerQuest> getClaimableQuests(UUID playerUUID) {
        List<PlayerQuest> result = new ArrayList<>();
        for (PlayerQuest pq : getPlayerQuests(playerUUID)) {
            if (pq.isCompleted()) {
                QuestModel model = getQuest(pq.getQuestId());
                if (model instanceof com.eldanior.system.quest.chapitres.ChapterQuests.ActQuest) continue; // acte rendu au PNJ : garde comme memoire
                if (model != null && !(model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.isInfoOnly())) {
                    result.add(pq);
                }
            }
        }
        return result;
    }

    /** Compte les quêtes disponibles + en cours par catégorie */
    public static int countByCategory(UUID playerUUID, QuestCategory category, boolean isPK) {
        if (category == QuestCategory.PRIME) return activeBounties.size();
        int count = 0;
        // Quêtes possédées par le joueur dans cette catégorie
        for (PlayerQuest pq : getPlayerQuests(playerUUID)) {
            QuestModel model = getQuest(pq.getQuestId());
            if (model != null && model.getCategory() == category && !pq.isCompleted()) count++;
        }
        // Les principales et secondaires ne s'obtiennent que via NPC — ne pas compter les "disponibles"
        if (category == QuestCategory.JOURNALIERE) {
            for (QuestModel quest : getAvailableQuests(playerUUID, isPK)) {
                if (quest.getCategory() == category) count++;
            }
        }
        return count;
    }

    // Repertoire de donnees pour la sauvegarde fichier (shutdown safety)
    private static Path dataDir;

    // Jour courant pour les journalieres
    private static volatile int currentDay = -1;

    // Selection aleatoire de 7 quetes journalieres pour aujourd'hui
    private static final int DAILY_SELECTION_COUNT = 5;
    private static volatile List<String> todaysDailyIds = List.of();

    public static void init() {
        registerQuests();
        currentDay = getDayOfYear();
        refreshDailySelection();
        System.out.println("[Eldanior] Systeme de Quetes initialise. " + allQuests.size() + " quetes, " + todaysDailyIds.size() + " journalieres du jour.");
    }

    private static void registerQuests() {
        // 2026-11-14 : quetes remises a zero (demande de l'utilisateur) ; les nouvelles se creent avec le skill quest-creation.
        // Principale - Histoire (chaine de dialogues : Ancien Conseiller -> Tavernier -> Naissance d'un Roi)
        register(new com.eldanior.system.quest.definitions.principal.MainQuest_NaissanceRoi());
        register(new com.eldanior.system.quest.definitions.secondaire.SecQuest_AncienConseiller());
        register(new com.eldanior.system.quest.definitions.secondaire.InfoQuest_Tavernier());

        // Journalieres : les 5 affiches du tableau des quetes (tout le tirage du jour)
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_TrorkWarrior());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SkeletonBurnt());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_CollectC2());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_ExploreB2());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_Legend());

        // Actes des quetes en chapitres (quest.chapitres, fenetre des PNJ facon Chapitres)
        for (QuestModel m : com.eldanior.system.quest.chapitres.ChapterQuests.models()) register(m);
    }

    private static void register(QuestModel quest) {
        allQuests.put(quest.getId(), quest);
    }

    // ==================== GETTERS ====================

    public static QuestModel getQuest(String id) {
        QuestModel q = allQuests.get(id);
        if (q != null || id == null) return q;
        var house = com.eldanior.system.quest.chapitres.HouseQuests.model(id);       // quetes des maisons (2026-11-18)
        if (house == null) house = com.eldanior.system.quest.chapitres.GuildQuests.model(id);   // tableau de guilde (2026-11-19)
        return house != null ? house : com.eldanior.system.quest.chapitres.Primes.model(id);   // contrats de prime (a la volee)
    }

    /**
     * Trouve la prochaine quete dialogue pour un NPC specifique.
     * Retourne la premiere quete non-completee de ce NPC.
     */
    public static QuestModel getNextDialogueForNpc(com.hypixel.hytale.server.core.universe.PlayerRef playerRef, String npcId) {
        UUID playerUUID = null;
        try {
            playerUUID = UUIDExtractor.getUUID(playerRef);
        } catch (Exception e) { EldaniorLogger.error("QuestManager", e); }

        List<PlayerQuest> owned = playerUUID != null ? getPlayerQuests(playerUUID) : List.of();
        Set<String> completedIds = new HashSet<>();
        for (PlayerQuest pq : owned) {
            if (pq.isCompleted()) completedIds.add(pq.getQuestId());
        }

        Set<String> ownedIds = new HashSet<>();
        for (PlayerQuest pq : owned) ownedIds.add(pq.getQuestId());

        long now = System.currentTimeMillis();

        for (QuestModel quest : allQuests.values()) {
            if (!npcId.equals(quest.getNpcGiverId())) continue;
            if (completedIds.contains(quest.getId())) continue;
            if (!(quest instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq)) continue;

            // Verifier cooldown
            if (quest.getCooldownMinutes() > 0 && playerUUID != null) {
                long cdEnd = getCooldownEnd(playerUUID, quest.getId());
                if (cdEnd > now) continue; // Encore en cooldown
            }

            // Si la quete a un prerequis (unlocksQuestId d'une autre quete),
            // verifier que le joueur l'a bien debloquee (= la quete prerequise est completee)
            // Chercher si une autre quete doit etre completee pour debloquer celle-ci
            boolean locked = false;
            for (QuestModel other : allQuests.values()) {
                if (other instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest otherNq) {
                    if (quest.getId().equals(otherNq.getUnlocksQuestId())) {
                        // "other" doit etre completee pour debloquer "quest"
                        if (!completedIds.contains(other.getId())) {
                            locked = true;
                        }
                        break;
                    }
                }
            }
            if (locked) continue;

            return quest;
        }
        return null;
    }
    public static Collection<QuestModel> getAllQuests() { return allQuests.values(); }

    // Cooldowns par joueur: questId -> timestamp de fin de cooldown
    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public static List<QuestModel> getAvailableQuests(UUID playerUUID) {
        return getAvailableQuests(playerUUID, false);
    }

    public static List<QuestModel> getAvailableQuests(UUID playerUUID, boolean isPK) {
        List<QuestModel> available = new ArrayList<>();
        List<PlayerQuest> owned = getPlayerQuests(playerUUID);
        Set<String> ownedIds = new HashSet<>();
        for (PlayerQuest pq : owned) ownedIds.add(pq.getQuestId());

        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();

        for (QuestModel quest : allQuests.values()) {
            if (ownedIds.contains(quest.getId())) continue;
            if (quest instanceof com.eldanior.system.quest.chapitres.ChapterQuests.ActQuest) continue; // se prennent au PNJ

            // Filtrer PK : quetes PK seulement pour les PK, quetes normales seulement pour non-PK
            if (quest.isPKOnly() && !isPK) continue;
            if (quest.isDaily() && !quest.isPKOnly() && isPK) continue;

            // Journalieres : seulement celles selectionnees aujourd'hui
            if (quest.isDaily() && !todaysDailyIds.contains(quest.getId())) continue;

            // Verifier cooldown
            if (quest.getCooldownMinutes() > 0) {
                Long cdEnd = playerCooldowns.get(quest.getId());
                if (cdEnd != null && now < cdEnd) continue; // Encore en cooldown
            }

            // Pour les principales: verifier la chaine
            if (quest.isMainStory() && !isMainQuestAvailable(quest.getId(), owned)) continue;

            available.add(quest);
        }
        return available;
    }

    /** Selectionne aleatoirement DAILY_SELECTION_COUNT quetes journalieres pour aujourd'hui.
     *  Le seed est base sur le jour, donc tous les joueurs voient la meme selection. */
    private static void refreshDailySelection() {
        // Pool normal (non-PK)
        List<String> normalDailies = new ArrayList<>();
        // Pool PK
        List<String> pkDailies = new ArrayList<>();

        for (QuestModel quest : allQuests.values()) {
            if (!quest.isDaily()) continue;
            if (quest.isPKOnly()) {
                pkDailies.add(quest.getId());
            } else {
                normalDailies.add(quest.getId());
            }
        }

        Random dayRandom = new Random(currentDay * 31L + 2026);

        List<String> newDailyIds = new ArrayList<>();

        // 5 quetes normales
        Collections.shuffle(normalDailies, dayRandom);
        int normalCount = Math.min(DAILY_SELECTION_COUNT, normalDailies.size());
        newDailyIds.addAll(normalDailies.subList(0, normalCount));

        // 5 quetes PK
        Collections.shuffle(pkDailies, dayRandom);
        int pkCount = Math.min(DAILY_SELECTION_COUNT, pkDailies.size());
        newDailyIds.addAll(pkDailies.subList(0, pkCount));

        // Remplacement atomique — les lecteurs voient l'ancienne ou la nouvelle liste, jamais un etat intermediaire
        todaysDailyIds = Collections.unmodifiableList(newDailyIds);

        System.out.println("[Quests] Journalieres du jour: " + todaysDailyIds);
    }

    public static List<String> getTodaysDailyIds() { return todaysDailyIds; }

    public static void setCooldown(UUID playerUUID, String questId, int minutes) {
        if (minutes <= 0) return;
        cooldowns.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
                .put(questId, System.currentTimeMillis() + (minutes * 60000L));
    }

    public static long getCooldownEnd(UUID playerUUID, String questId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        if (playerCooldowns == null) return 0;
        Long end = playerCooldowns.get(questId);
        return end != null ? end : 0;
    }

    public static String serializeCooldowns(UUID playerUUID) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        if (playerCooldowns == null || playerCooldowns.isEmpty()) return "";
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for (var entry : playerCooldowns.entrySet()) {
            if (entry.getValue() > now) { // Seulement les cooldowns actifs
                if (sb.length() > 0) sb.append("|");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    public static void deserializeCooldowns(UUID playerUUID, String data) {
        if (data == null || data.isEmpty()) return;
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        for (String entry : data.split("\\|")) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    long end = Long.parseLong(parts[1]);
                    if (end > now) { // Seulement si pas expiré
                        playerCooldowns.put(parts[0], end);
                    }
                } catch (NumberFormatException e) { /* format invalide */ }
            }
        }
    }

    private static boolean isMainQuestAvailable(String questId, List<PlayerQuest> owned) {
        if ("main_1".equals(questId)) return true; // Premiere quete toujours dispo
        // Chercher la quete precedente qui pointe vers celle-ci
        for (QuestModel q : allQuests.values()) {
            if (questId.equals(q.getNextQuestId())) {
                // La precedente doit etre completee
                for (PlayerQuest pq : owned) {
                    if (pq.getQuestId().equals(q.getId()) && pq.isCompleted()) return true;
                }
                return false;
            }
        }
        return true;
    }

    // ==================== PLAYER QUESTS ====================

    public static List<PlayerQuest> getPlayerQuests(UUID playerUUID) {
        return playerQuests.computeIfAbsent(playerUUID, k -> new CopyOnWriteArrayList<>());
    }

    /** Parcourt les quetes de tous les joueurs en memoire (listes modifiables : CopyOnWriteArrayList). */
    public static void forEachPlayerQuests(java.util.function.BiConsumer<UUID, List<PlayerQuest>> fn) {
        playerQuests.forEach(fn);
    }

    public static PlayerQuest getActiveQuest(UUID playerUUID) {
        for (PlayerQuest pq : getPlayerQuests(playerUUID)) {
            if (pq.isActive()) return pq;
        }
        return null;
    }

    /** Quetes en cours a la fois par joueur, hors quete principale (2026-11-13 : 1, puis 2 a la demande de l'utilisateur). */
    public static final int MAX_ACTIVE = 2;

    /**
     * Regle de l'utilisateur (2026-11-13) : au plus MAX_ACTIVE quetes en cours, plus la quete principale qui s'ajoute toujours.
     * Renvoie une des quetes en cours qui empechent d'accepter questId (null : rien ne l'empeche).
     */
    public static QuestModel blockingQuest(UUID playerUUID, String questId) {
        QuestModel model = getQuest(questId);
        if (model == null || model.isMainStory() || infoOnly(model)) return null;      // info : rendue aussitot
        if (com.eldanior.system.quest.chapitres.GuildQuests.isGuild(questId)) return null;   // tableau de guilde : sa propre limite
        int running = 0;
        QuestModel last = null;
        for (PlayerQuest pq : getPlayerQuests(playerUUID)) {
            if (pq.isCompleted() || pq.getQuestId().equals(questId)
                    || com.eldanior.system.quest.chapitres.GuildQuests.isGuild(pq.getQuestId())) continue;   // hors limite (2026-11-19)
            QuestModel other = getQuest(pq.getQuestId());
            if (other != null && !other.isMainStory() && !infoOnly(other)) { running++; last = other; }
        }
        return running >= MAX_ACTIVE ? last : null;
    }

    private static boolean infoOnly(QuestModel m) {
        return m instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.isInfoOnly();
    }

    /** Message de refus d'une quete (une seule a la fois), null si elle peut etre acceptee. */
    public static String refusal(UUID playerUUID, String questId) {
        QuestModel other = blockingQuest(playerUUID, questId);
        return other == null ? null
                : "Vous avez déjà " + MAX_ACTIVE + " quêtes en cours (dont « " + other.getName() + " »). Terminez-en ou abandonnez-en une d'abord (la quête principale s'ajoute toujours).";
    }

    public static boolean acceptQuest(UUID playerUUID, String questId) {
        QuestModel model = getQuest(questId);
        if (model == null) return false;

        List<PlayerQuest> quests = getPlayerQuests(playerUUID);
        for (PlayerQuest pq : quests) {
            if (pq.getQuestId().equals(questId)) return false; // Deja acceptee
        }
        if (blockingQuest(playerUUID, questId) != null) return false;            // une seule quete a la fois (hors principale)

        PlayerQuest pq = new PlayerQuest(questId);
        quests.add(pq);
        return true;
    }

    public static boolean activateQuest(UUID playerUUID, String questId) {
        List<PlayerQuest> quests = getPlayerQuests(playerUUID);
        // Desactiver l'ancienne
        for (PlayerQuest pq : quests) {
            if (pq.isActive()) pq.setAccepted();
        }
        // Activer la nouvelle
        for (PlayerQuest pq : quests) {
            if (pq.getQuestId().equals(questId) && !pq.isCompleted()) {
                pq.setActive();
                return true;
            }
        }
        return false;
    }

    public static void abandonQuest(UUID playerUUID, String questId) {
        List<PlayerQuest> quests = getPlayerQuests(playerUUID);
        quests.removeIf(pq -> pq.getQuestId().equals(questId) && !pq.isCompleted());
    }

    // ==================== PROGRESSION ====================

    /**
     * Fait avancer de amount toutes les quetes en cours du joueur que match accepte (2026-11-13 : 2 quetes en cours a la fois,
     * les deux avancent, plus seulement la quete suivie). Les quetes de PNJ a condition n'avancent pas par ces crochets.
     */
    private static void progressAll(UUID playerUUID, java.util.function.Predicate<QuestModel> match, int amount) {
        if (playerUUID == null || amount <= 0) return;
        for (PlayerQuest pq : getPlayerQuests(playerUUID)) {
            if (pq.isCompleted()) continue;
            QuestModel model = getQuest(pq.getQuestId());
            if (model == null || model instanceof com.eldanior.system.quest.chapitres.ChapterQuests.ActQuest) continue;   // leurs propres crochets
            if (model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.getCompletionCondition() != null) continue;
            if (!match.test(model)) continue;
            pq.addProgress(amount);
            checkCompletion(playerUUID, pq, model);
        }
    }

    /** Appele quand un mob est tue : quetes de massacre et de chasse en cours. */
    public static void onMobKill(UUID playerUUID, String mobTypeId) {
        com.eldanior.system.quest.chapitres.ChapterQuests.onMobKill(playerUUID, mobTypeId);
        String mob = mobTypeId != null ? mobTypeId.toLowerCase() : "";
        progressAll(playerUUID, m -> m.getType() == QuestType.MASSACRE
                || m.getType() == QuestType.CHASSE && m.getTargetId() != null && mob.contains(m.getTargetId().toLowerCase()), 1);
    }

    public static void onChestDiscovered(UUID playerUUID) {
        progressAll(playerUUID, m -> m.getType() == QuestType.EXPLORATION, 1);
    }

    public static void onDuelWin(UUID playerUUID) {
        com.eldanior.system.quest.chapitres.ChapterQuests.onPlayerDuelWon(playerUUID);       // cartes : Kind.DUEL_PLAYER
        progressAll(playerUUID, m -> m.getType() == QuestType.DUEL, 1);
    }

    /** Or gagne : quetes de collection en cours (progression du montant gagne, pas du total). */
    public static void onGoldGained(UUID playerUUID, long amountGained) {
        progressAll(playerUUID, QuestManager::isMoneyQuest, (int) Math.min(Integer.MAX_VALUE, amountGained));
    }

    /**
     * Quetes d'argent liees a la banque (2026-11-14, demande de l'utilisateur : les pieces ne se convertissent plus qu'au
     * banquier) : elles avancent avec l'argent verse au compte (depot de pieces chez le banquier, primes touchees) et
     * reculent d'autant a chaque retrait (pas de boucle retirer / redeposer). Les prets ne comptent pas.
     */
    public static void onBankDeposit(UUID playerUUID, long amount) { onGoldGained(playerUUID, amount); }

    public static void onBankWithdraw(UUID playerUUID, long amount) {
        if (playerUUID == null || amount <= 0) return;
        for (PlayerQuest pq : getPlayerQuests(playerUUID)) {
            if (pq.isCompleted() || !isMoneyQuest(getQuest(pq.getQuestId()))) continue;
            pq.addProgress(-(int) Math.min(pq.getProgress(), amount));
        }
    }

    /** Quete d'argent : type COLLECTION, hors dialogues d'information (talk_tavernier... ont aussi ce type). */
    private static boolean isMoneyQuest(QuestModel m) {
        return m != null && m.getType() == QuestType.COLLECTION
                && !(m instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.isInfoOnly());
    }

    public static void onPKKilled(UUID killerUUID, UUID pkUUID) {
        progressAll(killerUUID, m -> m.getType() == QuestType.EXECUTION, 1);
    }

    // MINAGE/RECOLTE : la progression est basee sur l'inventaire du joueur.
    // Pas de hook auto — le NPC verifie et retire les items au claim.

    /**
     * Compte le nombre total d'un item dans l'inventaire du joueur (hotbar + storage + backpack).
     * Utilise un match partiel (contains) pour supporter les targetId comme "ore_iron"
     * qui matchent des items comme "Ore_Iron_Stone".
     */
    public static int countItemInInventory(com.hypixel.hytale.server.core.entity.entities.Player player, String itemId) {
        if (player == null || itemId == null) return 0;
        int count = 0;
        String lower = itemId.toLowerCase();
        var inv = player.getInventory();
        for (short i = 0; i < 9; i++) {
            var item = inv.getHotbar().getItemStack(i);
            if (item != null && !item.isEmpty() && matchesItemId(item.getItemId(), lower)) {
                count += item.getQuantity();
            }
        }
        for (short i = 0; i < 27; i++) {
            var item = inv.getStorage().getItemStack(i);
            if (item != null && !item.isEmpty() && matchesItemId(item.getItemId(), lower)) {
                count += item.getQuantity();
            }
        }
        try {
            for (short i = 0; i < 8; i++) {
                var item = inv.getBackpack().getItemStack(i);
                if (item != null && !item.isEmpty() && matchesItemId(item.getItemId(), lower)) {
                    count += item.getQuantity();
                }
            }
        } catch (IllegalArgumentException ignored) { /* backpack capacity 0 */ }
        return count;
    }

    /**
     * Match un item ID avec un targetId. Supporte :
     * - Match exact (ignoreCase) : "Ore_Iron_Stone" == "ore_iron_stone"
     * - Match partiel (contains) : "Ore_Iron_Stone" contient "ore_iron"
     */
    private static boolean matchesItemId(String actualId, String targetLower) {
        if (actualId == null) return false;
        String actualLower = actualId.toLowerCase();
        // Retirer le namespace "hytale:" si present
        if (actualLower.contains(":")) actualLower = actualLower.substring(actualLower.indexOf(':') + 1);
        if (targetLower.contains(":")) targetLower = targetLower.substring(targetLower.indexOf(':') + 1);
        // 2026-11-14 : butin de monstre = identifiant exact (« Butin_Croc_Loup » ne doit pas compter « Butin_Croc_Loup_Trork »)
        if (targetLower.startsWith("butin_")) return actualLower.equals(targetLower);
        return actualLower.equals(targetLower) || actualLower.contains(targetLower);
    }

    /**
     * Retire un nombre donne d'items de l'inventaire (hotbar + storage + backpack).
     * Retourne true si tous les items ont ete retires.
     */
    public static boolean removeItemsFromInventory(com.hypixel.hytale.server.core.entity.entities.Player player, String itemId, int amount) {
        if (player == null || itemId == null || amount <= 0) return false;
        int remaining = amount;
        String lower = itemId.toLowerCase();
        var inv = player.getInventory();

        // Parcourir hotbar
        for (short i = 0; i < 9 && remaining > 0; i++) {
            var item = inv.getHotbar().getItemStack(i);
            if (item != null && !item.isEmpty() && matchesItemId(item.getItemId(), lower)) {
                int inStack = item.getQuantity();
                if (inStack <= remaining) {
                    inv.getHotbar().removeItemStackFromSlot(i);
                    remaining -= inStack;
                } else {
                    // Retirer partiellement : remplacer le stack par un plus petit
                    inv.getHotbar().setItemStackForSlot(i,
                            new com.hypixel.hytale.server.core.inventory.ItemStack(item.getItemId(), inStack - remaining));
                    remaining = 0;
                }
            }
        }
        // Parcourir storage
        for (short i = 0; i < 27 && remaining > 0; i++) {
            var item = inv.getStorage().getItemStack(i);
            if (item != null && !item.isEmpty() && matchesItemId(item.getItemId(), lower)) {
                int inStack = item.getQuantity();
                if (inStack <= remaining) {
                    inv.getStorage().removeItemStackFromSlot(i);
                    remaining -= inStack;
                } else {
                    inv.getStorage().setItemStackForSlot(i,
                            new com.hypixel.hytale.server.core.inventory.ItemStack(item.getItemId(), inStack - remaining));
                    remaining = 0;
                }
            }
        }
        // Parcourir backpack
        try {
            for (short i = 0; i < 8 && remaining > 0; i++) {
                var item = inv.getBackpack().getItemStack(i);
                if (item != null && !item.isEmpty() && matchesItemId(item.getItemId(), lower)) {
                    int inStack = item.getQuantity();
                    if (inStack <= remaining) {
                        inv.getBackpack().removeItemStackFromSlot(i);
                        remaining -= inStack;
                    } else {
                        inv.getBackpack().setItemStackForSlot(i,
                                new com.hypixel.hytale.server.core.inventory.ItemStack(item.getItemId(), inStack - remaining));
                        remaining = 0;
                    }
                }
            }
        } catch (IllegalArgumentException ignored) { /* backpack capacity 0 */ }

        return remaining <= 0;
    }

    /**
     * Verifie si un joueur a assez d'items pour une quete MINAGE/RECOLTE.
     */
    public static boolean hasEnoughItemsForQuest(com.hypixel.hytale.server.core.entity.entities.Player player, QuestModel model) {
        if (model == null || model.getTargetId() == null) return false;
        return countItemInInventory(player, model.getTargetId()) >= model.getTargetAmount();
    }

    /**
     * Verifie si un item est present dans l'inventaire complet du joueur.
     * Delegue a countItemInInventory pour un match coherent (partiel, ignoreCase).
     */
    public static boolean hasItemInFullInventory(com.hypixel.hytale.server.core.entity.entities.Player player, String itemId) {
        return countItemInInventory(player, itemId) > 0;
    }

    private static void checkCompletion(UUID playerUUID, PlayerQuest pq, QuestModel model) {
        if (pq.getProgress() >= model.getTargetAmount()) {
            pq.setCompleted();
            // Recompenses donnees via le systeme qui appelle (DeathXPSystem etc.)
            // On stocke juste le flag completed ici
        }
    }

    // ==================== DAILY RESET ====================

    public static synchronized void checkDailyReset() {
        int today = getDayOfYear();
        if (today != currentDay) {
            currentDay = today;
            // Reset les journalieres NON completees (en cours)
            for (var entry : playerQuests.entrySet()) {
                entry.getValue().removeIf(pq -> {
                    QuestModel model = getQuest(pq.getQuestId());
                    return model != null && model.isDaily() && !pq.isCompleted();
                });
            }
            // Nouvelle selection aleatoire
            refreshDailySelection();
            System.out.println("[Quests] Nouveau jour ! Journalieres resetees + nouvelle selection.");
        }
    }

    private static int getDayOfYear() {
        return java.time.LocalDate.now().getDayOfYear();
    }

    // ==================== FILE-BASED PERSISTENCE (shutdown safety) ====================

    public static void initDataDir(Path pluginDataDir) {
        dataDir = pluginDataDir.resolve("eldanior_data");
    }

    /**
     * Sauvegarde toutes les quetes et cooldowns de tous les joueurs connectes
     * dans un fichier .properties. Appele au shutdown pour eviter la perte de progression.
     */
    public static void saveAllToFile() {
        if (dataDir == null) return;
        try {
            Properties props = new Properties();
            props.setProperty("_version", "1");
            for (Map.Entry<UUID, List<PlayerQuest>> entry : playerQuests.entrySet()) {
                UUID uuid = entry.getKey();
                String questData = serializePlayerQuests(uuid);
                String cooldownData = serializeCooldowns(uuid);
                if (!questData.isEmpty()) {
                    props.setProperty(uuid.toString() + ".quests", questData);
                }
                if (!cooldownData.isEmpty()) {
                    props.setProperty(uuid.toString() + ".cooldowns", cooldownData);
                }
            }
            PersistenceUtils.writeAtomicWithBackup(
                    dataDir.resolve("quest_shutdown.properties"), props,
                    "Quest & cooldown data saved at shutdown");
            System.out.println("[Quests] Donnees de " + playerQuests.size() + " joueurs sauvegardees au shutdown.");
        } catch (Exception e) {
            EldaniorLogger.error("QuestManager.saveAllToFile", e);
        }
    }

    /**
     * Charge les quetes et cooldowns depuis le fichier shutdown pour un joueur specifique.
     * Retourne true si des donnees ont ete trouvees et chargees.
     */
    public static boolean loadPlayerFromFile(UUID playerUUID) {
        if (dataDir == null) return false;
        Path file = dataDir.resolve("quest_shutdown.properties");
        if (!Files.exists(file)) return false;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            }
            // Version check
            String fileVersion = props.getProperty("_version");
            if (fileVersion == null) {
                System.out.println("[QuestManager] WARNING: quest_shutdown.properties n'a pas de _version — fichier ancien, migration future possible.");
            }
            String questData = props.getProperty(playerUUID.toString() + ".quests");
            String cooldownData = props.getProperty(playerUUID.toString() + ".cooldowns");
            boolean loaded = false;
            if (questData != null && !questData.isEmpty()) {
                deserializePlayerQuests(playerUUID, questData);
                loaded = true;
            }
            if (cooldownData != null && !cooldownData.isEmpty()) {
                deserializeCooldowns(playerUUID, cooldownData);
                loaded = true;
            }
            return loaded;
        } catch (Exception e) {
            EldaniorLogger.error("QuestManager.loadPlayerFromFile", e);
            return false;
        }
    }

    // ==================== SERIALIZATION ====================

    public static String serializePlayerQuests(UUID playerUUID) {
        List<PlayerQuest> quests = getPlayerQuests(playerUUID);
        StringBuilder sb = new StringBuilder();
        for (PlayerQuest pq : quests) {
            if (sb.length() > 0) sb.append("|");
            sb.append(pq.serialize());
        }
        return sb.toString();
    }

    public static void deserializePlayerQuests(UUID playerUUID, String data) {
        if (data == null || data.isEmpty()) return;
        List<PlayerQuest> quests = getPlayerQuests(playerUUID);
        quests.clear();
        for (String entry : data.split("\\|")) {
            PlayerQuest pq = PlayerQuest.deserialize(entry);
            if (pq != null) quests.add(pq);
        }
    }

    /** Nettoie les donnees en memoire d'un joueur qui se deconnecte. */
    public static void handleDisconnect(UUID playerUUID) {
        if (playerUUID == null) return;
        // Sauvegarder avant suppression
        if (dataDir != null) {
            try {
                Path file = dataDir.resolve("quest_shutdown.properties");
                Properties props = new Properties();
                if (Files.exists(file)) {
                    try (InputStream in = Files.newInputStream(file)) { props.load(in); }
                }
                String qd = serializePlayerQuests(playerUUID);
                String cd = serializeCooldowns(playerUUID);
                if (!qd.isEmpty()) props.setProperty(playerUUID + ".quests", qd);
                if (!cd.isEmpty()) props.setProperty(playerUUID + ".cooldowns", cd);
                PersistenceUtils.writeAtomicWithBackup(file, props, "Quest data saved on disconnect");
            } catch (Exception e) { EldaniorLogger.error("QuestManager.handleDisconnect save", e); }
        }
        playerQuests.remove(playerUUID);
        cooldowns.remove(playerUUID);
        activeBounties.remove(playerUUID);
    }
}
