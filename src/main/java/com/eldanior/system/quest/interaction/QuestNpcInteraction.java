package com.eldanior.system.quest.interaction;

import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.dialogue.DialogueScreen;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Interaction generique pour TOUS les PNJ de quetes.
 * Un seul Root + une seule Interaction pour tous les NPC.
 * Le NpcId est lu automatiquement depuis le NPCTypeId du NPC interagi.
 *
 * Usage: assigner Root_QuestNpcInteraction au NPC dans son JSON.
 * Le npcGiverId des quetes doit correspondre au NPCTypeId du NPC.
 */
public class QuestNpcInteraction extends SimpleInteraction {

    public QuestNpcInteraction() { super(); }

    public static final BuilderCodec<QuestNpcInteraction> CODEC =
            BuilderCodec.builder(QuestNpcInteraction.class, QuestNpcInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {

        if (!firstRun || type != InteractionType.Use) return;

        var entityRef = context.getOwningEntity();
        var store = entityRef.getStore();

        Player player = store.getComponent(entityRef, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (player == null || playerRef == null) return;

        // Recuperer le NPC type ID depuis le NPC interagi
        String npcId = "unknown";
        var targetRef = context.getTargetEntity();
        if (targetRef != null) {
            NPCEntity npc = store.getComponent(targetRef, NPCEntity.getComponentType());
            if (npc != null) {
                npcId = npc.getNPCTypeId();
            }
        }

        // Trouver la prochaine quete pour ce NPC
        QuestModel quest = QuestManager.getNextDialogueForNpc(playerRef, npcId);

        if (quest instanceof NpcDialogueQuest dialogueQuest && dialogueQuest.hasDialogue()) {
            player.getPageManager().openCustomPage(entityRef, store,
                    new DialogueScreen(playerRef, dialogueQuest));
        } else if (quest != null) {
            playerRef.sendMessage(Message.raw("§6[PNJ] §eQuete : §f" + quest.getName()));
            playerRef.sendMessage(Message.raw("§7" + quest.getDescription()));
        } else {
            playerRef.sendMessage(Message.raw("§6[PNJ] §7Je n'ai rien pour vous pour le moment."));
        }
    }
}
