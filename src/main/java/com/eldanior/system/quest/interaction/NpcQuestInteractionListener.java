package com.eldanior.system.quest.interaction;

import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.dialogue.DialogueScreen;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Set;

/**
 * Listener global pour les interactions joueur-NPC de quetes.
 * Comme le plugin NPCDialog : detecte l'interaction (touche F) avec un NPC existant
 * et ouvre le dialogue en code.
 */
public class NpcQuestInteractionListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // NPCTypeId des PNJ qui donnent des quetes (doit correspondre au nom du role NPC)
    private static final Set<String> QUEST_NPC_IDS = Set.of(
            "Guild_Npc",
            "AncienConseiller_Npc",
            "TavernierQuest_Npc"
    );

    public static void onPlayerInteract(PlayerInteractEvent event) {
        LOGGER.atInfo().log("[QUEST-NPC] PlayerInteractEvent fired! ActionType=" + event.getActionType());

        // Seulement l'action Use (touche F)
        if (event.getActionType() != com.hypixel.hytale.protocol.InteractionType.Use) {
            LOGGER.atInfo().log("[QUEST-NPC] Ignored: not Use action");
            return;
        }

        // Verifier qu'on a une cible entite
        var targetRef = event.getTargetRef();
        var targetEntity = event.getTargetEntity();

        LOGGER.atInfo().log("[QUEST-NPC] targetRef=" + targetRef + " targetEntity=" + targetEntity);

        if (targetRef == null || !targetRef.isValid()) {
            LOGGER.atInfo().log("[QUEST-NPC] No valid targetRef");
            return;
        }

        var playerEntityRef = event.getPlayerRef();
        Player player = event.getPlayer();
        if (player == null) return;

        // Verifier que la cible est un NPC
        var store = playerEntityRef.getStore();
        NPCEntity npc = store.getComponent(targetRef, NPCEntity.getComponentType());
        if (npc == null) {
            LOGGER.atInfo().log("[QUEST-NPC] Target is not an NPC");
            return;
        }

        String npcTypeId = npc.getNPCTypeId();
        LOGGER.atInfo().log("[QUEST-NPC] NPC TypeId = " + npcTypeId);

        if (npcTypeId == null || !QUEST_NPC_IDS.contains(npcTypeId)) {
            LOGGER.atInfo().log("[QUEST-NPC] NPC not in quest list: " + npcTypeId);
            return;
        }

        // C'est un PNJ de quetes -> ouvrir le dialogue
        PlayerRef playerRef = store.getComponent(playerEntityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        LOGGER.atInfo().log("[QUEST-NPC] Opening dialogue for NPC: " + npcTypeId);

        QuestModel quest = QuestManager.getNextDialogueForNpc(playerRef, npcTypeId);

        if (quest instanceof NpcDialogueQuest dialogueQuest && dialogueQuest.hasDialogue()) {
            player.getPageManager().openCustomPage(playerEntityRef, store,
                    new DialogueScreen(playerRef, dialogueQuest));
            LOGGER.atInfo().log("[QUEST-NPC] DialogueScreen opened for quest: " + quest.getId());
        } else {
            playerRef.sendMessage(Message.raw("[PNJ] Je n'ai rien pour vous pour le moment."));
            LOGGER.atInfo().log("[QUEST-NPC] No dialogue quest found for NPC: " + npcTypeId);
        }
    }
}
