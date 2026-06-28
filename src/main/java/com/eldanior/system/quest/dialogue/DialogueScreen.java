package com.eldanior.system.quest.dialogue;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
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
import java.util.List;
import java.util.UUID;

/**
 * Fenetre de dialogue pour les quetes PNJ.
 * 2 modes :
 * - Quete normale : dialogue + ACCEPTER LA QUETE (demarre la quete)
 * - Info only : dialogue + VALIDER (valide un objectif d'une autre quete)
 */
public class DialogueScreen extends InteractiveCustomUIPage<DialogueScreen.DialogueEventData> {

    private final NpcDialogueQuest quest;
    private int currentPage = 0;

    public DialogueScreen(@Nonnull PlayerRef playerRef, NpcDialogueQuest quest) {
        super(playerRef, CustomPageLifetime.CanDismiss, DialogueEventData.CODEC);
        this.quest = quest;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Quest/DialoguePage.ui");

        events.addEventBinding(CustomUIEventBindingType.Activating, "#DlgBtnNext", EventData.of("Action", "next"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#DlgBtnAccept", EventData.of("Action", "accept"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#DlgBtnClose", EventData.of("Action", "close"));

        renderPage(ui, ref, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull DialogueEventData data) {
        if (data.action == null) return;

        switch (data.action) {
            case "next" -> {
                if (currentPage < quest.getPages().size() - 1) {
                    currentPage++;
                    UICommandBuilder update = new UICommandBuilder();
                    renderPage(update, ref, store);
                    this.sendUpdate(update);
                }
            }
            case "accept" -> handleAccept(ref, store);
            case "close" -> this.close();
        }
    }

    private void renderPage(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        List<DialoguePage> pages = quest.getPages();
        if (pages == null || pages.isEmpty()) return;

        DialoguePage page = pages.get(currentPage);
        boolean isLastPage = currentPage >= pages.size() - 1;

        // Header
        ui.set("#DlgSpeaker.Text", page.getSpeakerName());
        ui.set("#DlgPageNum.Text", (currentPage + 1) + " / " + pages.size());

        // Contenu
        ui.set("#DlgText.Text", page.getText());
        ui.set("#DlgImageHint.Text", page.getImageHint() != null ? page.getImageHint() : "");

        // Boutons
        ui.set("#DlgBtnNext.Visible", !isLastPage);
        ui.set("#DlgBtnClose.Visible", isLastPage);

        if (isLastPage) {
            ui.set("#DlgBtnAccept.Visible", true);

            if (quest.isInfoOnly()) {
                // Mode info : bouton VALIDER seulement si le joueur a la quete parente
                boolean hasParentQuest = false;
                UUID checkUUID = getPlayerUUID(ref, store);
                if (checkUUID != null && quest.getValidatesObjective() != null) {
                    // Chercher une quete en cours qui a cet objectif comme condition
                    for (PlayerQuest pq : QuestManager.getPlayerQuests(checkUUID)) {
                        if (pq.isCompleted()) continue;
                        QuestModel parentModel = QuestManager.getQuest(pq.getQuestId());
                        if (parentModel instanceof NpcDialogueQuest parentNq
                                && parentNq.getCompletionCondition() != null
                                && quest.getValidatesObjective().equals(
                                    parentNq.getCompletionCondition().getRequiredQuestCompleted())) {
                            hasParentQuest = true;
                            break;
                        }
                    }
                }
                ui.set("#DlgBtnAccept.Visible", hasParentQuest);
                ui.set("#DlgBtnAccept.Text", "VALIDER");
                ui.set("#DlgObjectives.Visible", false);
                ui.set("#DlgRewards.Visible", false);
            } else {
                // Mode quete : bouton ACCEPTER + recompenses (pas d'objectifs affiches)
                ui.set("#DlgBtnAccept.Text", "ACCEPTER LA QUETE");
                ui.set("#DlgObjectives.Visible", false);

                // Recompenses
                ui.set("#DlgRewards.Visible", true);
                StringBuilder rewards = new StringBuilder("Recompenses : ");
                rewards.append("+").append(quest.getRewardXP()).append(" XP");
                if (quest.getRewardGold() > 0) rewards.append("   +").append(quest.getRewardGold()).append(" Or");
                if (quest.getRewardTitleId() != null) rewards.append("   Titre: ").append(quest.getRewardTitleId());
                ui.set("#DlgRewards.Text", rewards.toString());
            }
        } else {
            ui.set("#DlgBtnAccept.Visible", false);
            ui.set("#DlgObjectives.Visible", false);
            ui.set("#DlgRewards.Visible", false);
        }
    }

    private void handleAccept(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID playerUUID = getPlayerUUID(ref, store);
        if (playerUUID == null) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (data == null) return;

        if (quest.isInfoOnly()) {
            // Mode info : accepter + completer immediatement (valide l'objectif)
            QuestManager.acceptQuest(playerUUID, quest.getId());
            for (PlayerQuest pq : QuestManager.getPlayerQuests(playerUUID)) {
                if (pq.getQuestId().equals(quest.getId())) {
                    pq.addProgress(1);
                    pq.setCompleted();
                    break;
                }
            }

            data.setQuestData(QuestManager.serializePlayerQuests(playerUUID));
            store.putComponent(ref, type, data);

            PlayerRef pRef3 = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef3 != null) {
                com.eldanior.system.Leveling.utils.NotificationHelper.sendSuccess(pRef3, "<color:green>Objectif valide !</color>");
            }
        } else {
            // Mode quete : verifier si deja acceptee
            boolean alreadyAccepted = false;
            for (PlayerQuest pq : QuestManager.getPlayerQuests(playerUUID)) {
                if (pq.getQuestId().equals(quest.getId())) {
                    alreadyAccepted = true;
                    break;
                }
            }

            if (alreadyAccepted) {
                // Pour MINAGE/RECOLTE : verifier inventaire et completer si assez d'items
                if (quest.getType() == com.eldanior.system.quest.QuestType.MINAGE
                        || quest.getType() == com.eldanior.system.quest.QuestType.RECOLTE) {
                    if (player != null && quest.getTargetId() != null) {
                        int count = QuestManager.countItemInInventory(player, quest.getTargetId());
                        if (count >= quest.getTargetAmount()) {
                            // Retirer les items et ouvrir l'ecran de completion
                            QuestManager.removeItemsFromInventory(player, quest.getTargetId(), quest.getTargetAmount());
                            for (PlayerQuest pq2 : QuestManager.getPlayerQuests(playerUUID)) {
                                if (pq2.getQuestId().equals(quest.getId())) {
                                    pq2.setCompleted();
                                    break;
                                }
                            }
                            // Donner recompenses
                            int oldLevel = data.getLevel();
                            data.addExperience(quest.getRewardXP());
                            data.addMoney(quest.getRewardGold());
                            if (quest.getRewardTitleId() != null) data.addTitle(quest.getRewardTitleId());
                            if (quest.getCooldownMinutes() > 0) {
                                QuestManager.setCooldown(playerUUID, quest.getId(), quest.getCooldownMinutes());
                            }
                            // Retirer de la liste
                            QuestManager.getPlayerQuests(playerUUID).removeIf(pq3 -> pq3.getQuestId().equals(quest.getId()) && pq3.isCompleted());
                            // Level up
                            if (data.getLevel() > oldLevel) {
                                int gained = (data.getLevel() - oldLevel) * 3;
                                data.setAttributePoints(data.getAttributePoints() + gained);
                            }
                            data.setQuestData(QuestManager.serializePlayerQuests(playerUUID));
                            data.setCooldownData(QuestManager.serializeCooldowns(playerUUID));
                            store.putComponent(ref, type, data);
                            com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, data);
                            PlayerRef pRefClaim = store.getComponent(ref, PlayerRef.getComponentType());
                            if (pRefClaim != null) {
                                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pRefClaim,
                                        "QUETE TERMINEE", quest.getName(), true);
                                com.eldanior.system.Leveling.utils.NotificationHelper.sendSuccess(pRefClaim,
                                        "<color:green>+" + quest.getRewardXP() + " XP</color> <color:gold>+" + quest.getRewardGold() + " Or</color>");
                                if (data.getLevel() > oldLevel) {
                                    com.eldanior.system.Leveling.utils.NotificationHelper.showLevelUpTitle(pRefClaim, data.getLevel());
                                }
                            }
                            this.close();
                            return;
                        } else {
                            if (player != null) player.getPlayerRef().sendMessage(
                                    Message.raw("Il vous manque des items ! " + count + "/" + quest.getTargetAmount()));
                            this.close();
                            return;
                        }
                    }
                }
                if (player != null) player.getPlayerRef().sendMessage(Message.raw("Quete deja en cours !"));
                this.close();
                return;
            }

            // Accepter et activer la quete
            QuestManager.acceptQuest(playerUUID, quest.getId());
            QuestManager.activateQuest(playerUUID, quest.getId());

            data.setQuestData(QuestManager.serializePlayerQuests(playerUUID));
            store.putComponent(ref, type, data);

            // Event title pour quete acceptee
            PlayerRef pRef2 = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef2 != null) {
                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pRef2,
                        "NOUVELLE QUETE", quest.getName(), true);
            }
        }

        this.close();
    }

    private UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        try { return UUIDExtractor.getUUID(pRef); } catch (Exception e) { return null; }
    }

    public static class DialogueEventData {
        public static final BuilderCodec<DialogueEventData> CODEC = BuilderCodec.builder(DialogueEventData.class, DialogueEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}
