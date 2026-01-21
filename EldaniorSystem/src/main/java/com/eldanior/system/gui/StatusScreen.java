package com.eldanior.system.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message; // Import Important !
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class StatusScreen extends InteractiveCustomUIPage<StatusScreen.StatusEventData> {

    public StatusScreen(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, StatusEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {

        // 1. CHEMIN: Le fichier doit être dans resources/Common/UI/Custom/Status.ui
        uiCommandBuilder.append("status.ui");

        // Récupération des données
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) data = new PlayerLevelData();

        String playerName = getPlayerName(ref, store);

        // 2. TEXTE: On utilise .TextSpans et Message.raw() comme dans MyUI
        uiCommandBuilder.set("#NameText.TextSpans", Message.raw("NAME: " + playerName));
        uiCommandBuilder.set("#JobText.TextSpans", Message.raw("JOB: " + data.getPlayerClass()));
        uiCommandBuilder.set("#TitleText.TextSpans", Message.raw("TITLE: " + data.getCurrentTitle()));
        uiCommandBuilder.set("#LevelText.TextSpans", Message.raw(String.valueOf(data.getLevel())));

        uiCommandBuilder.set("#HpText.TextSpans", Message.raw("HP: " + (data.getVitality() * 10)));
        uiCommandBuilder.set("#MpText.TextSpans", Message.raw("MP: " + (data.getIntelligence() * 10)));

        uiCommandBuilder.set("#StrVal.TextSpans", Message.raw(String.valueOf(data.getStrength())));
        uiCommandBuilder.set("#VitVal.TextSpans", Message.raw(String.valueOf(data.getVitality())));
        uiCommandBuilder.set("#IntVal.TextSpans", Message.raw(String.valueOf(data.getIntelligence())));

        uiCommandBuilder.set("#PointsText.TextSpans", Message.raw("Points: " + data.getAttributePoints()));
        uiCommandBuilder.set("#MoneyText.TextSpans", Message.raw("COINS: " + data.getMoney()));

        // Evenements
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnStr", EventData.of("Action", "str"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVit", EventData.of("Action", "vit"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnInt", EventData.of("Action", "int"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull StatusEventData data) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData playerData = store.getComponent(ref, type);
        if (playerData == null) return;

        if (playerData.getAttributePoints() > 0) {
            boolean changed = false;

            if ("str".equals(data.action)) {
                playerData.setStrength(playerData.getStrength() + 1);
                changed = true;
            } else if ("vit".equals(data.action)) {
                playerData.setVitality(playerData.getVitality() + 1);
                changed = true;
            } else if ("int".equals(data.action)) {
                playerData.setIntelligence(playerData.getIntelligence() + 1);
                changed = true;
            }

            if (changed) {
                playerData.setAttributePoints(playerData.getAttributePoints() - 1);

                // On met à jour avec la même méthode (Message.raw)
                UICommandBuilder update = new UICommandBuilder();
                update.set("#StrVal.TextSpans", Message.raw(String.valueOf(playerData.getStrength())));
                update.set("#VitVal.TextSpans", Message.raw(String.valueOf(playerData.getVitality())));
                update.set("#IntVal.TextSpans", Message.raw(String.valueOf(playerData.getIntelligence())));
                update.set("#PointsText.TextSpans", Message.raw("Points: " + playerData.getAttributePoints()));

                this.sendUpdate(update);
            }
        }
    }

    private String getPlayerName(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            PlayerRef info = store.getComponent(ref, PlayerRef.getComponentType());
            Field f = PlayerRef.class.getDeclaredField("name");
            f.setAccessible(true);
            return (String) f.get(info);
        } catch (Exception e) { return "Player"; }
    }

    public static class StatusEventData {
        public static final BuilderCodec<StatusEventData> CODEC = BuilderCodec.builder(StatusEventData.class, StatusEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}