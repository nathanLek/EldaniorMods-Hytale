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
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.math.vector.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PersonalChestPage extends InteractiveCustomUIPage<PersonalChestPage.ChestEventData> {

    private final PlayerPersonalChestData chestData;
    private final List<ItemStack> chestItems;
    private static final String PORTAL_PARTICLE_EFFECT = "Portal_Round_Blue";

    public PersonalChestPage(@Nonnull PlayerRef playerRef, @Nonnull PlayerPersonalChestData chestData) {
        super(playerRef, CustomPageLifetime.CanDismiss, ChestEventData.CODEC);
        this.chestData = chestData;
        this.chestItems = new ArrayList<>(chestData.getStoredItems());

        while (chestItems.size() < 27) {
            chestItems.add(ItemStack.EMPTY);
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("PersonalChestWithHotbar.ui");
        InventoryEffectConfig config = new InventoryEffectConfig();
        spawnParticleEffect(ref, store, config);
        updateFullUI(ref, store, commands, events);
    }

    private void updateFullUI(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder commands, UIEventBuilder events) {
        updateChestSlots(commands, events);
        updateHotbarSlots(ref, store, commands, events);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose", EventData.of("Action", "close"));
    }

    private void spawnParticleEffect(Ref<EntityStore> playerRef, Store<EntityStore> store, InventoryEffectConfig config) {
        // 1. Visuel : On utilise l'ID de la config
        TransformComponent playerTransform = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (playerTransform != null) {
            Vector3d position = playerTransform.getPosition().add(0, 0.5, 0);
            ParticleUtil.spawnParticleEffect(config.getOpenParticleId(), position, store);
        }

        // 2. Gameplay : On applique l'effet défini dans la config avec la durée configurée
        // On vérifie d'abord si l'ID n'est pas vide ou null pour éviter les erreurs
        if (config.getOpenStatusEffectId() != null && !config.getOpenStatusEffectId().isEmpty()) {
            EffectsManager.applyCustomEffect(
                    playerRef,
                    config.getOpenStatusEffectId(),
                    config.getOpenEffectDuration(),
                    OverlapBehavior.OVERWRITE,
                    store
            );
        }
    }

    private void playCloseEffect(Ref<EntityStore> playerRef, Store<EntityStore> store, InventoryEffectConfig config) {
        // 1. Visuel : Particule de fermeture depuis la config

        TransformComponent playerTransform = store.getComponent(playerRef, TransformComponent.getComponentType());
        assert playerTransform != null;
        float yaw = (float) Math.toRadians(playerTransform.getRotation().getYaw());
        float pitch = (float) Math.toRadians(playerTransform.getRotation().getPitch());

        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        Vector3d lookDirection = new Vector3d(x, y, z).normalize();

        if (playerTransform != null) {
            //Vector3d position = playerTransform.getPosition().add(0, 0, 0);
            ParticleUtil.spawnParticleEffect(config.getCloseParticleId(), lookDirection, store);
        }

        // 2. Gameplay : Nettoyage de l'effet d'ouverture (si nécessaire)
        if (config.getOpenStatusEffectId() != null && !config.getOpenStatusEffectId().isEmpty()) {
            EffectsManager.removeEffect(playerRef, config.getOpenStatusEffectId(), store);
        }
    }

    private void updateChestSlots(UICommandBuilder commands, UIEventBuilder events) {
        for (int i = 0; i < 27; i++) {
            String slotRoot = "#Slot" + i;
            String iconPath = slotRoot + " #Icon";
            String qtyPath = slotRoot + " #Quantity";

            ItemStack item = chestItems.get(i);

            if (item != null && !item.isEmpty()) {
                commands.set(iconPath + ".Visible", true);
                commands.set(iconPath + ".ItemId", item.getItemId());

                if (item.getQuantity() > 1) {
                    commands.set(qtyPath + ".Visible", true);
                    commands.set(qtyPath + ".TextSpans", Message.raw(String.valueOf(item.getQuantity())));
                } else {
                    commands.set(qtyPath + ".Visible", false);
                }

            } else {
                commands.set(iconPath + ".Visible", false);
                commands.set(qtyPath + ".Visible", false);
            }

            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    slotRoot,
                    EventData.of("Action", "takeFromChest").append("SlotIndex", String.valueOf(i))
            );
        }
    }

    private void updateHotbarSlots(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder commands, UIEventBuilder events) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        Inventory inventory = player.getInventory();
        ItemContainer hotbar = inventory.getHotbar();

        for (int i = 0; i < 9; i++) {
            String slotRoot = "#Hotbar" + i;
            String iconPath = slotRoot + " #Icon";
            String qtyPath = slotRoot + " #Quantity";

            ItemStack item = hotbar.getItemStack((short) i);

            if (item != null && !item.isEmpty()) {
                commands.set(iconPath + ".Visible", true);
                commands.set(iconPath + ".ItemId", item.getItemId());

                if (item.getQuantity() > 1) {
                    commands.set(qtyPath + ".Visible", true);
                    commands.set(qtyPath + ".TextSpans", Message.raw(String.valueOf(item.getQuantity())));
                } else {
                    commands.set(qtyPath + ".Visible", false);
                }

            } else {
                commands.set(iconPath + ".Visible", false);
                commands.set(qtyPath + ".Visible", false);
            }

            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    slotRoot,
                    EventData.of("Action", "depositToChest").append("HotbarSlot", String.valueOf(i))
            );
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ChestEventData data) {
        if ("close".equals(data.action)) {
            saveAndClose(ref, store);
            return;
        }

        if ("depositToChest".equals(data.action) && data.hotbarSlot != null) {
            int hotbarSlot = Integer.parseInt(data.hotbarSlot);

            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            Inventory inventory = player.getInventory();
            ItemContainer hotbar = inventory.getHotbar();
            ItemStack item = hotbar.getItemStack((short) hotbarSlot);

            if (item != null && !item.isEmpty()) {
                handleDepositToChest(ref, store, hotbarSlot);
            }

        } else if ("takeFromChest".equals(data.action) && data.slotIndex != null) {
            int slotIndex = Integer.parseInt(data.slotIndex);

            if (slotIndex >= 0 && slotIndex < chestItems.size()) {
                ItemStack item = chestItems.get(slotIndex);
                if (item != null && !item.isEmpty()) {
                    handleTakeFromChest(ref, store, slotIndex);
                }
            }
        }
    }

    private void handleDepositToChest(Ref<EntityStore> ref, Store<EntityStore> store, int hotbarSlot) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        Inventory inventory = player.getInventory();
        ItemContainer hotbar = inventory.getHotbar();

        if (hotbarSlot < 0 || hotbarSlot >= 9) return;

        ItemStack itemToDeposit = hotbar.getItemStack((short) hotbarSlot);
        if (itemToDeposit == null || itemToDeposit.isEmpty()) {
            return;
        }

        int emptySlot = -1;
        for (int i = 0; i < 27; i++) {
            ItemStack slotItem = chestItems.get(i);
            if (slotItem == null || slotItem.isEmpty()) {
                emptySlot = i;
                break;
            }
        }

        if (emptySlot == -1) {
            player.sendMessage(Message.raw("§cCoffre plein!"));
            return;
        }

        chestItems.set(emptySlot, itemToDeposit);
        hotbar.removeItemStackFromSlot((short) hotbarSlot);

        UICommandBuilder update = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        updateFullUI(ref, store, update, events);
        this.sendUpdate(update);

        player.sendMessage(Message.raw("§aItem déposé!"));
    }

    private void handleTakeFromChest(Ref<EntityStore> ref, Store<EntityStore> store, int slotIndex) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        if (slotIndex < 0 || slotIndex >= chestItems.size()) return;

        ItemStack itemToTake = chestItems.get(slotIndex);
        if (itemToTake == null || itemToTake.isEmpty()) {
            return;
        }

        Inventory inventory = player.getInventory();
        ItemContainer hotbar = inventory.getHotbar();

        var result = hotbar.addItemStack(itemToTake);

        if (result.succeeded()) {
            chestItems.set(slotIndex, ItemStack.EMPTY);

            UICommandBuilder update = new UICommandBuilder();
            UIEventBuilder events = new UIEventBuilder();
            updateFullUI(ref, store, update, events);
            this.sendUpdate(update);

            player.sendMessage(Message.raw("§aItem récupéré!"));
        } else {
            player.sendMessage(Message.raw("§cInventaire plein!"));
        }
    }

    private void saveAndClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        InventoryEffectConfig config = new InventoryEffectConfig();
        playCloseEffect(ref, store, config);

        ComponentType<EntityStore, PlayerPersonalChestData> type = EldaniorSystem.get().getPlayerPersonalChestDataType();

        List<ItemStack> cleanedItems = new ArrayList<>();
        for (ItemStack item : chestItems) {
            if (item != null && !item.isEmpty()) {
                cleanedItems.add(item);
            }
        }

        chestData.setStoredItems(cleanedItems);
        store.putComponent(ref, type, chestData);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.sendMessage(Message.raw("§aCoffre sauvegardé! " + cleanedItems.size() + " items."));
        }

        this.close();
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        InventoryEffectConfig config = new InventoryEffectConfig();
        playCloseEffect(ref, store, config);
    }

    public static class ChestEventData {
        public static final BuilderCodec<ChestEventData> CODEC = BuilderCodec.builder(ChestEventData.class, ChestEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action)
                .add()
                .append(new KeyedCodec<>("SlotIndex", Codec.STRING), (d, v) -> d.slotIndex = v, d -> d.slotIndex)
                .add()
                .append(new KeyedCodec<>("HotbarSlot", Codec.STRING), (d, v) -> d.hotbarSlot = v, d -> d.hotbarSlot)
                .add()
                .build();

        public String action;
        public String slotIndex;
        public String hotbarSlot;
    }
}