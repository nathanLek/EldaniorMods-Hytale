package com.eldanior.system.TreasureChest.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.BsonValue;

public class PlayerChestData implements Component<EntityStore> {
    private static final String KEY_ITEMS = "Items";
    private static final String KEY_DISCOVERED = "Discovered";
    private static final String KEY_ID = "id";
    private static final String KEY_Q = "q";
    private static final String KEY_D = "d";
    private static final String KEY_MD = "md";
    private static final String KEY_META = "meta";

    private transient int tickTimer = 0;

    // Mise à jour du CODEC principal pour inclure notre nouvelle Map de Cooldowns
    public static final BuilderCodec<PlayerChestData> CODEC = BuilderCodec.builder(PlayerChestData.class, PlayerChestData::new)
            .addField(new KeyedCodec<>("Templates", new MapCodec<>(new LegacyChestDataCodec(), ConcurrentHashMap::new)),
                    (data, value) -> data.lootData = new ConcurrentHashMap<>(value),
                    (data) -> data.lootData)
            .addField(new KeyedCodec<>("Cooldowns", new MapCodec<>(Codec.LONG, ConcurrentHashMap::new)), // AJOUT POUR LE COOLDOWN
                    (data, value) -> data.chestCooldowns = new ConcurrentHashMap<>(value),
                    (data) -> data.chestCooldowns)
            .build();

    private Map<String, ChestData> lootData = new ConcurrentHashMap<>();

    // --- GESTION DU COOLDOWN ---
    // La Map qui stocke le moment où le joueur a vidé le coffre
    private Map<String, Long> chestCooldowns = new ConcurrentHashMap<>();

    public PlayerChestData() {
    }

    public PlayerChestData(PlayerChestData other) {
        for(Map.Entry<String, ChestData> entry : other.lootData.entrySet()) {
            this.lootData.put(entry.getKey(), new ChestData(entry.getValue()));
        }
        // Copie des cooldowns pour le clonage
        if (other.chestCooldowns != null) {
            this.chestCooldowns = new ConcurrentHashMap<>(other.chestCooldowns);
        }
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new PlayerChestData(this);
    }

    // --- NOUVELLES MÉTHODES COOLDOWN ---
    public long getLastLootTime(int x, int y, int z, String world_name) {
        String key = getKey(x, y, z, world_name);
        if (chestCooldowns != null && chestCooldowns.containsKey(key)) {
            return chestCooldowns.get(key);
        }
        return 0L;
    }

    public void setLastLootTime(int x, int y, int z, String world_name, long time) {
        if (this.chestCooldowns == null) {
            this.chestCooldowns = new ConcurrentHashMap<>();
        }
        String key = getKey(x, y, z, world_name);
        this.chestCooldowns.put(key, time);
    }
    // -----------------------------------

    public static String getDeprecatedKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    public static String getKey(int x, int y, int z, String world_name) {
        return x + "," + y + "," + z + "," + world_name;
    }

    public boolean hasDeprecatedData(int x, int y, int z) {
        return this.lootData.containsKey(getDeprecatedKey(x, y, z));
    }

    public void replaceDeprecatedData(int x, int y, int z, String world_name) {
        String oldKey = getDeprecatedKey(x, y, z);
        ChestData data = this.lootData.get(oldKey);
        if (data != null) {
            this.lootData.put(getKey(x, y, z, world_name), data);
            this.lootData.remove(oldKey);
        }
    }

    public void resetChest(int x, int y, int z, String world_name) {
        this.lootData.remove(getKey(x, y, z, world_name));
        this.lootData.remove(getDeprecatedKey(x, y, z));
        // Reset du cooldown aussi
        if (this.chestCooldowns != null) {
            this.chestCooldowns.remove(getKey(x, y, z, world_name));
        }
    }

    public void resetAllChests() {
        this.lootData.clear();
        if (this.chestCooldowns != null) {
            this.chestCooldowns.clear();
        }
    }

    public boolean isDiscovered(int x, int y, int z, String world_name) {
        String key = getKey(x, y, z, world_name);
        if (!this.lootData.containsKey(key) && this.hasDeprecatedData(x, y, z)) {
            this.replaceDeprecatedData(x, y, z, world_name);
        }
        ChestData data = this.lootData.get(key);
        return data != null && data.discovered;
    }

    public void setDiscovered(int x, int y, int z, String world_name, boolean discovered) {
        String key = getKey(x, y, z, world_name);
        this.lootData.compute(key, (k, v) -> {
            if (v == null) {
                return new ChestData(new ArrayList<>(), discovered);
            } else {
                v.discovered = discovered;
                return v;
            }
        });
    }

