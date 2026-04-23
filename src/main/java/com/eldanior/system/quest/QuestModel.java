package com.eldanior.system.quest;

public class QuestModel {

    private final String id;
    private final String name;
    private final String description;
    private final QuestType type;
    private final QuestCategory category;
    private final QuestDifficulty difficulty;

    // Objectif
    private final String targetId;     // mob type pour CHASSE, UUID pour EXECUTION, null sinon
    private final int targetAmount;    // nombre a atteindre

    // Recompenses
    private final int rewardXP;
    private final long rewardGold;
    private final String rewardTitleId; // null si pas de titre

    // Quete suivante dans la chaine (scenario)
    private final String nextQuestId;  // null si pas de suite

    // PNJ donneur (null = disponible via fenetre)
    private final String npcGiverId;

    // Cooldown en minutes (0 = pas de cooldown)
    private final int cooldownMinutes;

    public QuestModel(String id, String name, String description,
                      QuestType type, QuestCategory category, QuestDifficulty difficulty,
                      String targetId, int targetAmount,
                      int rewardXP, long rewardGold, String rewardTitleId,
                      String nextQuestId, String npcGiverId) {
        this(id, name, description, type, category, difficulty, targetId, targetAmount,
                rewardXP, rewardGold, rewardTitleId, nextQuestId, npcGiverId, 0);
    }

    public QuestModel(String id, String name, String description,
                      QuestType type, QuestCategory category, QuestDifficulty difficulty,
                      String targetId, int targetAmount,
                      int rewardXP, long rewardGold, String rewardTitleId,
                      String nextQuestId, String npcGiverId, int cooldownMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.difficulty = difficulty;
        this.targetId = targetId;
        this.targetAmount = targetAmount;
        this.rewardXP = rewardXP;
        this.rewardGold = rewardGold;
        this.rewardTitleId = rewardTitleId;
        this.nextQuestId = nextQuestId;
        this.npcGiverId = npcGiverId;
        this.cooldownMinutes = cooldownMinutes;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public QuestCategory getCategory() { return category; }
    public QuestDifficulty getDifficulty() { return difficulty; }
    public String getTargetId() { return targetId; }
    public int getTargetAmount() { return targetAmount; }
    public int getRewardXP() { return (int)(rewardXP * difficulty.getRewardMultiplier()); }
    public long getRewardGold() { return (long)(rewardGold * difficulty.getRewardMultiplier()); }
    public String getRewardTitleId() { return rewardTitleId; }
    public String getNextQuestId() { return nextQuestId; }
    public String getNpcGiverId() { return npcGiverId; }
    public int getCooldownMinutes() { return cooldownMinutes; }
    public boolean isDaily() { return category == QuestCategory.JOURNALIERE; }
    public boolean isMainStory() { return category == QuestCategory.PRINCIPAL; }

    public String getRewardText() {
        StringBuilder sb = new StringBuilder();
        sb.append(getRewardXP()).append(" XP");
        if (getRewardGold() > 0) sb.append(" + ").append(getRewardGold()).append(" Or");
        if (rewardTitleId != null) sb.append(" + Titre");
        return sb.toString();
    }

    public String getObjectiveText() {
        return switch (type) {
            case CHASSE -> "Tuer " + targetAmount + " " + formatMobName(targetId);
            case MASSACRE -> "Tuer " + targetAmount + " monstres";
            case EXPLORATION -> "Decouvrir " + targetAmount + " coffres";
            case COLLECTION -> "Accumuler " + targetAmount + " Or";
            case DUEL -> "Gagner " + targetAmount + " duels";
            case EXECUTION -> "Eliminer le criminel";
        };
    }

    private String formatMobName(String mobId) {
        if (mobId == null) return "monstres";
        String[] parts = mobId.replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }
}
