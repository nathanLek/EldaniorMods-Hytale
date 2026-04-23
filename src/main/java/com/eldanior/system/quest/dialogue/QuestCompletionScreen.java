package com.eldanior.system.quest.dialogue;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.QuestManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Ecran de completion de quete.
 * Affiche un message de felicitations + recompenses + bouton RECLAMER.
 * La completion se fait au clic (dans le handler de page, pas dans un system tick).
 */
public class QuestCompletionScreen extends InteractiveCustomUIPage<QuestCompletionScreen.CompletionEventData> {

    private final NpcDialogueQuest quest;

    public QuestCompletionScreen(@Nonnull PlayerRef playerRef, NpcDialogueQuest quest) {
        super(playerRef, CustomPageLifetime.CanDismiss, CompletionEventData.CODEC);
        this.quest = quest;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Quest/NpcMessage.ui");

        String npcName = quest.getPages().get(quest.getPages().size() - 1).getSpeakerName();
        ui.set("#NpcMsgSpeaker.Text", npcName);

        StringBuilder msg = new StringBuilder();
        msg.append("Vous avez rempli tous les objectifs !\n\n");
        msg.append("Recompenses :\n");
        msg.append("+").append(quest.getRewardXP()).append(" XP");
        if (quest.getRewardGold() > 0) msg.append("   +").append(quest.getRewardGold()).append(" Or");
        if (quest.getRewardTitleId() != null) msg.append("\nTitre : ").append(quest.getRewardTitleId());
        if (quest.getUnlocksQuestId() != null) msg.append("\n\nUne nouvelle quete sera debloquee !");

        ui.set("#NpcMsgText.Text", msg.toString());
        ui.set("#NpcMsgBtnOk.Text", "RECLAMER");

        events.addEventBinding(CustomUIEventBindingType.Activating, "#NpcMsgBtnOk", EventData.of("Action", "claim"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull CompletionEventData data) {
        if (!"claim".equals(data.action)) return;

        UUID playerUUID = getPlayerUUID(ref, store);
        if (playerUUID == null) { this.close(); return; }

        var type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData pData = store.getComponent(ref, type);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (pData == null) { this.close(); return; }

        // Donner recompenses
        pData.addExperience(quest.getRewardXP());
        pData.addMoney(quest.getRewardGold());
        if (quest.getRewardTitleId() != null) pData.addTitle(quest.getRewardTitleId());

        // Marquer comme completee
        for (PlayerQuest pq : QuestManager.getPlayerQuests(playerUUID)) {
            if (pq.getQuestId().equals(quest.getId())) {
                pq.addProgress(1);
                pq.setCompleted();
                break;
            }
        }

        // Debloquer et activer la quete suivante
        if (quest.getUnlocksQuestId() != null) {
            QuestManager.acceptQuest(playerUUID, quest.getUnlocksQuestId());
            QuestManager.activateQuest(playerUUID, quest.getUnlocksQuestId());
        }

        // Sauvegarder
        pData.setQuestData(QuestManager.serializePlayerQuests(playerUUID));
        store.putComponent(ref, type, pData);

        if (player != null) {
            player.sendMessage(Message.raw("§a§lQuete terminee : " + quest.getName() + " !"));
            if (quest.getUnlocksQuestId() != null) {
                var nextQuest = QuestManager.getQuest(quest.getUnlocksQuestId());
                if (nextQuest != null) {
                    player.sendMessage(Message.raw("§6Nouvelle quete : " + nextQuest.getName()));
                }
            }
        }

        this.close();
    }

    private UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        try {
            Field f = PlayerRef.class.getDeclaredField("uuid");
            f.setAccessible(true);
            return (UUID) f.get(pRef);
        } catch (Exception e) { return null; }
    }

    public static class CompletionEventData {
        public static final BuilderCodec<CompletionEventData> CODEC = BuilderCodec.builder(CompletionEventData.class, CompletionEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
