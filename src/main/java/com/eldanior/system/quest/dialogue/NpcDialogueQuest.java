package com.eldanior.system.quest.dialogue;

import com.eldanior.system.quest.*;

import java.util.List;

/**
 * Quete narrative avec dialogue multi-pages.
 * Utilisee pour les quetes PNJ (principales, secondaires) et les dialogues info.
 */
public class NpcDialogueQuest extends QuestModel {

    private final List<DialoguePage> pages;
    private final QuestCondition completionCondition;
    private final String unlocksQuestId;
    private final boolean infoOnly; // true = pas de quete, juste un dialogue informatif avec VALIDER
    private final String validatesObjective; // ID de quete dont un objectif est valide en parlant

    // Constructeur standard (quete avec objectifs)
    public NpcDialogueQuest(String id, String name, String description,
                             QuestType type, QuestCategory category, QuestDifficulty difficulty,
                             String targetId, int targetAmount,
                             int rewardXP, long rewardGold, String rewardTitleId,
                             String nextQuestId, String npcGiverId,
                             List<DialoguePage> pages,
                             QuestCondition completionCondition,
                             String unlocksQuestId) {
        super(id, name, description, type, category, difficulty,
                targetId, targetAmount, rewardXP, rewardGold, rewardTitleId, nextQuestId, npcGiverId);
        this.pages = pages;
        this.completionCondition = completionCondition;
        this.unlocksQuestId = unlocksQuestId;
        this.infoOnly = false;
        this.validatesObjective = null;
    }

    // Constructeur avec cooldown (quetes secondaires repetables)
    public NpcDialogueQuest(String id, String name, String description,
                             QuestType type, QuestCategory category, QuestDifficulty difficulty,
                             String targetId, int targetAmount,
                             int rewardXP, long rewardGold, String rewardTitleId,
                             String nextQuestId, String npcGiverId,
                             List<DialoguePage> pages,
                             QuestCondition completionCondition,
                             String unlocksQuestId,
                             int cooldownMinutes) {
        super(id, name, description, type, category, difficulty,
                targetId, targetAmount, rewardXP, rewardGold, rewardTitleId, nextQuestId, npcGiverId, cooldownMinutes);
        this.pages = pages;
        this.completionCondition = completionCondition;
        this.unlocksQuestId = unlocksQuestId;
        this.infoOnly = false;
        this.validatesObjective = null;
    }

    // Constructeur info-only (dialogue sans quete, valide un objectif d'une autre quete)
    public NpcDialogueQuest(String id, String name, String description,
                             String npcGiverId, List<DialoguePage> pages,
                             String validatesObjective) {
        super(id, name, description, QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                null, 1, 0, 0, null, null, npcGiverId);
        this.pages = pages;
        this.completionCondition = null;
        this.unlocksQuestId = null;
        this.infoOnly = true;
        this.validatesObjective = validatesObjective;
    }

    public List<DialoguePage> getPages() { return pages; }
    public QuestCondition getCompletionCondition() { return completionCondition; }
    public String getUnlocksQuestId() { return unlocksQuestId; }
    public boolean hasDialogue() { return pages != null && !pages.isEmpty(); }
    public boolean isInfoOnly() { return infoOnly; }
    public String getValidatesObjective() { return validatesObjective; }

    @Override
    public String getObjectiveText() {
        if (completionCondition != null) {
            return completionCondition.getRequirementText();
        }
        return super.getObjectiveText();
    }
}
