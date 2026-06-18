package com.eldanior.system.quest.dialogue;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.config.UUIDExtractor;
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
        int oldLevel = pData.getLevel();
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

        // Notifications + level up check
        PlayerRef pRef2 = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef2 != null) {
            com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pRef2,
                    "QUETE TERMINEE", quest.getName(), true);
            com.eldanior.system.Leveling.utils.NotificationHelper.sendSuccess(pRef2,
                    "<color:green>+" + quest.getRewardXP() + " XP</color> <color:gold>+" + quest.getRewardGold() + " Or</color>");

            // Level up ?
            if (pData.getLevel() > oldLevel) {
                com.eldanior.system.Leveling.utils.NotificationHelper.showLevelUpTitle(pRef2, pData.getLevel());
                // Points d'attributs
                int gained = (pData.getLevel() - oldLevel) * 3;
                pData.setAttributePoints(pData.getAttributePoints() + gained);
                store.putComponent(ref, type, pData);
            }

            // Titre débloqué ?
            if (quest.getRewardTitleId() != null) {
                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pRef2,
                        "TITRE DEBLOQUE", quest.getRewardTitleId(), true);
            }
        }

        // Mettre à jour les stats en jeu
        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, pData);

        // Verifier titres en temps reel apres completion de quete (XP, or, niveau)
        PlayerRef pRef3 = store.getComponent(ref, PlayerRef.getComponentType());
        com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, pData, pRef3);

        this.close();
    }

    private UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        try { return UUIDExtractor.getUUID(pRef); } catch (Exception e) { return null; }
    }

    public static class CompletionEventData {
        public static final BuilderCodec<CompletionEventData> CODEC = BuilderCodec.builder(CompletionEventData.class, CompletionEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
