package com.eldanior.system.quest.definitions.npc;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.dialogue.DialogueScreen;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.UUID;

/**
 * Interaction generique pour les PNJ donneurs de quetes.
 *
 * Usage dans un InteractionConfig ou un system:
 *   NpcQuestInteraction.interact(playerRef, store, ref, "npc_village_elder");
 *
 * Le NPC propose toutes les quetes qui ont son npcGiverId.
 * Si le joueur a deja la quete, il affiche la progression.
 * Si la quete est completee, il donne les recompenses.
 */
public class NpcQuestInteraction {

    /**
     * Appele quand un joueur interagit avec un NPC donneur de quetes.
     * @param npcId L'identifiant du NPC (doit correspondre au npcGiverId des quetes)
     */
    public static void interact(PlayerRef playerRef, Store<EntityStore> store,
                                Ref<EntityStore> playerEntityRef, String npcId) {
        UUID playerUUID = extractUUID(playerRef);
        if (playerUUID == null) return;

        List<PlayerQuest> playerQuests = QuestManager.getPlayerQuests(playerUUID);

        // Chercher les quetes de ce NPC
        boolean hasInteraction = false;
        for (QuestModel quest : QuestManager.getAllQuests()) {
            if (!npcId.equals(quest.getNpcGiverId())) continue;

            // Chercher si le joueur a deja cette quete
            PlayerQuest pq = null;
            for (PlayerQuest existing : playerQuests) {
                if (existing.getQuestId().equals(quest.getId())) {
                    pq = existing;
                    break;
                }
            }

            if (pq == null) {
                // Quete pas encore acceptee -> ouvrir le dialogue si disponible
                if (quest instanceof NpcDialogueQuest dialogueQuest && dialogueQuest.hasDialogue()) {
                    // Ouvrir la fenetre de dialogue
                    Player player = store.getComponent(playerEntityRef, Player.getComponentType());
                    if (player != null) {
                        player.getPageManager().openCustomPage(playerEntityRef, store,
                                new DialogueScreen(playerRef, dialogueQuest));
                    }
                } else {
                    // Quete simple sans dialogue
                    playerRef.sendMessage(Message.raw("[PNJ] Nouvelle quete : " + quest.getName()));
                    playerRef.sendMessage(Message.raw("" + quest.getDescription()));
                    playerRef.sendMessage(Message.raw("Objectif : " + quest.getObjectiveText()));
                    playerRef.sendMessage(Message.raw("Recompense : " + quest.getRewardText()));

                    QuestManager.acceptQuest(playerUUID, quest.getId());
                    if (QuestManager.getActiveQuest(playerUUID) == null) {
                        QuestManager.activateQuest(playerUUID, quest.getId());
                    }
                    playerRef.sendMessage(Message.raw("Quete acceptee !"));
                }
                hasInteraction = true;

            } else if (pq.isCompleted()) {
                // Quete completee -> si dialogue, rouvrir pour reclamer
                if (quest instanceof NpcDialogueQuest dialogueQuest && dialogueQuest.hasDialogue()) {
                    Player player = store.getComponent(playerEntityRef, Player.getComponentType());
                    if (player != null) {
                        player.getPageManager().openCustomPage(playerEntityRef, store,
                                new DialogueScreen(playerRef, dialogueQuest));
                    }
                } else {
                    playerRef.sendMessage(Message.raw("[PNJ] Quete terminee : " + quest.getName() + " !"));
                    giveRewards(playerRef, store, playerEntityRef, quest);
                }
                hasInteraction = true;

            } else {
                // Quete en cours -> afficher progression ou rouvrir dialogue
                if (quest instanceof NpcDialogueQuest dialogueQuest && dialogueQuest.hasDialogue()) {
                    Player player = store.getComponent(playerEntityRef, Player.getComponentType());
                    if (player != null) {
                        player.getPageManager().openCustomPage(playerEntityRef, store,
                                new DialogueScreen(playerRef, dialogueQuest));
                    }
                } else {
                    playerRef.sendMessage(Message.raw("[PNJ] Quete en cours : " + quest.getName()));
                    playerRef.sendMessage(Message.raw("Progression : " + pq.getProgress() + " / " + quest.getTargetAmount()));
                }
                hasInteraction = true;
            }
        }

        if (!hasInteraction) {
            playerRef.sendMessage(Message.raw("[PNJ] Je n'ai rien pour vous pour le moment."));
        }
    }

    private static void giveRewards(PlayerRef playerRef, Store<EntityStore> store,
                                     Ref<EntityStore> ref, QuestModel quest) {
        try {
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data == null) return;

            data.addExperience(quest.getRewardXP());
            data.addMoney(quest.getRewardGold());

            if (quest.getRewardTitleId() != null) {
                data.addTitle(quest.getRewardTitleId());
                playerRef.sendMessage(Message.raw("Titre debloque : " + quest.getRewardTitleId()));
            }

            store.putComponent(ref, type, data);

            playerRef.sendMessage(Message.raw("+" + quest.getRewardXP() + " XP"));
            if (quest.getRewardGold() > 0) {
                playerRef.sendMessage(Message.raw("+" + quest.getRewardGold() + " Or"));
            }

            // Debloquer la quete suivante si chainee
            if (quest.getNextQuestId() != null) {
                playerRef.sendMessage(Message.raw("Nouvelle quete disponible : parlez au PNJ !"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static UUID extractUUID(PlayerRef playerRef) {
        if (playerRef == null) return null;
        try { return UUIDExtractor.getUUID(playerRef); } catch (Exception e) { return null; }
    }
}