    public List<ItemStack> getInventory(int x, int y, int z, String world_name) {
        String key = getKey(x, y, z, world_name);
        if (!this.lootData.containsKey(key) && this.hasDeprecatedData(x, y, z)) {
            this.replaceDeprecatedData(x, y, z, world_name);
        }
        ChestData data = this.lootData.get(key);
        return data != null ? new ArrayList<>(data.items) : new ArrayList<>();
    }

    public void setInventory(int x, int y, int z, String world_name, List<ItemStack> items) {
        String key = getKey(x, y, z, world_name);
        this.lootData.compute(key, (k, v) -> {
            if (v == null) {
                return new ChestData(new ArrayList<>(items), true);
            } else {
                v.items = new ArrayList<>(items);
                return v;
            }
        });
    }

    public void incrementTimer() {
        ++this.tickTimer;
    }

    public int getTimer() {
        return this.tickTimer;
    }

    public void resetTimer() {
        this.tickTimer = 0;
    }

    // --- ChestData ---

    public static class ChestData {
        public List<ItemStack> items = new ArrayList<>();
        public boolean discovered = false;
        public static final Codec<ChestData> INTERNAL_CODEC;

        public ChestData() {
        }

        public ChestData(List<ItemStack> items, boolean discovered) {
            this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
            this.discovered = discovered;
        }

        public ChestData(ChestData other) {
            this.items = new ArrayList<>(other.items);
            this.discovered = other.discovered;
        }

        static {
            BuilderCodec.Builder<ChestData> builder = BuilderCodec.builder(ChestData.class, ChestData::new);

            builder.addField(new KeyedCodec<>("Items", new ItemStackListCodec()),
                    (ChestData d, List<ItemStack> v) -> d.items = v,
                    (ChestData d) -> d.items);

            builder.addField(new KeyedCodec<>("Discovered", Codec.BOOLEAN),
                    (ChestData d, Boolean v) -> d.discovered = v,
                    (ChestData d) -> d.discovered);

            INTERNAL_CODEC = builder.build();
        }
    }

    // --- LegacyChestDataCodec ---

    public static class LegacyChestDataCodec implements Codec<ChestData> {
        @Nonnull
        public ChestData decode(@Nonnull BsonValue value, @Nonnull ExtraInfo extraInfo) {
            if (value.isString()) {
                return this.parseLegacyJson(value.asString().getValue());
            } else if (value.isArray()) {
                List<ItemStack> items = ItemStackListCodec.deserializeBsonArray(value.asArray());
                return new ChestData(items, !items.isEmpty());
            } else {
                return value.isDocument() ? ChestData.INTERNAL_CODEC.decode(value, extraInfo) : new ChestData();
            }
        }

        @Nonnull
        public ChestData decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
            reader.consumeWhiteSpace();
            int firstChar = reader.peek();
            if (firstChar == '"') { // Remplacement propre du code ASCII 34
                return this.parseLegacyJson(reader.readString());
            } else if (firstChar == '[') { // Remplacement propre du code ASCII 91
                List<ItemStack> items = (new ItemStackListCodec()).decodeJson(reader, extraInfo);
                return new ChestData(items, !items.isEmpty());
            } else {
                List<ItemStack> items = new ArrayList<>();
                boolean discovered = false;
                reader.expect('{');
                reader.consumeWhiteSpace();
                if (reader.tryConsume('}')) {
                    return new ChestData(items, discovered);
                } else {
                    while(true) {
                        reader.consumeWhiteSpace();
                        String key = reader.readString();
                        reader.consumeWhiteSpace();
                        reader.expect(':');
                        reader.consumeWhiteSpace();
                        switch (key) {
                            case "Items" -> items = (new ItemStackListCodec()).decodeJson(reader, extraInfo);
                            case "Discovered" -> discovered = reader.readBooleanValue();
                            default -> reader.skipValue();
                        }
                        reader.consumeWhiteSpace();
                        if (reader.tryConsume('}')) {
                            return new ChestData(items, discovered);
                        }
                        reader.expect(',');
                    }
                }
            }
        }

        private ChestData parseLegacyJson(String json) {
            if (json != null && !json.isEmpty()) {
                try {
                    String trimmed = json.trim();
                    if (trimmed.startsWith("[")) {
                        List<ItemStack> items = ItemStackListCodec.deserializeBsonArray(BsonArray.parse(json));
                        return new ChestData(items, !items.isEmpty());
                    } else {
                        BsonDocument doc = BsonDocument.parse(json);
                        List<ItemStack> items = new ArrayList<>();
                        boolean discovered = false;
                        if (doc.containsKey("Items")) {
                            items = ItemStackListCodec.deserializeBsonArray(doc.getArray("Items"));
                            if (!doc.containsKey("Discovered")) {
                                discovered = !items.isEmpty();
                            }
                        }
                        if (doc.containsKey("Discovered")) {
                            discovered = doc.getBoolean("Discovered").getValue();
                        }
                        return new ChestData(items, discovered);
                    }
                } catch (Exception e) {
                    return new ChestData();
                }
            } else {
                return new ChestData();
            }
        }

