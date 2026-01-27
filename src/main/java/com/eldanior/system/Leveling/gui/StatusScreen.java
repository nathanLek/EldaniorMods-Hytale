package com.eldanior.system.Leveling.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
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
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;

import javax.annotation.Nonnull;
import java.util.Objects;

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

        // 1. Récupération des bonus de classe
        ClassModel classModel = ClassManager.get(data.getPlayerClassId());
        int bStr = (classModel != null) ? classModel.getBonusStr() : 0;
        int bVit = (classModel != null) ? classModel.getBonusVit() : 0;
        int bInt = (classModel != null) ? classModel.getBonusInt() : 0;
        int bEnd = (classModel != null) ? classModel.getBonusEnd() : 0;
        int bAgl = (classModel != null) ? classModel.getBonusAgl() : 0;
        int bLck = (classModel != null) ? classModel.getBonusLck() : 0;

        // 2. Récupération du pseudo
        String playerName = getPlayerName(ref, store);

        // 3. Récupération des stats HUD (PV / Mana)
        EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        float currentHp = 0, currentMp = 0, maxMp = 100;

        if (statMap != null) {
            int healthIndex = DefaultEntityStatTypes.getHealth();
            if (statMap.get(healthIndex) != null) {
                currentHp = Objects.requireNonNull(statMap.get(healthIndex)).get();
            }
            int manaIndex = DefaultEntityStatTypes.getMana();
            if (statMap.get(manaIndex) != null) {
                currentMp = Objects.requireNonNull(statMap.get(manaIndex)).get();
                maxMp = Objects.requireNonNull(statMap.get(manaIndex)).getMax();
            }
        }

        // --- MISE A JOUR DE L'UI ---
        uiCommandBuilder.set("#NameText.TextSpans", Message.raw("NOM: " + playerName));
        uiCommandBuilder.set("#JobText.TextSpans", Message.raw("CLASSE: " + data.getPlayerClass()));
        uiCommandBuilder.set("#TitleText.TextSpans", Message.raw("TITRE: " + data.getCurrentTitle()));
        uiCommandBuilder.set("#LevelText.TextSpans", Message.raw(String.valueOf(data.getLevel())));

        String xpInfo = "LEVEL: " + data.getLevel() + " XP: " + data.getExperience() + " / " + data.getRequiredExperience();
        uiCommandBuilder.set("#LevelLabel.TextSpans", Message.raw(xpInfo));
        uiCommandBuilder.set("#ProgressBar.Value", data.getExperienceProgress());

        uiCommandBuilder.set("#MpText.TextSpans", Message.raw("MP: " + (int)currentMp + " / " + (int)maxMp));
        uiCommandBuilder.set("#MpProgressBar.Value", maxMp > 0 ? currentMp / maxMp : 0.0f);

        // Affichage des stats avec bonus (+X)
        uiCommandBuilder.set("#StrVal.TextSpans", Message.raw("FOR: " + data.getStrength() + " (+" + bStr + ")"));
        uiCommandBuilder.set("#VitVal.TextSpans", Message.raw("VIE: " + data.getVitality() + " (+" + bVit + ")"));
        uiCommandBuilder.set("#IntVal.TextSpans", Message.raw("INT: " + data.getIntelligence() + " (+" + bInt + ")"));
        uiCommandBuilder.set("#PerVal.TextSpans", Message.raw("END: " + data.getEndurance() + " (+" + bEnd + ")"));
        uiCommandBuilder.set("#AglVal.TextSpans", Message.raw("AGL: " + data.getAgility() + " (+" + bAgl + ")"));
        uiCommandBuilder.set("#CmdVal.TextSpans", Message.raw("CH: " + data.getLuck() + " (+" + bLck + ")"));

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
        PlayerLevelData playerData = store.getComponent(ref, type);
        if (playerData == null) return;

        if (playerData.getAttributePoints() > 0) {
            boolean changed = false;
            if ("str".equals(data.action)) { playerData.setStrength(playerData.getStrength() + 1); changed = true; }
            else if ("vit".equals(data.action)) { playerData.setVitality(playerData.getVitality() + 1); changed = true; }
            else if ("int".equals(data.action)) { playerData.setIntelligence(playerData.getIntelligence() + 1); changed = true; }
            else if ("per".equals(data.action)) { playerData.setEndurance(playerData.getEndurance() + 1); changed = true; }
            else if ("agl".equals(data.action)) { playerData.setAgility(playerData.getAgility() + 1); changed = true; }
            else if ("cmd".equals(data.action)) { playerData.setLuck(playerData.getLuck() + 1); changed = true; }

            if (changed) {
                playerData.setAttributePoints(playerData.getAttributePoints() - 1);
                com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, playerData);
                store.putComponent(ref, type, playerData);

                // Récupération des bonus pour la mise à jour visuelle
                ClassModel classModel = ClassManager.get(playerData.getPlayerClassId());
                int bStr = (classModel != null) ? classModel.getBonusStr() : 0;
                int bVit = (classModel != null) ? classModel.getBonusVit() : 0;
                int bInt = (classModel != null) ? classModel.getBonusInt() : 0;
                int bEnd = (classModel != null) ? classModel.getBonusEnd() : 0;
                int bAgl = (classModel != null) ? classModel.getBonusAgl() : 0;
                int bLck = (classModel != null) ? classModel.getBonusLck() : 0;

                UICommandBuilder update = new UICommandBuilder();
                update.set("#StrVal.TextSpans", Message.raw("FOR: " + playerData.getStrength() + " (+" + bStr + ")"));
                update.set("#VitVal.TextSpans", Message.raw("VIE: " + playerData.getVitality() + " (+" + bVit + ")"));
                update.set("#IntVal.TextSpans", Message.raw("INT: " + playerData.getIntelligence() + " (+" + bInt + ")"));
                update.set("#PerVal.TextSpans", Message.raw("END: " + playerData.getEndurance() + " (+" + bEnd + ")"));
                update.set("#AglVal.TextSpans", Message.raw("AGL: " + playerData.getAgility() + " (+" + bAgl + ")"));
                update.set("#CmdVal.TextSpans", Message.raw("CH: " + playerData.getLuck() + " (+" + bLck + ")"));
                update.set("#PointsText.TextSpans", Message.raw("Points disponibles: " + playerData.getAttributePoints()));

                this.sendUpdate(update);
            }
        }
    }

    private String getPlayerName(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef info = store.getComponent(ref, PlayerRef.getComponentType());
        return (info != null) ? info.getUsername() : "Inconnu";
    }

    public static class StatusEventData {
        public static final BuilderCodec<StatusEventData> CODEC = BuilderCodec.builder(StatusEventData.class, StatusEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}