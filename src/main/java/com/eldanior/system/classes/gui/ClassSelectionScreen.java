package com.eldanior.system.classes.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
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
import java.util.Objects;

public class ClassSelectionScreen extends InteractiveCustomUIPage<ClassSelectionScreen.ClassEventData> {

    private final List<ClassModel> baseClasses;
    private final boolean isEvolution;

    public ClassSelectionScreen(@Nonnull PlayerRef playerRef, @Nonnull List<ClassModel> baseClasses, boolean isEvolution) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClassEventData.CODEC);
        this.baseClasses = baseClasses;
        this.isEvolution = isEvolution;

        System.out.println("[DEBUG] Classes base trouvées : " + baseClasses.size());
        baseClasses.forEach(m -> System.out.println(" - " + m.getId() + " rarity=" + m.getRarity() + " nextClassId=" + m.getNextClassId()));
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("Classes/ClassSelection.ui");

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        String currentClassId = (data != null) ? data.getPlayerClassId() : "novice";

        int index = 0;

        for (ClassModel model : this.baseClasses) {
            if (model.isAdminAccess()) continue;

            String tileId = "#ClassTile" + index;

            commands.set(tileId + " #ClassLabel.Text", model.getDisplayName());
            commands.set(tileId + " #CategoryIcon.ItemId", getIconForClass(model.getId()));

            System.out.println("[DEBUG] Binding Activating sur : " + tileId);

            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    tileId,
                    EventData.of("Action", "selectClass").append("ClassId", model.getId())
            );

            index++;
        }

        for (int i = index; i < 6; i++) {
            commands.set("#ClassTile" + i + ".Visible", false);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull ClassEventData data) {
        System.out.println("[DEBUG] : == > 1 ");

        if (!"selectClass".equals(data.action) || data.classId == null) return;

        System.out.println("[DEBUG] : == > 2 ");

        ClassModel model = ClassManager.get(data.classId);
        if (model == null) return;

        System.out.println("[DEBUG] : == > 3 ");

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

        System.out.println("[DEBUG] : == > 4 ");

        PlayerLevelData playerData = store.getComponent(ref, type);

        System.out.println("[DEBUG] : == > 5 ");

        if (playerData == null) playerData = new PlayerLevelData();

        System.out.println("[DEBUG] : == > 6 ");

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        System.out.println("[DEBUG] : == > 7 ");

        if (!isEvolution && playerData.getPlayerClassId().equalsIgnoreCase(data.classId)) {
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef,
                        "<color:yellow>Vous êtes déjà " + model.getDisplayName() + " !</color>",
                        NotificationStyle.Warning);
            }
            return;
        }

        System.out.println("[DEBUG] : == > 8 ");


        playerData.setPlayerClass(model.getDisplayName());
        playerData.setPlayerClassId(model.getId());
        playerData.forgetAllSkills();

        System.out.println("[DEBUG] : == > 9 ");

        StatCalculator.updatePlayerStats(ref, store, playerData);

        System.out.println("[DEBUG] : == > 10 ");

        store.putComponent(ref, type, playerData);

        System.out.println("[DEBUG] : == > 11 ");

        if (playerRef != null) {
            NotificationHelper.sendNotification(playerRef,
                    "<color:green>Classe choisie : </color><color:gold>" + model.getDisplayName() + "</color>",
                    NotificationStyle.Success);
        }

        System.out.println("[DEBUG] : == > 12 ");

        this.close();
    }

    private String getIconForClass(String classId) {
        return switch (classId) {
            case "warrior"  -> "Weapon_Sword_Crude";
            case "mage"     -> "fireball";
            case "assassin" -> "Weapon_Daggers_Crude";
            case "archer"   -> "Weapon_Shortbow_Bomb";
            case "merchant" -> "Deco_Treasure";
            default         -> "Plant_Fruit_Apple";
        };
    }

    public static class ClassEventData {
        public String action;
        public String classId;

        public static final BuilderCodec<ClassEventData> CODEC =
                BuilderCodec.builder(ClassEventData.class, ClassEventData::new)
                        .addField(new KeyedCodec<>("Action",  Codec.STRING), (d, v) -> d.action  = v, d -> d.action)
                        .addField(new KeyedCodec<>("ClassId", Codec.STRING), (d, v) -> d.classId = v, d -> d.classId)
                        .build();
    }
}