        @Nonnull
        public BsonValue encode(@Nonnull ChestData data, @Nonnull ExtraInfo extraInfo) {
            return ChestData.INTERNAL_CODEC.encode(data, extraInfo);
        }

        @Nonnull
        public Schema toSchema(@Nonnull SchemaContext context) {
            return ChestData.INTERNAL_CODEC.toSchema(context);
        }
    }

    // --- ItemStackListCodec ---

    public static class ItemStackListCodec implements Codec<List<ItemStack>> {
        @Nonnull
        public List<ItemStack> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
            return deserializeBsonArray(bsonValue.asArray());
        }

        @Nonnull
        public List<ItemStack> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
            List<ItemStack> list = new ArrayList<>();
            reader.expect('[');
            reader.consumeWhiteSpace();
            if (reader.tryConsume(']')) {
                return list;
            } else {
                while(true) {
                    reader.consumeWhiteSpace();
                    int c = reader.peek();
                    if (c != 'n' && c != 'N') { // Remplace les codes ascii obscurs (110 et 78)
                        list.add(this.decodeItemStackJson(reader));
                    } else {
                        reader.readNullValue();
                        list.add(null);
                    }
                    reader.consumeWhiteSpace();
                    if (reader.tryConsume(']')) {
                        return list;
                    }
                    reader.expect(',');
                }
            }
        }

        private ItemStack decodeItemStackJson(RawJsonReader reader) throws IOException {
            reader.expect('{');
            reader.consumeWhiteSpace();
            if (reader.tryConsume('}')) {
                return new ItemStack("air", 0, 0.0, 0.0, null);
            } else {
                String id = "air";
                int q = 1;
                double d = 0.0;
                double md = 0.0;
                BsonDocument meta = null;
                while(true) {
                    reader.consumeWhiteSpace();
                    String key = reader.readString();
                    reader.consumeWhiteSpace();
                    reader.expect(':');
                    reader.consumeWhiteSpace();
                    switch (key) {
                        case "id" -> id = reader.readString().intern();
                        case "q" -> q = reader.readIntValue();
                        case "d" -> d = reader.readDoubleValue();
                        case "md" -> md = reader.readDoubleValue();
                        case "meta" -> meta = RawJsonReader.readBsonDocument(reader);
                        default -> reader.skipValue();
                    }
                    reader.consumeWhiteSpace();
                    if (reader.tryConsume('}')) {
                        return new ItemStack(id, q, d, md, meta);
                    }
                    reader.expect(',');
                }
            }
        }

        public static List<ItemStack> deserializeBsonArray(BsonArray array) {
            List<ItemStack> items = new ArrayList<>();
            if (array == null) return items;
            for(BsonValue value : array) {
                if (value.isNull()) {
                    items.add(null);
                } else if (value.isDocument()) {
                    try {
                        BsonDocument doc = value.asDocument();
                        items.add(new ItemStack(
                                doc.getString("id").getValue().intern(),
                                doc.getInt32("q").getValue(),
                                doc.getDouble("d").getValue(),
                                doc.getDouble("md").getValue(),
                                doc.containsKey("meta") ? doc.getDocument("meta") : null
                        ));
                    } catch (Exception ignored) {}
                }
            }
            return items;
        }

        @Nonnull
        public BsonValue encode(@Nonnull List<ItemStack> items, @Nonnull ExtraInfo extraInfo) {
            BsonArray array = new BsonArray();
            for(ItemStack stack : items) {
                if (stack != null) {
                    BsonDocument doc = new BsonDocument();
                    doc.append("id", new BsonString(stack.getItemId()));
                    doc.append("q", new BsonInt32(stack.getQuantity()));
                    doc.append("d", new BsonDouble(stack.getDurability()));
                    doc.append("md", new BsonDouble(stack.getMaxDurability()));
                    if (stack.getMetadata() != null) doc.append("meta", stack.getMetadata());
                    array.add(doc);
                } else {
                    array.add(new BsonNull());
                }
            }
            return array;
        }

        @Nonnull
        public Schema toSchema(@Nonnull SchemaContext context) {
            return new ArraySchema();
        }
    }
}