package com.eldanior.system.TreasureChest.resources;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import static com.eldanior.system.TreasureChest.resources.TreasureChestTemplate.ChestData.INTERNAL_CODEC;

public class TreasureChestTemplate implements Resource<ChunkStore> {
    private static final String DEFAULT_DROPLIST = "undefined";

    public static final BuilderCodec<TreasureChestTemplate> CODEC = BuilderCodec.builder(TreasureChestTemplate.class, TreasureChestTemplate::new)
            .addField(new KeyedCodec<>("Templates", new MapCodec<>(new LegacyChestDataCodec(), ConcurrentHashMap::new)),
                    (data, value) -> data.templates = new ConcurrentHashMap<>(value),
                    (data) -> data.templates).build();

    private Map<String, ChestData> templates = new ConcurrentHashMap<>();

    public TreasureChestTemplate() {
    }

    public TreasureChestTemplate(TreasureChestTemplate other) {
        for(Map.Entry<String, ChestData> entry : other.templates.entrySet()) {
            this.templates.put(entry.getKey(), new ChestData(entry.getValue()));
        }
    }

    public Set<String> getTemplateKeys() {
        return this.templates.keySet();
    }

    @Nullable
    public Resource<ChunkStore> clone() {
        return new TreasureChestTemplate(this);
    }

    public static String getKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    public boolean hasTemplate(int x, int y, int z) {
        return this.templates.containsKey(getKey(x, y, z));
    }

    public List<ItemStack> getTemplate(int x, int y, int z) {
        ChestData data = this.templates.get(getKey(x, y, z));
        return data != null ? new ArrayList<>(data.items) : new ArrayList<>();
    }

    public String getDropList(int x, int y, int z) {
        ChestData data = this.templates.get(getKey(x, y, z));
        return data != null ? data.dropList : DEFAULT_DROPLIST;
    }

    public void setDropList(int x, int y, int z, String dropList) {
        ChestData data = this.templates.get(getKey(x, y, z));
        if (data != null) {
            data.dropList = dropList != null ? dropList : DEFAULT_DROPLIST;
        }
    }

    public void saveTemplate(int x, int y, int z, List<ItemStack> items, String dropList) {
        this.templates.put(getKey(x, y, z), new ChestData(items, dropList));
    }

    public void removeTemplate(int x, int y, int z) {
        this.templates.remove(getKey(x, y, z));
    }

    // --- ChestData Corrigé ---
    public static class ChestData {
        public List<ItemStack> items = new ArrayList<>();
        public String dropList = "undefined";
        public boolean discovered = false; // Ajouté pour compatibilité Loot

        public static final Codec<ChestData> INTERNAL_CODEC;

        public ChestData() {}

        public ChestData(List<ItemStack> items, String dropList) {
            this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
            this.dropList = dropList != null ? dropList : "undefined";
            this.discovered = !this.items.isEmpty();
        }

        public ChestData(ChestData other) {
            this.items = new ArrayList<>(other.items);
            this.dropList = other.dropList;
            this.discovered = other.discovered;
        }

        static {
            BuilderCodec.Builder<ChestData> builder = BuilderCodec.builder(ChestData.class, ChestData::new);

            builder.addField(new KeyedCodec<>("Items", new ItemStackListCodec()),
                    (d, v) -> d.items = v, (d) -> d.items);

            builder.addField(new KeyedCodec<>("DropList", Codec.STRING),
                    (d, v) -> d.dropList = v, (d) -> d.dropList);

            builder.addField(new KeyedCodec<>("Discovered", Codec.BOOLEAN),
                    (d, v) -> d.discovered = v, (d) -> d.discovered);

            INTERNAL_CODEC = builder.build();
        }
    }

    // --- LegacyChestDataCodec Corrigé ---
    public static class LegacyChestDataCodec implements Codec<ChestData> {
        public @org.jspecify.annotations.Nullable ChestData decode(@Nonnull BsonValue value, @Nonnull ExtraInfo extraInfo) {
            if (value.isString()) {
                return this.parseLegacyJson(value.asString().getValue());
            } else {
                return value.isDocument() ? INTERNAL_CODEC.decode(value, extraInfo) : new ChestData();
            }
        }

