package com.eldanior.system.quest;

import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestManager {

    private static final Map<String, QuestModel> allQuests = new LinkedHashMap<>();

    // Quetes actives par joueur (UUID -> liste de PlayerQuest)
    private static final Map<UUID, List<PlayerQuest>> playerQuests = new ConcurrentHashMap<>();

    // Jour courant pour les journalieres
    private static int currentDay = -1;

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
        // Principal - Histoire
        register(new com.eldanior.system.quest.definitions.principal.MainQuest_NaissanceRoi());

        // Secondaire
        register(new com.eldanior.system.quest.definitions.secondaire.SideQuest_Explorateur());
        register(new com.eldanior.system.quest.definitions.secondaire.SideQuest_Duelliste());
        register(new com.eldanior.system.quest.definitions.secondaire.SecQuest_AncienConseiller());
        register(new com.eldanior.system.quest.definitions.secondaire.InfoQuest_Tavernier());

        // === Journalieres (auto-generated: 116 quetes) ===
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_Bear());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_DinosaureHunt());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_DragonHunt());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_FeranHunt());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_FeranWindwalker());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_GoblinHermit());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_GoblinPatrol());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_GoblinRaid());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_GolemCrystal());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_OutlanderBrute());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_OutlanderHunt());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_Owl());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_Pigeon());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SaurianHunt());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_ScarakHunt());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SkeletonBase());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SkeletonBurnt());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SkeletonFrost());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SkeletonIncandescent());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SkeletonSand());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SlothianThreat());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_SpiritHunt());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_Squelettes());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_TrorkScout());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_TrorkWarrior());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_VoidCrawler());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_VoidLarva());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_VoidNecro());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_VoidSpectre());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_ZombieBase());
        register(new com.eldanior.system.quest.definitions.journaliere.chasse.DailyChasse_Zombies());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_Big());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_CollectB2());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_CollectC2());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_CollectD2());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_CollectE2());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_CollectF2());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_CollectS());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_Fortune());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_Huge());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_Legend());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_Medium());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_PetitTresor());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_Small());
        register(new com.eldanior.system.quest.definitions.journaliere.collection.DailyCollect_Tiny());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_Champion());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_ChampionDuJour());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_DuelA2());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_DuelB2());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_DuelD());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_DuelF());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_Duo());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_First());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_Invincible());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_PremierSang());
        register(new com.eldanior.system.quest.definitions.journaliere.duel.DailyDuel_Triple());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_Big());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_ChasseurCoffres());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_ExploreA());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_ExploreB2());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_ExploreE());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_ExploreE2());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_GrandExplorateur());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_Huge());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_Legend());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_Medium());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_Small());
        register(new com.eldanior.system.quest.definitions.journaliere.exploration.DailyExplore_Tiny());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_Carnage());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_Easy());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_Exterminateur());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_Extreme());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_Hard());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_Legend());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_MassacreA());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_MassacreC());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_MassacreC2());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_MassacreD2());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_MassacreE());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_MassacreE2());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_MassacreF2());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_MassacreS());
        register(new com.eldanior.system.quest.definitions.journaliere.massacre.DailyMassacre_Medium());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin1());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin10());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin2());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin3());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin4());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin5());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin6());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin7());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin8());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Assassin9());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Duel1());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Duel2());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Duel3());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Duel4());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Duel5());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Duel6());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_DuelSombre());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Pillard());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Pillard1());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Pillard2());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Pillard3());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Pillard4());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Pillard5());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur1());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur2());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur3());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur4());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur5());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur6());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur7());
        register(new com.eldanior.system.quest.definitions.journaliere.pk.DailyPK_Terreur8());    }

    private static void register(QuestModel quest) {
        allQuests.put(quest.getId(), quest);
    }

    // ==================== GETTERS ====================

    public static QuestModel getQuest(String id) { return allQuests.get(id); }

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

        for (QuestModel quest : allQuests.values()) {
            if (!npcId.equals(quest.getNpcGiverId())) continue;
            if (completedIds.contains(quest.getId())) continue;
            if (!(quest instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq)) continue;

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

    public static PlayerQuest getActiveQuest(UUID playerUUID) {
        for (PlayerQuest pq : getPlayerQuests(playerUUID)) {
            if (pq.isActive()) return pq;
        }
        return null;
    }

    public static boolean acceptQuest(UUID playerUUID, String questId) {
        QuestModel model = getQuest(questId);
        if (model == null) return false;

        List<PlayerQuest> quests = getPlayerQuests(playerUUID);
        for (PlayerQuest pq : quests) {
            if (pq.getQuestId().equals(questId)) return false; // Deja acceptee
        }

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
     * Appele quand un mob est tue. Verifie la quete active du joueur.
     */
    public static void onMobKill(UUID playerUUID, String mobTypeId) {
        PlayerQuest active = getActiveQuest(playerUUID);
        if (active == null || active.isCompleted()) return;

        QuestModel model = getQuest(active.getQuestId());
        if (model == null) return;

        // Les quetes NPC (avec QuestCondition) ne progressent pas via les hooks auto
        if (model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq
                && nq.getCompletionCondition() != null) return;

        boolean progress = false;
        if (model.getType() == QuestType.MASSACRE) {
            progress = true;
        } else if (model.getType() == QuestType.CHASSE && model.getTargetId() != null) {
            if (mobTypeId.toLowerCase().contains(model.getTargetId().toLowerCase())) {
                progress = true;
            }
        }

        if (progress) {
            active.addProgress(1);
            checkCompletion(playerUUID, active, model);
        }
    }

    public static void onChestDiscovered(UUID playerUUID) {
        PlayerQuest active = getActiveQuest(playerUUID);
        if (active == null || active.isCompleted()) return;
        QuestModel model = getQuest(active.getQuestId());
        if (model == null || model.getType() != QuestType.EXPLORATION) return;
        if (model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.getCompletionCondition() != null) return;
        active.addProgress(1);
        checkCompletion(playerUUID, active, model);
    }

    public static void onDuelWin(UUID playerUUID) {
        PlayerQuest active = getActiveQuest(playerUUID);
        if (active == null || active.isCompleted()) return;
        QuestModel model = getQuest(active.getQuestId());
        if (model == null || model.getType() != QuestType.DUEL) return;
        if (model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.getCompletionCondition() != null) return;
        active.addProgress(1);
        checkCompletion(playerUUID, active, model);
    }

    public static void onGoldGained(UUID playerUUID, long amountGained) {
        PlayerQuest active = getActiveQuest(playerUUID);
        if (active == null || active.isCompleted()) return;
        QuestModel model = getQuest(active.getQuestId());
        if (model == null || model.getType() != QuestType.COLLECTION) return;
        if (model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.getCompletionCondition() != null) return;
        // Incrementer la progression du montant gagne (pas le total)
        active.addProgress((int) amountGained);
        checkCompletion(playerUUID, active, model);
    }

    public static void onPKKilled(UUID killerUUID, UUID pkUUID) {
        PlayerQuest active = getActiveQuest(killerUUID);
        if (active == null || active.isCompleted()) return;
        QuestModel model = getQuest(active.getQuestId());
        if (model == null || model.getType() != QuestType.EXECUTION) return;
        if (model instanceof com.eldanior.system.quest.dialogue.NpcDialogueQuest nq && nq.getCompletionCondition() != null) return;
        active.addProgress(1);
        checkCompletion(killerUUID, active, model);
    }

    private static void checkCompletion(UUID playerUUID, PlayerQuest pq, QuestModel model) {
        if (pq.getProgress() >= model.getTargetAmount()) {
            pq.setCompleted();
            // Recompenses donnees via le systeme qui appelle (DeathXPSystem etc.)
            // On stocke juste le flag completed ici
        }
    }

    // ==================== DAILY RESET ====================

    public static void checkDailyReset() {
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
        playerQuests.remove(playerUUID);
        cooldowns.remove(playerUUID);
    }
}
