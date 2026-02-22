package com.eldanior.system.Inventory.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Inventory.components.PlayerPersonalChestData;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.Effects.config.InventoryEffectConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Vector3d;

import javax.annotation.Nonnull;

public class PersonalChestPage extends InteractiveCustomUIPage<PersonalChestPage.ChestEventData> {

    private final PlayerPersonalChestData chestData;

    public PersonalChestPage(@Nonnull PlayerRef playerRef, @Nonnull PlayerPersonalChestData chestData) {
        super(playerRef, CustomPageLifetime.CanDismiss, ChestEventData.CODEC);
        this.chestData = chestData;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("Inventory/PersonalChestWithHotbar.ui");
        spawnOpenEffect(ref, store);
        buildFullUI(ref, store, commands, events);
    }

    private void buildFullUI(Ref<EntityStore> ref, Store<EntityStore> store,
                             UICommandBuilder commands, UIEventBuilder events) {
        buildChestSlots(commands, events);
        buildHotbarSlots(ref, store, commands, events);
        bindNavButtons(events);
    }

    private void buildChestSlots(UICommandBuilder commands, UIEventBuilder events) {
        for (int i = 0; i < PlayerPersonalChestData.CHEST_SIZE; i++) {
            String slotId = "#Slot" + i;
            ItemStack item = chestData.getItem(i);
            renderSlot(commands, slotId, item);
            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    slotId,
                    EventData.of("Action", "takeFromChest").append("SlotIndex", String.valueOf(i))
            );
        }
    }

    private void buildHotbarSlots(Ref<EntityStore> ref, Store<EntityStore> store,
                                  UICommandBuilder commands, UIEventBuilder events) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        ItemContainer hotbar = player.getInventory().getHotbar();
        for (int i = 0; i < 9; i++) {
            String slotId = "#Hotbar" + i;
            ItemStack item = hotbar.getItemStack((short) i);
            renderSlot(commands, slotId, item);
            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    slotId,
                    EventData.of("Action", "depositToChest").append("HotbarSlot", String.valueOf(i))
            );
        }
    }

    private void renderSlot(UICommandBuilder commands, String slotId, ItemStack item) {
        String iconPath = slotId + " #Icon";
        String qtyPath  = slotId + " #Quantity";

        boolean hasItem = item != null && !item.isEmpty();
        commands.set(iconPath + ".Visible", hasItem);

        if (hasItem) {
            commands.set(iconPath + ".ItemId", item.getItemId());
            boolean showQty = item.getQuantity() > 1;
            commands.set(qtyPath + ".Text", showQty ? String.valueOf(item.getQuantity()) : "");
            commands.set(slotId + ".TooltipText", item.getItemId());
        } else {
            commands.set(qtyPath + ".Text", "");
            commands.set(slotId + ".TooltipText", "");
        }
    }

    private void bindNavButtons(UIEventBuilder events) {
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnShop",      EventData.of("Action", "nav_shop"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnInventory", EventData.of("Action", "nav_inventory"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnStatus",    EventData.of("Action", "nav_status"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSkills",    EventData.of("Action", "nav_skills"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClasses",   EventData.of("Action", "nav_classes"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull ChestEventData data) {
        if (data.action == null) return;

        switch (data.action) {
            case "depositToChest" -> {
                if (data.hotbarSlot != null) handleDeposit(ref, store, Integer.parseInt(data.hotbarSlot));
            }
            case "takeFromChest" -> {
                if (data.slotIndex != null) handleTake(ref, store, Integer.parseInt(data.slotIndex));
            }
            case "nav_status" -> {
                this.close();
                Player player = store.getComponent(ref, Player.getComponentType());
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (player != null && playerRef != null) {
                    player.getPageManager().openCustomPage(ref, store,
                            new com.eldanior.system.Leveling.gui.StatusScreen(playerRef));
                }
            }
            case "nav_shop", "nav_inventory", "nav_skills", "nav_classes" -> {
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    player.sendMessage(Message.raw("§e[Navigation] " + data.action + " (à implémenter)"));
                }
            }
        }
    }

    private void handleDeposit(Ref<EntityStore> ref, Store<EntityStore> store, int hotbarSlot) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || hotbarSlot < 0 || hotbarSlot >= 9) return;

        ItemContainer hotbar = player.getInventory().getHotbar();
        ItemStack item = hotbar.getItemStack((short) hotbarSlot);
        if (item == null || item.isEmpty()) return;

        int emptySlot = chestData.findFirstEmptySlot();
        if (emptySlot == -1) {
            player.sendMessage(Message.raw("§cCoffre plein !"));
            return;
        }

        chestData.setItem(emptySlot, item);
        hotbar.removeItemStackFromSlot((short) hotbarSlot);
        persistAndRefresh(ref, store);
        player.sendMessage(Message.raw("§aItem déposé !"));
    }

    private void handleTake(Ref<EntityStore> ref, Store<EntityStore> store, int slotIndex) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || slotIndex < 0 || slotIndex >= PlayerPersonalChestData.CHEST_SIZE) return;

        ItemStack item = chestData.getItem(slotIndex);
        if (item == null || item.isEmpty()) return;

        var result = player.getInventory().getHotbar().addItemStack(item);
        if (result.succeeded()) {
            chestData.clearSlot(slotIndex);
            persistAndRefresh(ref, store);
            player.sendMessage(Message.raw("§aItem récupéré !"));
        } else {
            player.sendMessage(Message.raw("§cInventaire plein !"));
        }
    }

    private void persistAndRefresh(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerPersonalChestData> type =
                EldaniorSystem.get().getPlayerPersonalChestDataType();
        store.putComponent(ref, type, chestData);

        UICommandBuilder update = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        buildFullUI(ref, store, update, events);
        this.sendUpdate(update);
    }

    private void spawnOpenEffect(Ref<EntityStore> ref, Store<EntityStore> store) {
        InventoryEffectConfig config = new InventoryEffectConfig();
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform != null) {
            Vector3d pos = transform.getPosition().add(0, 0.5, 0);
            ParticleUtil.spawnParticleEffect(config.getOpenParticleId(), pos, store);
        }
        if (config.getOpenStatusEffectId() != null && !config.getOpenStatusEffectId().isEmpty()) {
            EffectsManager.applyCustomEffect(ref, config.getOpenStatusEffectId(),
                    config.getOpenEffectDuration(), OverlapBehavior.OVERWRITE, store);
        }
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerPersonalChestData> type =
                EldaniorSystem.get().getPlayerPersonalChestDataType();
        store.putComponent(ref, type, chestData);

        InventoryEffectConfig config = new InventoryEffectConfig();
        if (config.getOpenStatusEffectId() != null && !config.getOpenStatusEffectId().isEmpty()) {
            EffectsManager.removeEffect(ref, config.getOpenStatusEffectId(), store);
        }
    }

    public static class ChestEventData {
        public String action;
        public String slotIndex;
        public String hotbarSlot;

        public static final BuilderCodec<ChestEventData> CODEC =
                BuilderCodec.builder(ChestEventData.class, ChestEventData::new)
                        .addField(new KeyedCodec<>("Action",     Codec.STRING), (d, v) -> d.action     = v, d -> d.action)
                        .addField(new KeyedCodec<>("SlotIndex",  Codec.STRING), (d, v) -> d.slotIndex  = v, d -> d.slotIndex)
                        .addField(new KeyedCodec<>("HotbarSlot", Codec.STRING), (d, v) -> d.hotbarSlot = v, d -> d.hotbarSlot)
                        .build();
    }
}