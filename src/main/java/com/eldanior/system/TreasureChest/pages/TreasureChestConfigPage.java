package com.eldanior.system.TreasureChest.pages;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.resources.TreasureChestConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TreasureChestConfigPage extends InteractiveCustomUIPage<TreasureChestConfigPage.TreasureConfigData> {

    public TreasureChestConfigPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, TreasureConfigData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("TreasureChest/TreasureChestConfigPage.ui");

        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        // REMPLACER : 'votreResourceType' par l'instance du type de ressource enregistrée dans votre plugin
        assert player != null;
        assert player.getWorld() != null;
        TreasureChestConfig config = (TreasureChestConfig) player.getWorld().getChunkStore().getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE); // Ou votre instance de ResourceType

        List<DropdownEntryInfo> entries = new ArrayList<>();
        entries.add(new DropdownEntryInfo(LocalizableString.fromString("true"), "true"));
        entries.add(new DropdownEntryInfo(LocalizableString.fromString("false"), "false"));

        // Dropdown : Cassage de coffre
        uiCommandBuilder.set("#CanPlayerBreakLootChestsDropdown.Entries", entries);
        uiCommandBuilder.set("#CanPlayerBreakLootChestsDropdown.Value", String.valueOf(config.isCanPlayerBreakLootChests()));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#CanPlayerBreakLootChestsDropdown", EventData.of("Config", "CanPlayerBreakLootChests").append("@DropdownValue", "#CanPlayerBreakLootChestsDropdown.Value"), false);

        // Dropdown : Loot Aléatoire
        uiCommandBuilder.set("#IsLootRandomDropdown.Entries", entries);
        uiCommandBuilder.set("#IsLootRandomDropdown.Value", String.valueOf(config.isLootChestRandom()));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#IsLootRandomDropdown", EventData.of("Config", "IsLootRandom").append("@DropdownValue", "#IsLootRandomDropdown.Value"), false);

        // Dropdown : Message à l'ouverture
        uiCommandBuilder.set("#IsMessageAppearDropdown.Entries", entries);
        uiCommandBuilder.set("#IsMessageAppearDropdown.Value", String.valueOf(config.isMessageAppear()));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#IsMessageAppearDropdown", EventData.of("Config", "IsMessageAppear").append("@DropdownValue", "#IsMessageAppearDropdown.Value"), false);

        // Dropdown : Particules
        uiCommandBuilder.set("#IsParticlesAppearDropdown.Entries", entries);
        uiCommandBuilder.set("#IsParticlesAppearDropdown.Value", String.valueOf(config.isParticlesAppear()));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#IsParticlesAppearDropdown", EventData.of("Config", "IsParticlesAppear").append("@DropdownValue", "#IsParticlesAppearDropdown.Value"), false);

        // Champ Numérique : Intervalle de Reset
        uiCommandBuilder.set("#NextLootResetIntervalNumberField.Value", config.getNextLootResetInterval());
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#NextLootResetIntervalNumberField", (new EventData()).append("@Days", "#NextLootResetIntervalNumberField.Value"), false);

        // Color Picker : Couleur des particules
        uiCommandBuilder.set("#ParticlesColorPicker.Value", config.getParticlesColor());
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ParticlesColorPicker", (new EventData()).append("@Color", "#ParticlesColorPicker.Value"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull TreasureConfigData data) {
        super.handleDataEvent(ref, store, data);

        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        assert player != null;
        assert player.getWorld() != null;
        TreasureChestConfig config = (TreasureChestConfig) player.getWorld().getChunkStore().getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE);

        // Gestion des Dropdowns (Boolean)
        if (data.config != null) {
            boolean boolValue = Boolean.parseBoolean(data.dropdownValue);
            switch (data.config) {
                case "CanPlayerBreakLootChests" -> config.setCanPlayerBreakLootChests(boolValue);
                case "IsLootRandom" -> config.setLootRandom(boolValue);
                case "IsMessageAppear" -> config.setMessageAppear(boolValue);
                case "IsParticlesAppear" -> config.setParticlesAppear(boolValue);
            }
        }

        // Gestion du Reset Interval (Jours)
        if (data.days != config.getNextLootResetInterval()) {
            if (data.days > 0) {
                int currentEpochDay = (int) LocalDate.now().toEpochDay();
                config.setNextLootResetInterval(data.days);
                config.setNextLootReset(currentEpochDay + data.days);
            } else {
                config.setNextLootResetInterval(0);
                config.setNextLootReset(-1);
            }
        }

        // Gestion de la couleur
        if (data.color != null) {
            config.setParticlesColor(data.color);
        }
    }

    // --- Data Class ---
    public static class TreasureConfigData {
        private String config;
        private String dropdownValue;
        private String color;
        private int days;

        public static final BuilderCodec<TreasureConfigData> CODEC = BuilderCodec.builder(TreasureConfigData.class, TreasureConfigData::new)
                .addField(new KeyedCodec<>("Config", Codec.STRING), (d, v) -> d.config = v, (d) -> d.config)
                .addField(new KeyedCodec<>("@DropdownValue", Codec.STRING), (d, v) -> d.dropdownValue = v, (d) -> d.dropdownValue)
                .addField(new KeyedCodec<>("@Color", Codec.STRING), (d, v) -> d.color = v, (d) -> d.color)
                .addField(new KeyedCodec<>("@Days", Codec.INTEGER), (d, v) -> d.days = v, (d) -> d.days)
                .build();
    }
}