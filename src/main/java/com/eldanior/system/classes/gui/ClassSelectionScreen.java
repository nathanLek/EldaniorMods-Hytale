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

    private final List<ClassModel> baseClasses;
    private final boolean isEvolution;

    public ClassSelectionScreen(@Nonnull PlayerRef playerRef, @Nonnull List<String> rolledClassIds, boolean isEvolution) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClassEventData.CODEC);
        this.isEvolution = isEvolution;
        this.baseClasses = new ArrayList<>();

        for (String id : rolledClassIds) {
            ClassModel model = ClassManager.get(id);
            if (model != null && !model.isAdminAccess()) {
                this.baseClasses.add(model);
            }
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Classes/ClassSelection.ui");

        for (int i = 0; i < 5; i++) {
            if (i < baseClasses.size()) {
                ClassModel model = baseClasses.get(i);
                String p = "#ClassCard" + i;
                String rarityColor = getRarityHexColor(model.getRarity());

                ui.set(p + ".Visible", true);

                // Nom + rareté
                ui.set("#CName" + i + ".Text", model.getDisplayName());
                ui.set("#CName" + i + ".Style.TextColor", rarityColor);
                ui.set("#CRarity" + i + ".Text", model.getRarity().name());
                ui.set("#CRarity" + i + ".Style.TextColor", rarityColor);

                // Type
                ui.set("#CType" + i + ".Text", model.getType().getLabel());

                // Description
                ui.set("#CDesc" + i + ".Text", model.getDescription());

                // Stats bonus
                ui.set("#CStat" + i + "a.Text", "STR +" + model.getBonusStr());
                ui.set("#CStat" + i + "b.Text", "VIT +" + model.getBonusVit());
                ui.set("#CStat" + i + "c.Text", "INT +" + model.getBonusInt());
                ui.set("#CStat" + i + "d.Text", "END +" + model.getBonusEnd());
                ui.set("#CStat" + i + "e.Text", "AGL +" + model.getBonusAgl());
                ui.set("#CStat" + i + "f.Text", "LCK +" + model.getBonusLck());

                // Skills
                List<PassiveSkill> skills = model.getSkillsPassiveIds();
                if (skills != null) {
                    for (int s = 0; s < 3; s++) {
                        String skillLabel = "#CSkill" + i + (char)('a' + s);
                        if (s < skills.size()) {
                            ui.set(skillLabel + ".Text", "- " + skills.get(s).getDisplayName());
                        } else {
                            ui.set(skillLabel + ".Text", "");
                        }
                    }
                }

                // Event binding
                events.addEventBinding(CustomUIEventBindingType.Activating, "#CBtn" + i,
                        EventData.of("Action", "selectClass").append("ClassId", model.getId()));
            } else {
                ui.set("#ClassCard" + i + ".Visible", false);
            }
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull ClassEventData data) {
        if (!"selectClass".equals(data.action) || data.classId == null) return;

        ClassModel model = ClassManager.get(data.classId);
        if (model == null) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData playerData = store.getComponent(ref, type);
        if (playerData == null) playerData = new PlayerLevelData();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (!isEvolution && playerData.getPlayerClassId().equalsIgnoreCase(data.classId)) {
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef,
                        "<color:yellow>Vous etes deja " + model.getDisplayName() + " !</color>",
                        NotificationStyle.Warning);
            }
            return;
        }

        playerData.setPlayerClass(model.getDisplayName());
        playerData.setPlayerClassId(model.getId());
        StatCalculator.updatePlayerStats(ref, store, playerData);
        store.putComponent(ref, type, playerData);

        if (playerRef != null) {
            NotificationHelper.showEventTitle(playerRef, "NOUVELLE CLASSE", model.getDisplayName(), true);
        }

        this.close();
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
