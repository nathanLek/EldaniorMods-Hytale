package com.eldanior.system.Inventory.components;

import com.eldanior.system.TreasureChest.components.PlayerChestData.ItemStackListCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerPersonalChestData implements Component<EntityStore> {

    public static final int CHEST_SIZE = 27;

    public static ComponentType<EntityStore, PlayerPersonalChestData> TYPE;

    private List<ItemStack> storedItems;

    public PlayerPersonalChestData() {
        this.storedItems = createEmptyChest();
    }

    public static final BuilderCodec<PlayerPersonalChestData> CODEC =
            BuilderCodec.builder(PlayerPersonalChestData.class, PlayerPersonalChestData::new)
                    .addField(
                            new KeyedCodec<>("StoredItems", new ItemStackListCodec()),
                            (data, value) -> data.storedItems = normalize(value),
                            data -> data.storedItems
                    )
                    .build();

    private static List<ItemStack> createEmptyChest() {
        List<ItemStack> list = new ArrayList<>(CHEST_SIZE);
        for (int i = 0; i < CHEST_SIZE; i++) {
            list.add(ItemStack.EMPTY);
        }
        return list;
    }

    private static List<ItemStack> normalize(List<ItemStack> raw) {
        if (raw == null) return createEmptyChest();

        List<ItemStack> result = new ArrayList<>(CHEST_SIZE);
        for (int i = 0; i < CHEST_SIZE; i++) {
            ItemStack item = (i < raw.size()) ? raw.get(i) : null;
            result.add((item != null && !item.isEmpty()) ? item : ItemStack.EMPTY);
        }
        return result;
    }

    public List<ItemStack> getStoredItems() {
        return storedItems;
    }

    public void setStoredItems(List<ItemStack> items) {
        this.storedItems = normalize(items);
    }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= CHEST_SIZE) return ItemStack.EMPTY;
        ItemStack item = storedItems.get(slot);
        return (item != null) ? item : ItemStack.EMPTY;
    }

    public void setItem(int slot, ItemStack item) {
        if (slot < 0 || slot >= CHEST_SIZE) return;
        storedItems.set(slot, (item != null && !item.isEmpty()) ? item : ItemStack.EMPTY);
    }

    public void clearSlot(int slot) {
        setItem(slot, ItemStack.EMPTY);
    }

    public int findFirstEmptySlot() {
        for (int i = 0; i < CHEST_SIZE; i++) {
            ItemStack item = storedItems.get(i);
            if (item == null || item.isEmpty()) return i;
        }
        return -1;
    }

    public boolean isFull() {
        return findFirstEmptySlot() == -1;
    }

    public int countItems() {
        int count = 0;
        for (ItemStack item : storedItems) {
            if (item != null && !item.isEmpty()) count++;
        }
        return count;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        PlayerPersonalChestData copy = new PlayerPersonalChestData();
        copy.storedItems = new ArrayList<>(CHEST_SIZE);
        for (ItemStack item : this.storedItems) {
            copy.storedItems.add(item);
        }
        return copy;
    }
}