        @Nonnull
        public ChestData decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
            reader.consumeWhiteSpace();
            int firstChar = reader.peek();
            if (firstChar == 34) {
                return this.parseLegacyJson(reader.readString());
            } else if (firstChar == 91) {
                List<ItemStack> items = (new ItemStackListCodec()).decodeJson(reader, extraInfo);
                return new ChestData(items, "undefined");
            } else {
                List<ItemStack> items = new ArrayList<>();
                String dropList = "undefined";
                boolean discovered = false;
                reader.expect('{');
                reader.consumeWhiteSpace();
                if (reader.tryConsume('}')) return new ChestData(items, dropList);

                while(true) {
                    reader.consumeWhiteSpace();
                    String key = reader.readString();
                    reader.consumeWhiteSpace();
                    reader.expect(':');
                    reader.consumeWhiteSpace();
                    switch (key) {
                        case "Items", "items" -> items = (new ItemStackListCodec()).decodeJson(reader, extraInfo);
                        case "DropList", "dropList" -> dropList = reader.readString();
                        case "Discovered" -> discovered = reader.readBooleanValue();
                        default -> reader.skipValue();
                    }
                    reader.consumeWhiteSpace();
                    if (reader.tryConsume('}')) {
                        ChestData data = new ChestData(items, dropList);
                        data.discovered = discovered;
                        return data;
                    }
                    reader.expect(',');
                }
            }
        }

        private ChestData parseLegacyJson(String json) {
            if (json == null || json.isEmpty()) return new ChestData();
            try {
                String trimmed = json.trim();
                if (trimmed.startsWith("[")) {
                    List<ItemStack> items = ItemStackListCodec.deserializeBsonArray(BsonArray.parse(json));
                    return new ChestData(items, "undefined");
                } else {
                    BsonDocument doc = BsonDocument.parse(json);
                    List<ItemStack> items = new ArrayList<>();
                    String dropList = "undefined";
                    if (doc.containsKey("items")) items = ItemStackListCodec.deserializeBsonArray(doc.getArray("items"));
                    else if (doc.containsKey("Items")) items = ItemStackListCodec.deserializeBsonArray(doc.getArray("Items"));

                    if (doc.containsKey("dropList")) dropList = doc.getString("dropList").getValue();
                    else if (doc.containsKey("DropList")) dropList = doc.getString("DropList").getValue();

                    return new ChestData(items, dropList);
                }
            } catch (Exception var6) {
                return new ChestData();
            }
        }

        @Nonnull
        public BsonValue encode(@Nonnull ChestData data, @Nonnull ExtraInfo extraInfo) {
            return INTERNAL_CODEC.encode(data, extraInfo);
        }

        @Nonnull
        public Schema toSchema(@Nonnull SchemaContext context) {
            return INTERNAL_CODEC.toSchema(context);
        }
    }

    // --- ItemStackListCodec Corrigé ---
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
            if (reader.tryConsume(']')) return list;
            while(true) {
                reader.consumeWhiteSpace();
                int c = reader.peek();
                if (c != 'n' && c != 'N') {
                    list.add(this.decodeItemStackJson(reader));
                } else {
                    reader.readNullValue();
                    list.add((ItemStack) null);
                }
                reader.consumeWhiteSpace();
                if (reader.tryConsume(']')) return list;
                reader.expect(',');
            }
        }

        private ItemStack decodeItemStackJson(RawJsonReader reader) throws IOException {
            reader.expect('{');
            reader.consumeWhiteSpace();
            if (reader.tryConsume('}')) return new ItemStack("air", 0, 0.0, 0.0, null);
            String id = "";
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
                if (reader.tryConsume('}')) return new ItemStack(id, q, d, md, meta);
                reader.expect(',');
            }
        }

        public static List<ItemStack> deserializeBsonArray(BsonArray array) {
            List<ItemStack> items = new ArrayList<>();
            if (array == null) return items;
            for(BsonValue value : array) {
                if (value.isNull()) items.add((ItemStack) null);
                else if (value.isDocument()) {
                    try {
                        BsonDocument doc = value.asDocument();
                        items.add(new ItemStack(
                                doc.getString("id").getValue().intern(),
                                doc.getInt32("q").getValue(),
                                doc.getDouble("d").getValue(),
                                doc.getDouble("md").getValue(),
                                doc.containsKey("meta") ? doc.getDocument("meta") : null
                        ));
                    } catch (Exception e) { EldaniorLogger.error("TreasureChestTemplate", e); }
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