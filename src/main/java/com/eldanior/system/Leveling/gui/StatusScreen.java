package com.eldanior.system.Leveling.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData; // Assure-toi que c'est le bon import
import com.eldanior.system.Leveling.utils.StatCalculator; // Import du calculateur
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
        // --- Construction Initiale (inchangée) ---
        uiCommandBuilder.append("Status/status.ui");

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) data = new PlayerLevelData();

        StatCalculator.updatePlayerStats(ref, store, data);

        ClassModel classModel = ClassManager.get(data.getPlayerClassId());
        int bStr = (classModel != null) ? classModel.getBonusStr() : 0;
        int bVit = (classModel != null) ? classModel.getBonusVit() : 0;
        int bInt = (classModel != null) ? classModel.getBonusInt() : 0;
        int bEnd = (classModel != null) ? classModel.getBonusEnd() : 0;
        int bAgl = (classModel != null) ? classModel.getBonusAgl() : 0;
        int bLck = (classModel != null) ? classModel.getBonusLck() : 0;

        String playerName = getPlayerName(ref, store);

        // Header
        uiCommandBuilder.set("#ClassText.TextSpans", Message.raw(data.getPlayerClass()));
        uiCommandBuilder.set("#NameText.TextSpans", Message.raw("NOM : " + playerName));
        uiCommandBuilder.set("#TitleText.TextSpans", Message.raw("TITRE : " + data.getCurrentTitle()));

        // Niveau
        uiCommandBuilder.set("#LevelText.TextSpans", Message.raw(String.valueOf(data.getLevel())));
        uiCommandBuilder.set("#LevelLabel.TextSpans", Message.raw("XP : " + data.getExperience() + " / " + data.getRequiredExperience()));
        uiCommandBuilder.set("#ProgressBar.Value", data.getExperienceProgress());

        // Affichage initial des barres (Mana/Vie)
        updateResourceBars(ref, store, uiCommandBuilder);

        // Stats
        uiCommandBuilder.set("#StrVal.TextSpans", Message.raw("FORCE  " + data.getStrength() + "  (+" + bStr + ")"));
        uiCommandBuilder.set("#VitVal.TextSpans", Message.raw("VITALITE  " + data.getVitality() + "  (+" + bVit + ")"));
        uiCommandBuilder.set("#IntVal.TextSpans", Message.raw("INTELLIGENCE  " + data.getIntelligence() + "  (+" + bInt + ")"));
        uiCommandBuilder.set("#PerVal.TextSpans", Message.raw("ENDURANCE  " + data.getEndurance() + "  (+" + bEnd + ")"));
        uiCommandBuilder.set("#AglVal.TextSpans", Message.raw("AGILITE  " + data.getAgility() + "  (+" + bAgl + ")"));
        uiCommandBuilder.set("#CmdVal.TextSpans", Message.raw("CHANCE   " + data.getLuck() + "  (+" + bLck + ")"));

        // Footer
        uiCommandBuilder.set("#PointsText.TextSpans", Message.raw("Points Disponibles : " + data.getAttributePoints()));
        uiCommandBuilder.set("#MoneyText.TextSpans", Message.raw(" " + data.getMoney()));

        // Bindings Stats
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnStr", EventData.of("Action", "str"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVit", EventData.of("Action", "vit"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnInt", EventData.of("Action", "int"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnPer", EventData.of("Action", "per"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnAgl", EventData.of("Action", "agl"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCmd", EventData.of("Action", "cmd"));

        // Bindings Nav
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnShop", EventData.of("Action", "nav_shop"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnInventory", EventData.of("Action", "nav_inventory"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnStatus", EventData.of("Action", "nav_status"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSkills", EventData.of("Action", "nav_skills"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClasses", EventData.of("Action", "nav_classes"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull StatusEventData data) {
        if (data.action == null) return;

        // --- Navigation ---
        if (data.action.startsWith("nav_")) {
            handleNavigation(ref, store, data.action);
            return;
        }

        // --- Logique d'ajout de points ---
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData playerData = store.getComponent(ref, type);

        // Vérification de sécurité
        if (playerData == null || playerData.getAttributePoints() <= 0) return;

        boolean changed = false;
        switch (data.action) {
            case "str" -> { playerData.setStrength(playerData.getStrength() + 1); changed = true; }
            case "vit" -> { playerData.setVitality(playerData.getVitality() + 1); changed = true; }
            case "int" -> { playerData.setIntelligence(playerData.getIntelligence() + 1); changed = true; }
            case "per" -> { playerData.setEndurance(playerData.getEndurance() + 1); changed = true; }
            case "agl" -> { playerData.setAgility(playerData.getAgility() + 1); changed = true; }
            case "cmd" -> { playerData.setLuck(playerData.getLuck() + 1); changed = true; }
        }

        if (!changed) return;

        // 1. Déduire le point
        playerData.setAttributePoints(playerData.getAttributePoints() - 1);

        // 2. Sauvegarder les données
        store.putComponent(ref, type, playerData);

        // 4. Mettre à jour l'écran (UI)
        sendUIUpdate(ref, store, playerData);
    }

    /**
     * Méthode séparée pour rafraîchir l'interface proprement
     */
    private void sendUIUpdate(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData playerData) {
        ClassModel classModel = ClassManager.get(playerData.getPlayerClassId());
        int bStr = (classModel != null) ? classModel.getBonusStr() : 0;
        int bVit = (classModel != null) ? classModel.getBonusVit() : 0;
        int bInt = (classModel != null) ? classModel.getBonusInt() : 0;
        int bEnd = (classModel != null) ? classModel.getBonusEnd() : 0;
        int bAgl = (classModel != null) ? classModel.getBonusAgl() : 0;
        int bLck = (classModel != null) ? classModel.getBonusLck() : 0;

        UICommandBuilder update = new UICommandBuilder();

        // Update Textes Stats
        update.set("#StrVal.TextSpans", Message.raw("FORCE  " + playerData.getStrength() + "  (+" + bStr + ")"));
        update.set("#VitVal.TextSpans", Message.raw("VITALITE  " + playerData.getVitality() + "  (+" + bVit + ")"));
        update.set("#IntVal.TextSpans", Message.raw("INTELLIGENCE  " + playerData.getIntelligence() + "  (+" + bInt + ")"));
        update.set("#PerVal.TextSpans", Message.raw("ENDURANCE  " + playerData.getEndurance() + "  (+" + bEnd + ")"));
        update.set("#AglVal.TextSpans", Message.raw("AGILITE  " + playerData.getAgility() + "  (+" + bAgl + ")"));
        update.set("#CmdVal.TextSpans", Message.raw("CHANCE   " + playerData.getLuck() + "  (+" + bLck + ")"));
        update.set("#PointsText.TextSpans", Message.raw("Points Disponibles : " + playerData.getAttributePoints()));

        // Update Barres (Mana/Vie) car Max Mana/Vie a peut-être changé !
        updateResourceBars(ref, store, update);

        this.sendUpdate(update);
    }

    /**
     * Helper pour mettre à jour les barres de ressource (Mana / Vie...)
     */
    private void updateResourceBars(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder builder) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        // Mana
        float currentMp = 0, maxMp = 100;
        int manaIndex = DefaultEntityStatTypes.getMana();
        if (statMap.get(manaIndex) != null) {
            currentMp = Objects.requireNonNull(statMap.get(manaIndex)).get();
            maxMp = Objects.requireNonNull(statMap.get(manaIndex)).getMax();
        }
        builder.set("#MpText.TextSpans", Message.raw((int) currentMp + " / " + (int) maxMp));
        builder.set("#MpProgressBar.Value", maxMp > 0 ? currentMp / maxMp : 0.0f);

        // Note: Tu peux ajouter la Vie ici de la même façon si tu as une barre de vie dans ton UI
    }

    private void handleNavigation(Ref<EntityStore> ref, Store<EntityStore> store, String action) {
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        switch (action) {
            case "nav_shop" -> {
                playerRef.sendMessage(Message.raw("§e[TODO] Ouverture du Shop..."));
                this.close();
            }
            case "nav_inventory" -> {
                playerRef.sendMessage(Message.raw("§e[TODO] Ouverture de l'Inventaire..."));
                this.close();
            }
            case "nav_skills" -> {
                playerRef.sendMessage(Message.raw("§e[TODO] Ouverture des Skills..."));
                this.close();
            }
            case "nav_classes" -> {
                playerRef.sendMessage(Message.raw("§e[TODO] Ouverture des Classes..."));
                this.close();
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