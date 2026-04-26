package com.eldanior.system.classes.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ClassSelectionScreen extends InteractiveCustomUIPage<ClassSelectionScreen.ClassEventData> {

    private static final int MAX_REROLLS = 2;

    private List<ClassModel> baseClasses;
    private List<String> rolledClassIds;
    private final boolean isEvolution;
    private int rerollsUsed;
    private boolean isAdmin;

    public ClassSelectionScreen(@Nonnull PlayerRef playerRef, @Nonnull List<String> rolledClassIds, boolean isEvolution) {
        this(playerRef, rolledClassIds, isEvolution, 0, false);
    }

    public ClassSelectionScreen(@Nonnull PlayerRef playerRef, @Nonnull List<String> rolledClassIds, boolean isEvolution, int rerollsUsed, boolean isAdmin) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClassEventData.CODEC);
        this.isEvolution = isEvolution;
        this.rolledClassIds = new ArrayList<>(rolledClassIds);
        this.rerollsUsed = rerollsUsed;
        this.isAdmin = isAdmin;
        this.baseClasses = resolveModels(rolledClassIds);
    }

    private static List<ClassModel> resolveModels(List<String> ids) {
        List<ClassModel> models = new ArrayList<>();
        for (String id : ids) {
            ClassModel model = ClassManager.get(id);
            if (model != null && !model.isAdminAccess()) {
                models.add(model);
            }
        }
        return models;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Classes/ClassSelection.ui");

        // Remplir les cartes
        populateCards(ui);

        // Event bindings pour les boutons CHOISIR (on envoie l'index, pas l'ID,
        // car les IDs changent au reroll mais les bindings sont fixes)
        for (int i = 0; i < 5; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#CBtn" + i,
                    EventData.of("Action", "selectClass").append("ClassId", String.valueOf(i)));
        }

        // Reroll : visible uniquement en mode evolution
        if (isEvolution) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#RerollButton",
                    EventData.of("Action", "reroll"));
            updateRerollDisplay(ui);
        }

        // Sauvegarder les choix proposes + rerolls (pour les retrouver si le joueur ferme)
        if (isEvolution) {
            try {
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData playerData = store.getComponent(ref, type);
                if (playerData != null) {
                    playerData.setSavedEvolutionChoices(this.rolledClassIds);
                    store.putComponent(ref, type, playerData);
                }
            } catch (Exception e) {
                System.err.println("[ClassSelection] Erreur sauvegarde choix: " + e.getMessage());
            }
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull ClassEventData data) {
        if (data.action == null) return;

        // === REROLL ===
        if ("reroll".equals(data.action)) {
            if (!isAdmin && rerollsUsed >= MAX_REROLLS) return;

            try {
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData playerData = store.getComponent(ref, type);
                if (playerData == null) return;

                ClassModel currentClass = ClassManager.get(playerData.getPlayerClassId());
                if (currentClass == null) return;

                List<String> possibleEvolutions = currentClass.getNextClassId();
                if (possibleEvolutions == null || possibleEvolutions.isEmpty()) return;

                // Nouveau tirage (1 choix pour tier 2, 3 pour tier 1)
                int numChoices = currentClass.getPromotionLevel() >= 400 ? 1 : 3;
                List<String> newChoices = OpenClassSelectionInteraction.performGachaRoll(possibleEvolutions, numChoices);
                this.rolledClassIds = newChoices;
                this.baseClasses = resolveModels(newChoices);
                this.rerollsUsed++;

                // Sauvegarder
                playerData.setSavedEvolutionChoices(newChoices);
                playerData.useReroll();
                store.putComponent(ref, type, playerData);

                // Rafraichir toute l'interface
                UICommandBuilder update = new UICommandBuilder();
                populateCards(update);
                updateRerollDisplay(update);
                this.sendUpdate(update);
            } catch (Exception e) {
                System.err.println("[ClassSelection] Erreur reroll: " + e.getMessage());
            }
            return;
        }

        // === SELECTION DE CLASSE ===
        if (!"selectClass".equals(data.action) || data.classId == null) return;

        // data.classId contient l'index de la carte (0, 1, 2...), on resout l'ID actuel
        int cardIndex;
        try { cardIndex = Integer.parseInt(data.classId); } catch (NumberFormatException e) { return; }
        if (cardIndex < 0 || cardIndex >= baseClasses.size()) return;

        ClassModel model = baseClasses.get(cardIndex);

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData playerData = store.getComponent(ref, type);
        if (playerData == null) playerData = new PlayerLevelData();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (!isEvolution && playerData.getPlayerClassId().equalsIgnoreCase(model.getId())) {
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef,
                        "<color:yellow>Vous etes deja " + model.getDisplayName() + " !</color>",
                        NotificationStyle.Warning);
            }
            return;
        }

        playerData.setPlayerClass(model.getDisplayName());
        playerData.setPlayerClassId(model.getId());
        playerData.clearSavedEvolutionChoices();
        StatCalculator.updatePlayerStats(ref, store, playerData);
        store.putComponent(ref, type, playerData);

        if (playerRef != null) {
            NotificationHelper.showEventTitle(playerRef, "NOUVELLE CLASSE", model.getDisplayName(), true);
        }

        this.close();
    }

    private void populateCards(UICommandBuilder ui) {
        for (int i = 0; i < 5; i++) {
            if (i < baseClasses.size()) {
                ClassModel model = baseClasses.get(i);
                String p = "#ClassCard" + i;
                String rarityColor = getRarityHexColor(model.getRarity());

                ui.set(p + ".Visible", true);

                ui.set("#CName" + i + ".Text", model.getDisplayName());
                ui.set("#CName" + i + ".Style.TextColor", rarityColor);
                ui.set("#CRarity" + i + ".Text", model.getRarity().name());
                ui.set("#CRarity" + i + ".Style.TextColor", rarityColor);
                ui.set("#CType" + i + ".Text", model.getType().getLabel());
                ui.set("#CDesc" + i + ".Text", model.getDescription());

                ui.set("#CStat" + i + "a.Text", "STR +" + model.getBonusStr());
                ui.set("#CStat" + i + "b.Text", "VIT +" + model.getBonusVit());
                ui.set("#CStat" + i + "c.Text", "INT +" + model.getBonusInt());
                ui.set("#CStat" + i + "d.Text", "END +" + model.getBonusEnd());
                ui.set("#CStat" + i + "e.Text", "AGL +" + model.getBonusAgl());
                ui.set("#CStat" + i + "f.Text", "LCK +" + model.getBonusLck());

                List<PassiveSkill> skills = model.getSkillsPassiveIds();
                if (skills != null) {
                    for (int s = 0; s < 3; s++) {
                        String skillLabel = "#CSkill" + i + (char) ('a' + s);
                        if (s < skills.size()) {
                            ui.set(skillLabel + ".Text", "- " + skills.get(s).getDisplayName());
                        } else {
                            ui.set(skillLabel + ".Text", "");
                        }
                    }
                }
            } else {
                ui.set("#ClassCard" + i + ".Visible", false);
            }
        }
    }

    private void updateRerollDisplay(UICommandBuilder ui) {
        if (isAdmin) {
            ui.set("#RerollRow.Visible", true);
            ui.set("#RerollLabel.Text", "Relancer (Admin)");
        } else {
            int remaining = MAX_REROLLS - rerollsUsed;
            if (remaining > 0) {
                ui.set("#RerollRow.Visible", true);
                ui.set("#RerollLabel.Text", "Relancer (" + remaining + " restant" + (remaining > 1 ? "s" : "") + ")");
            } else {
                ui.set("#RerollRow.Visible", false);
            }
        }
    }

    private String getRarityHexColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "#E0E0E0";
            case RARE -> "#3498DB";
            case EPIC -> "#9B59B6";
            case UNIQUE -> "#E74C3C";
            case LEGENDARY -> "#F1C40F";
            case DIVINE -> "#00FFFF";
            default -> "#FFFFFF";
        };
    }

    public static class ClassEventData {
        public String action;
        public String classId;

        public static final BuilderCodec<ClassEventData> CODEC =
                BuilderCodec.builder(ClassEventData.class, ClassEventData::new)
                        .addField(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action)
                        .addField(new KeyedCodec<>("ClassId", Codec.STRING), (d, v) -> d.classId = v, d -> d.classId)
                        .build();
    }
}