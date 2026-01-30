package com.eldanior.system.Inventory.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonInt32;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerPersonalChestData implements Component<EntityStore> {

    // Variable statique pour l'accès facile (indispensable pour ta commande)
    public static ComponentType<EntityStore, PlayerPersonalChestData> TYPE;

    private List<ItemStack> storedItems = new ArrayList<>();

    public PlayerPersonalChestData() {
        for (int i = 0; i < 27; i++) {
            storedItems.add(ItemStack.EMPTY);
        }
    }

    public static final BuilderCodec<PlayerPersonalChestData> CODEC =
            BuilderCodec.builder(PlayerPersonalChestData.class, PlayerPersonalChestData::new)
                    .append(new KeyedCodec<>("StoredItemsData", Codec.STRING),
                            (data, serialized) -> {
                                data.storedItems = deserializeItems(serialized);
                            },
                            data -> serializeItems(data.storedItems))
                    .add()
                    .build();

    private static String serializeItems(List<ItemStack> items) {
        BsonArray array = new BsonArray();
        for (ItemStack item : items) {
            BsonDocument doc = new BsonDocument();
            if (item != null && !item.isEmpty()) {
                doc.put("id", new BsonString(item.getItemId()));
                doc.put("qty", new BsonInt32(item.getQuantity()));
                doc.put("dur", new BsonInt32((int)item.getDurability()));
                doc.put("max", new BsonInt32((int)item.getMaxDurability()));
                if (item.getMetadata() != null) {
                    doc.put("meta", item.getMetadata());
                }
            } else {
                doc.put("id", new BsonString("Empty"));
            }
            array.add(doc);
        }

        BsonDocument wrapper = new BsonDocument();
        wrapper.put("items", array);

        return wrapper.toJson();
    }

    private static List<ItemStack> deserializeItems(String serialized) {
        List<ItemStack> items = new ArrayList<>();

        if (serialized == null || serialized.isEmpty() || serialized.equals("null")) {
            for (int i = 0; i < 27; i++) items.add(ItemStack.EMPTY);
            return items;
        }

        try {
            BsonDocument wrapper = BsonDocument.parse(serialized);

            if (!wrapper.containsKey("items")) {
                for (int i = 0; i < 27; i++) items.add(ItemStack.EMPTY);
                return items;
            }

            BsonArray array = wrapper.getArray("items");

            for (int i = 0; i < array.size(); i++) {
                BsonDocument doc = array.get(i).asDocument();
                String id = doc.getString("id").getValue();

                if ("Empty".equals(id)) {
                    items.add(ItemStack.EMPTY);
                } else {
                    int qty = doc.getInt32("qty").getValue();
                    double dur = doc.getInt32("dur").getValue();
                    double max = doc.getInt32("max").getValue();
                    BsonDocument meta = doc.containsKey("meta") ? doc.getDocument("meta") : null;
                    items.add(new ItemStack(id, qty, dur, max, meta));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            items.clear(); // En cas d'erreur de parsing, on reset pour éviter les bugs
            for (int i = 0; i < 27; i++) {
                items.add(ItemStack.EMPTY);
            }
        }

        // Compléter si la liste est trop courte
        while (items.size() < 27) {
            items.add(ItemStack.EMPTY);
        }
        return items;
    }

    public List<ItemStack> getStoredItems() {
        return storedItems;
    }

    public void setStoredItems(List<ItemStack> items) {
        this.storedItems = new ArrayList<>(items);
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        PlayerPersonalChestData copy = new PlayerPersonalChestData();
        copy.storedItems = new ArrayList<>();
        copy.storedItems.addAll(this.storedItems);
        return copy;
    }
}