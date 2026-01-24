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
import com.hypixel.hytale.server.core.Message;
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

        uiCommandBuilder.append("status.ui");

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) data = new PlayerLevelData();

        // --- CORRECTION 1 : SUPPRESSION DE LA BOUCLE WHILE ---
        // On ne calcule plus le level up ici. L'UI se contente de lire ce qui est stocké.

        int currentHp = data.getMaxHealth();
        int currentMp = data.getMaxMana();

        String playerName = getPlayerName(ref, store);

        uiCommandBuilder.set("#NameText.TextSpans", Message.raw("NOM: " + playerName));
        uiCommandBuilder.set("#JobText.TextSpans", Message.raw("CLASSE: " + data.getPlayerClass()));
        uiCommandBuilder.set("#TitleText.TextSpans", Message.raw("TITRE: " + data.getCurrentTitle()));
        uiCommandBuilder.set("#LevelText.TextSpans", Message.raw(String.valueOf(data.getLevel())));

        String xpInfo = "LEVEL: " + data.getLevel() + " XP: " + data.getExperience() + " / " + data.getRequiredExperience();
        uiCommandBuilder.set("#LevelLabel.TextSpans", Message.raw(xpInfo));
        uiCommandBuilder.set("#ProgressBar.Value", data.getExperienceProgress());

        uiCommandBuilder.set("#MpText.TextSpans", Message.raw("MP: " + currentMp + " / " + data.getMaxMana()));
        uiCommandBuilder.set("#MpProgressBar.Value", currentMp / (float) data.getMaxMana());

        uiCommandBuilder.set("#StrVal.TextSpans", Message.raw("FOR: " + data.getStrength() + " (+20)"));
        uiCommandBuilder.set("#VitVal.TextSpans", Message.raw("VIE: " + data.getVitality() + " (+20)"));
        uiCommandBuilder.set("#IntVal.TextSpans", Message.raw("INT: " + data.getIntelligence() + " (+20)"));

        uiCommandBuilder.set("#PerVal.TextSpans", Message.raw("END: " + data.getEndurance() + " (+20)"));
        uiCommandBuilder.set("#AglVal.TextSpans", Message.raw("AGL: " + data.getAgility() + " (+20)"));
        uiCommandBuilder.set("#CmdVal.TextSpans", Message.raw("CH: " + data.getLuck() + " (+20)"));

        uiCommandBuilder.set("#PointsText.TextSpans", Message.raw("Points disponibles: " + data.getAttributePoints()));
        uiCommandBuilder.set("#MoneyText.TextSpans", Message.raw(": " + data.getMoney()));

        // Bindings des boutons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnStr", EventData.of("Action", "str"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVit", EventData.of("Action", "vit"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnInt", EventData.of("Action", "int"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnPer", EventData.of("Action", "per"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnAgl", EventData.of("Action", "agl"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCmd", EventData.of("Action", "cmd"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull StatusEventData data) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

        // On récupère les données
        PlayerLevelData playerData = store.getComponent(ref, type);
        if (playerData == null) return;

        // On effectue les changements si possible
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
            } else if ("per".equals(data.action)) {
                playerData.setEndurance(playerData.getEndurance() + 1);
                changed = true;
            } else if ("agl".equals(data.action)) {
                playerData.setAgility(playerData.getAgility() + 1);
                changed = true;
            } else if ("cmd".equals(data.action)) {
                playerData.setLuck(playerData.getLuck() + 1);
                changed = true;
            }

            if (changed) {
                // On décrémente les points
                playerData.setAttributePoints(playerData.getAttributePoints() - 1);

                // --- CORRECTION 2 : SAUVEGARDE EXPLICITE ---
                // Très important ! Sans ça, les points sont perdus au redémarrage
                store.putComponent(ref, type, playerData);

                // On met à jour l'affichage immédiatement pour le joueur
                UICommandBuilder update = new UICommandBuilder();
                update.set("#StrVal.TextSpans", Message.raw("FOR: " + playerData.getStrength() + " (+20)"));
                update.set("#VitVal.TextSpans", Message.raw("VIT: " + playerData.getVitality() + " (+20)"));
                update.set("#IntVal.TextSpans", Message.raw("INT: " + playerData.getIntelligence() + " (+20)"));
                update.set("#PerVal.TextSpans", Message.raw("END: " + playerData.getEndurance() + " (+20)"));
                update.set("#AglVal.TextSpans", Message.raw("AGL: " + playerData.getAgility() + " (+20)"));
                update.set("#CmdVal.TextSpans", Message.raw("CH: " + playerData.getLuck() + " (+20)"));
                update.set("#PointsText.TextSpans", Message.raw("Points disponibles: " + playerData.getAttributePoints()));

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