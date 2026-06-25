package com.eldanior.system.quest.interaction;

import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.dialogue.DialogueScreen;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

/**
 * Supplier d'interaction pour les PNJ de quetes.
 * Inspire de NPCDialog-1.2.3 : utilise OpenCustomUIInteraction.CustomPageSupplier.
 * Le NPC JSON utilise: "Type": "OpenCustomUI", "CustomPage": "QuestDialog"
 */
public class QuestDialogSupplier implements OpenCustomUIInteraction.CustomPageSupplier {

    @Override
    public CustomUIPage tryCreate(Ref<EntityStore> entityRef,
                                   ComponentAccessor<EntityStore> accessor,
                                   PlayerRef playerRef,
                                   InteractionContext context) {

        // Recuperer le NPC cible
        Ref<EntityStore> targetRef = context.getTargetEntity();
        if (targetRef == null || !targetRef.isValid()) {
            // Fallback: essayer via clientState
            var clientState = context.getClientState();
            if (clientState != null && clientState.entityId >= 0) {
                var cmdBuf = context.getCommandBuffer();
                if (cmdBuf != null) {
                    var entityStore = (EntityStore) cmdBuf.getStore().getExternalData();
                    targetRef = entityStore.getRefFromNetworkId(clientState.entityId);
                }
            }
        }

        if (targetRef == null || !targetRef.isValid()) return null;

        // Recuperer le NPCTypeId
        var cmdBuf = context.getCommandBuffer();
        if (cmdBuf == null) return null;

        NPCEntity npc = cmdBuf.getComponent(targetRef, NPCEntity.getComponentType());
        String npcTypeId = npc != null ? npc.getNPCTypeId() : "unknown";

        // Trouver la quete pour ce NPC
        QuestModel quest = QuestManager.getNextDialogueForNpc(playerRef, npcTypeId);

        if (quest instanceof NpcDialogueQuest dialogueQuest && dialogueQuest.hasDialogue()) {
            return new DialogueScreen(playerRef, dialogueQuest);
        }

        // Pas de quete dialogue -> envoyer un message
        if (playerRef != null) {
            playerRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("[PNJ] Je n'ai rien pour vous pour le moment."));
        }
        return null;
    }
}