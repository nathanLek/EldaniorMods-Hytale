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

// IMPORTS AJOUTÉS POUR LIRE LES VRAIES STATS
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

        // 1. Récupération du pseudo (Méthode corrigée)
        String playerName = getPlayerName(ref, store);

        // 2. RECUPERATION DES VRAIES STATS HYTALE (PV / MANA ACTUELS)
        // On va chercher dans le moteur de jeu ce qui s'affiche réellement sur ton HUD
        EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());

        float currentHp = 0;
        // float maxHp = 100; // Pas utilisé dans l'affichage actuel mais dispo si besoin
        float currentMp = 0;
        float maxMp = 100;

        if (statMap != null) {
            // Vie
            int healthIndex = DefaultEntityStatTypes.getHealth();
            if (statMap.get(healthIndex) != null) {
                currentHp = Objects.requireNonNull(statMap.get(healthIndex)).get();
                // maxHp = statMap.get(healthIndex).getMax();
            }

            // Mana
            int manaIndex = DefaultEntityStatTypes.getMana();
            if (statMap.get(manaIndex) != null) {
                currentMp = Objects.requireNonNull(statMap.get(manaIndex)).get();   // La valeur qui bouge !
                maxMp = Objects.requireNonNull(statMap.get(manaIndex)).getMax();    // La valeur max (5000 etc.)
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

        // Affichage du Mana corrigé (Casted en int pour faire propre)
        uiCommandBuilder.set("#MpText.TextSpans", Message.raw("MP: " + (int)currentMp + " / " + (int)maxMp));

        // Barre de progression (évite la division par zéro)
        if (maxMp > 0) {
            uiCommandBuilder.set("#MpProgressBar.Value", currentMp / maxMp);
        } else {
            uiCommandBuilder.set("#MpProgressBar.Value", 0.0f);
        }

        uiCommandBuilder.set("#StrVal.TextSpans", Message.raw("FOR: " + data.getStrength() + " (+0)"));
        uiCommandBuilder.set("#VitVal.TextSpans", Message.raw("VIE: " + data.getVitality() + " (+0)"));
        uiCommandBuilder.set("#IntVal.TextSpans", Message.raw("INT: " + data.getIntelligence() + " (+0)"));

        uiCommandBuilder.set("#PerVal.TextSpans", Message.raw("END: " + data.getEndurance() + " (+0)"));
        uiCommandBuilder.set("#AglVal.TextSpans", Message.raw("AGL: " + data.getAgility() + " (+0)"));
        uiCommandBuilder.set("#CmdVal.TextSpans", Message.raw("CH: " + data.getLuck() + " (+0)"));

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
                // 1. On décrémente les points
                playerData.setAttributePoints(playerData.getAttributePoints() - 1);

                // 2. On met à jour les vraies stats du jeu (PV, Mana, Vitesse)
                com.eldanior.system.utils.StatCalculator.updatePlayerStats(ref, store, playerData);

                // 3. On sauvegarde les données RPG
                store.putComponent(ref, type, playerData);

                // 4. On met à jour l'affichage de l'écran
                UICommandBuilder update = new UICommandBuilder();

                // Mettre à jour les textes
                update.set("#StrVal.TextSpans", Message.raw("FOR: " + playerData.getStrength()));
                update.set("#VitVal.TextSpans", Message.raw("VIE: " + playerData.getVitality()));
                update.set("#IntVal.TextSpans", Message.raw("INT: " + playerData.getIntelligence()));
                update.set("#PerVal.TextSpans", Message.raw("END: " + playerData.getEndurance()));
                update.set("#AglVal.TextSpans", Message.raw("AGL: " + playerData.getAgility()));
                update.set("#CmdVal.TextSpans", Message.raw("CH: " + playerData.getLuck()));
                update.set("#PointsText.TextSpans", Message.raw("Points disponibles: " + playerData.getAttributePoints()));

                this.sendUpdate(update);
            }
        }
    }

    // --- CORRECTION DU PSEUDO ---
    private String getPlayerName(Ref<EntityStore> ref, Store<EntityStore> store) {
        // On utilise la méthode officielle getUsername() qui est stable
        PlayerRef info = store.getComponent(ref, PlayerRef.getComponentType());
        if (info != null) {
            return info.getUsername();
        }
        return "Inconnu";
    }

    public static class StatusEventData {
        public static final BuilderCodec<StatusEventData> CODEC = BuilderCodec.builder(StatusEventData.class, StatusEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .build();
        public String action;
    }
}