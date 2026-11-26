package com.eldanior.system.presentoir;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.presentoir.Presentoir.Slot;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelPermission;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Presentoirs : touche d'interaction avec un objet en main = l'exposer ; main vide = reprendre le dernier objet pose. Tout objet
 * ayant son propre modele s'expose (armes, artefacts, armures, butins, potions, outils) ; pas les blocs. Le contenu est sauvegarde ici (objets complets, durabilite comprise), pas avec le chunk.
 *
 * Rendu : une entite decor par presentoir, jamais sauvegardee, recreee quand un joueur approche. Un objet seul tourne sur un
 * support ; des qu'une piece d'armure est posee, un mannequin porte le tout et tient l'objet. Les pieces sont des attachements du
 * modele, construits a partir du Model et de la Texture de chaque objet. Un objet sans os de main ne peut pas etre attache : il
 * devient le modele principal de l'entite, tourne par Objet_Idle, et ne peut pas aller dans la main du mannequin.
 * Le halo prend la couleur de rarete de l'objet (a partir de Rare) ou sa lumiere propre.
 */
public final class PresentoirManager {

    private PresentoirManager() {}

    public static final String BLOCK_ID = "Eldanior_Presentoir";
    private static final Set<String> PLAIN_QUALITIES = Set.of("Junk", "Common", "Uncommon", "Template", "Tool", "Technical", "Debug", "Developer");
    private static final double SHOW_RANGE = 64;

    private static final Map<String, Presentoir> STANDS = new ConcurrentHashMap<>();
    private static Path file;

    // ===== Cycle de vie =====

    /** A appeler au demarrage du plugin (assets charges : decoder un ItemStack lit sa fiche). */
    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("presentoirs.json");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                tick();
            } catch (Exception e) {
                EldaniorLogger.error("[Presentoir] boucle", e);
            }
        }, 5, 2, TimeUnit.SECONDS);
        EldaniorLogger.info("[Presentoir] " + STANDS.size() + " presentoir(s) garni(s) charge(s).");
    }

    static Presentoir find(String key) { return STANDS.get(key); }

    static boolean isStand(BlockType type) { return type != null && type.getId() != null && type.getId().startsWith(BLOCK_ID); }

    /** Bloc de base d'un presentoir vise sur l'une de ses cases de remplissage (le presentoir de famille en compte 26). */
    static Vector3i base(World world, Vector3i at) {
        int filler = WorldBlockUtil.getFiller(world, at.x(), at.y(), at.z());
        if (filler <= 0) return new Vector3i(at.x(), at.y(), at.z());
        return new Vector3i(at.x() - FillerBlockUtil.unpackX(filler), at.y() - FillerBlockUtil.unpackY(filler), at.z() - FillerBlockUtil.unpackZ(filler));
    }

    private static void tick() {
        Map<String, List<Presentoir>> byWorld = new java.util.HashMap<>();
        for (Presentoir p : STANDS.values()) byWorld.computeIfAbsent(p.world, w -> new ArrayList<>()).add(p);
        byWorld.forEach((name, stands) -> {
            World world = Universe.get().getWorld(name);
            if (world != null) world.execute(() -> stands.forEach(p -> refresh(world, p)));
        });
    }

    /** Bloc disparu sans passer par un joueur (explosion, regeneration) : le contenu tombe. Sinon, entite a jour pres des joueurs. */
    private static void refresh(World world, Presentoir p) {
        if (STANDS.get(p.key()) != p) return;
        BlockType type = WorldBlockUtil.getBlockType(world, p.x, p.y, p.z);
        if (type == null) return;                                                       // chunk non charge
        if (!isStand(type)) destroyed(world, p);
        else show(world, p);
    }

    // ===== Interactions =====

    /** Touche d'interaction sur le bloc de base. Thread du monde. */
    static void use(World world, Ref<EntityStore> ref, Player player, PlayerRef pr, UUID uuid, Vector3i at) {
        boolean admin = pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        ParcelData parcel = ParcelManager.getParcelAt(world.getName(), at.x(), at.y(), at.z());
        if (!admin && parcel != null && !parcel.hasPermission(uuid, ParcelPermission.INTERACT)) return;      // ParcelInteractEvent previent

        String key = Presentoir.key(world.getName(), at.x(), at.y(), at.z());
        Presentoir p = STANDS.get(key);
        if (p != null && !admin && !uuid.equals(p.owner)) {
            pr.sendMessage(Message.raw("Ce présentoir appartient à " + p.ownerName + ".").color("#e8c060"));
            return;
        }
        Inventory inv = player.getInventory();
        ItemStack hand = inv.getActiveHotbarItem();
        if (hand == null || hand.isEmpty()) {
            if (p != null) takeLast(world, p, ref, inv, pr);
            return;
        }
        Slot slot = Slot.of(hand.getItem());
        if (slot == null) {
            pr.sendMessage(Message.raw("Les blocs ne s'exposent pas. Main vide pour reprendre un objet.").color("#e8c060"));
            return;
        }
        if (slot != Slot.ARME && !StandKind.of(WorldBlockUtil.getBlockType(world, at.x(), at.y(), at.z())).carriesArmor()) {
            pr.sendMessage(Message.raw("Ce présentoir est trop petit pour une armure.").color("#e8c060"));
            return;
        }
        if (p != null && !fits(p, slot, hand.getItem())) {
            pr.sendMessage(Message.raw("Le mannequin ne peut pas tenir cet objet : exposez-le sur un présentoir sans armure.").color("#e8c060"));
            return;
        }
        byte active = inv.getActiveHotbarSlot();
        if (active < 0) return;
        ItemStack shown = hand.withQuantity(1);
        ItemContainer hotbar = inv.getHotbar();
        if (!hotbar.removeItemStackFromSlot((short) active, 1).succeeded()) return;

        if (p == null) {
            p = new Presentoir(world.getName(), at.x(), at.y(), at.z());
            p.owner = uuid;
            p.ownerName = pr.getUsername();
            p.yaw = facing(ref, p, ref.getStore());
            STANDS.put(key, p);
        }
        ItemStack previous = p.items.remove(slot);                                      // retire puis remet : dernier pose
        p.items.put(slot, shown);
        if (previous != null) give(world, previous, ref, inv);
        show(world, p);
        save();
        pr.sendMessage(Message.raw("Exposé : ").insert(hand.getItem().getTranslationMessage()).color("#9fe0a0"));
    }

    /** Le mannequin ne tient que les objets qui ont un os de main. */
    private static boolean fits(Presentoir p, Slot slot, Item item) {
        if (slot == Slot.ARME) return !p.hasArmor() || ItemShape.of(model(item)).attached();
        Item object = p.object();
        return object == null || ItemShape.of(model(object)).attached();
    }

    private static void takeLast(World world, Presentoir p, Ref<EntityStore> ref, Inventory inv, PlayerRef pr) {
        Slot last = null;
        for (Slot s : p.items.keySet()) last = s;
        if (last == null) return;
        ItemStack stack = p.items.remove(last);
        give(world, stack, ref, inv);
        if (p.items.isEmpty()) {
            STANDS.remove(p.key());
            hide(p);
        } else {
            show(world, p);
        }
        save();
        Item item = stack.getItem();
        if (item != null) pr.sendMessage(Message.raw("Repris : ").insert(item.getTranslationMessage()).color("#9fe0a0"));
    }

    /** Peut casser ce presentoir ? Le garni ne se casse que par son proprietaire ou un admin. */
    static boolean canBreak(String world, Vector3i base, UUID uuid, boolean admin) {
        Presentoir p = STANDS.get(Presentoir.key(world, base.x(), base.y(), base.z()));
        return p == null || admin || p.owner.equals(uuid);
    }

    static String ownerName(String world, Vector3i base) {
        Presentoir p = STANDS.get(Presentoir.key(world, base.x(), base.y(), base.z()));
        return p != null ? p.ownerName : "";
    }

    /** Apres une casse autorisee : le contenu tombe au sol. */
    static void broken(World world, Vector3i base) {
        Presentoir p = STANDS.get(Presentoir.key(world.getName(), base.x(), base.y(), base.z()));
        if (p == null) return;
        world.execute(() -> {
            if (STANDS.get(p.key()) == p && !isStand(WorldBlockUtil.getBlockType(world, p.x, p.y, p.z))) destroyed(world, p);
        });
    }

    private static void destroyed(World world, Presentoir p) {
        STANDS.remove(p.key());
        hide(p);
        Vector3d at = new Vector3d(p.x + 0.5, p.y + 0.5, p.z + 0.5);
        p.items.values().forEach(stack -> drop(world, stack, at));
        save();
    }

    private static void give(World world, ItemStack stack, Ref<EntityStore> ref, Inventory inv) {
        var result = inv.getCombinedHotbarFirst().addItemStack(stack);
        ItemStack rest = result.getRemainder();
        if (rest == null || rest.isEmpty()) return;
        TransformComponent t = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
        if (t != null) drop(world, rest, new Vector3d(t.getPosition()));
    }

    private static void drop(World world, ItemStack stack, Vector3d at) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        var holder = ItemComponent.generateItemDrop(store, stack, at, new Rotation3f(), 0, 0.2f, 0);
        if (holder != null) store.addEntity(holder, AddReason.SPAWN);
    }

    /** Lacet vers le joueur, arrondi a la face du bloc la plus proche : le mannequin regarde celui qui l'a garni. */
    private static float facing(Ref<EntityStore> ref, Presentoir p, Store<EntityStore> store) {
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return 0;
        Vector3d pos = t.getPosition();
        double dx = pos.x - (p.x + 0.5), dz = pos.z - (p.z + 0.5);
        double yaw = PhysicsMath.headingFromDirection(dx, dz);
        double quarter = Math.PI / 2;
        return (float) (Math.round(yaw / quarter) * quarter);
    }

    // ===== Rendu =====

    private static void hide(Presentoir p) {
        if (p.entityValid()) p.entity.getStore().removeEntity(p.entity, RemoveReason.REMOVE);
        p.entity = null;
    }

    private static void show(World world, Presentoir p) {
        Store<EntityStore> store = world.getEntityStore().getStore();
        String look = p.look();
        if (p.entityValid()) {
            PresentoirEntity current = store.getComponent(p.entity, PresentoirEntity.type());
            if (current != null && look.equals(current.look())) return;
            hide(p);
        }
        Vector3d center = new Vector3d(p.x + 0.5, p.y, p.z + 0.5);
        if (p.items.isEmpty() || !playerNear(p.world, center)) return;
        try {
            StandKind kind = StandKind.of(WorldBlockUtil.getBlockType(world, p.x, p.y, p.z));
            Vector3d pos = new Vector3d(center);
            Model model = p.hasArmor() ? mannequin(p, pos, kind) : support(p, pos, kind);
            if (model == null) return;
            Rotation3f rot = new Rotation3f();
            rot.setYaw(p.yaw);
            var holder = store.getRegistry().newHolder();
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
            holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(pos, rot));
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PresentoirEntity.type(), new PresentoirEntity(p.key(), look));
            ColorLight light = glow(p);
            if (light != null) holder.addComponent(DynamicLight.getComponentType(), new DynamicLight(light));
            holder.ensureComponent(UUIDComponent.getComponentType());
            holder.ensureComponent(store.getRegistry().getNonSerializedComponentType());
            store.addEntity(holder, AddReason.SPAWN);                                   // PresentoirEntitySystem renseigne p.entity
        } catch (Exception e) {
            EldaniorLogger.error("[Presentoir] affichage de " + p.key(), e);
        }
    }

    /** Lumiere de l'objet le plus precieux : la sienne s'il en a une, sinon la couleur de sa rarete a partir de Rare. */
    private static ColorLight glow(Presentoir p) {
        ItemQuality best = null;
        for (ItemStack stack : p.items.values()) {
            Item item = stack.getItem();
            if (item != null && item.getLight() != null) return item.getLight();
            ItemQuality q = ItemQuality.getAssetMap().getAsset(stack.getQualityIndex());
            if (q != null && !PLAIN_QUALITIES.contains(q.getId()) && (best == null || q.getQualityValue() > best.getQualityValue())) best = q;
        }
        if (best == null || best.getTextColor() == null) return null;
        var c = best.getTextColor();
        String hex = String.format("#%x%x%x", (c.red & 255) >> 4, (c.green & 255) >> 4, (c.blue & 255) >> 4);
        BsonDocument light = new BsonDocument("Color", new BsonString(hex));
        light.put("Radius", new BsonInt32(4));
        try {
            return ProtocolCodecs.COLOR_LIGHT.decode(light, EmptyExtraInfo.EMPTY);
        } catch (Exception e) {                                                         // la lueur ne doit jamais empecher l'affichage
            EldaniorLogger.error("[Presentoir] lumiere " + hex, e);
            return null;
        }
    }

    /** Objet seul : centre dans le presentoir, reduit s'il depasse, grossi s'il est trop petit pour ce presentoir. */
    private static Model support(Presentoir p, Vector3d pos, StandKind kind) {
        Item item = p.object();
        if (item == null) return null;
        ItemShape shape = ItemShape.of(model(item));
        boolean both = shape.right() && shape.left();
        float height = Math.max(1, shape.maxY() - shape.minY());
        float zoom = shape.size() < kind.minSize ? Math.min(kind.maxZoom, kind.minSize / Math.max(1, shape.size())) : 1;
        float scale = Math.min(zoom, Math.min(kind.roomHeight / height, kind.roomRadius / (shape.radius() + (both ? 10 : 0))));
        pos.y += kind.center - (shape.minY() + shape.maxY()) / 2 * scale / 64;
        double low = p.y + kind.floor + 0.02 - pos.y, high = p.y + kind.top - pos.y;    // reste dans la hitbox du bloc
        Box box = new Box(-0.4, low, -0.4, 0.4, high, 0.4);
        if (!shape.attached()) return build("Eldanior_Presentoir_Objet", scale, box, new ModelAttachment[0], model(item), texture(item));
        return build(both ? "Eldanior_Presentoir_Support_Double" : "Eldanior_Presentoir_Support", scale, box, attachments(p), null, null);
    }

    private static Model mannequin(Presentoir p, Vector3d pos, StandKind kind) {
        pos.y += kind.floor;
        Item item = p.object();
        ItemShape shape = item != null ? ItemShape.of(model(item)) : null;
        String asset = shape == null ? "Eldanior_Presentoir_Mannequin"
                : shape.right() && shape.left() ? "Eldanior_Presentoir_Mannequin_Double"
                : shape.left() ? "Eldanior_Presentoir_Mannequin_Gauche" : "Eldanior_Presentoir_Mannequin_Droite";
        return build(asset, kind.mannequinScale, new Box(-0.4, 0, -0.4, 0.4, 2, 0.4), attachments(p), null, null);
    }

    /** Modele montre pour un objet : celui de l'objet, sauf les katanas du Clan Luo (modele d'exposition, rengaine). */
    private static String model(Item item) {
        String display = com.eldanior.system.skills.runes.spells.LuoKatanas.displayModel(item.getId());
        return display != null ? display : item.getModel();
    }

    private static String texture(Item item) {
        String display = com.eldanior.system.skills.runes.spells.LuoKatanas.displayTexture(item.getId());
        return display != null ? display : item.getTexture();
    }

    private static ModelAttachment[] attachments(Presentoir p) {
        List<ModelAttachment> list = new ArrayList<>();
        for (ItemStack stack : p.items.values()) {
            Item item = stack.getItem();
            if (item != null && model(item) != null) list.add(new ModelAttachment(model(item), texture(item), null, null, 1));
        }
        return list.toArray(new ModelAttachment[0]);
    }

    /** Copie du modele avec ses attachements et sa boite (memes champs que Model.createScaledModel) ; model et texture le remplacent. */
    private static Model build(String assetId, float scale, Box box, ModelAttachment[] attachments, String model, String texture) {
        ModelAsset asset = ModelAsset.getAssetMap().getAsset(assetId);
        if (asset == null) {
            EldaniorLogger.error("[Presentoir] modele introuvable " + assetId);
            return null;
        }
        Model m = Model.createUnitScaleModel(asset);
        return new Model(m.getModelAssetId(), scale, m.getRandomAttachmentIds(), attachments, box, model != null ? model : m.getModel(), texture != null ? texture : m.getTexture(),
                m.getGradientSet(), m.getGradientId(), m.getEyeHeight(), m.getCrouchOffset(), m.getSittingOffset(), m.getSleepingOffset(),
                m.getAnimationSetMap(), m.getCamera(), m.getLight(), m.getParticles(), m.getTrails(), m.getPhysicsValues(),
                m.getDetailBoxes(), m.getPhobia(), m.getPhobiaModelAssetId());
    }

    private static boolean playerNear(String world, Vector3d at) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> ref = pr.getReference();
            World w = ref != null && ref.isValid() ? ref.getStore().getExternalData().getWorld() : null;
            Vector3d pos = w != null && w.getName().equals(world) ? PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid()) : null;
            if (pos != null && pos.distanceSquared(at) <= SHOW_RANGE * SHOW_RANGE) return true;
        }
        return false;
    }

    // ===== Sauvegarde =====

    private static void save() {
        BsonArray all = new BsonArray();
        for (Presentoir p : STANDS.values()) {
            BsonDocument d = new BsonDocument();
            d.put("World", new BsonString(p.world));
            d.put("X", new BsonInt32(p.x));
            d.put("Y", new BsonInt32(p.y));
            d.put("Z", new BsonInt32(p.z));
            d.put("Owner", new BsonString(p.owner.toString()));
            d.put("OwnerName", new BsonString(p.ownerName != null ? p.ownerName : ""));
            d.put("Yaw", new BsonDouble(p.yaw));
            BsonDocument items = new BsonDocument();                                    // l'ordre de depot est conserve
            p.items.forEach((slot, stack) -> items.put(slot.name(), ItemStack.CODEC.encode(stack, EmptyExtraInfo.EMPTY)));
            d.put("Items", items);
            all.add(d);
        }
        BsonDocument root = new BsonDocument("Presentoirs", all);
        EldaniorLogger.SCHEDULER.execute(() -> {                                        // file unique : ecritures dans l'ordre
            try {
                Files.createDirectories(file.getParent());
                BsonUtil.writeDocument(file, root).join();
            } catch (Exception e) {
                EldaniorLogger.error("[Presentoir] sauvegarde", e);
            }
        });
    }

    private static void load() {
        if (!Files.exists(file)) return;
        try {
            BsonDocument root = BsonUtil.readDocumentNow(file);
            if (root == null || !root.containsKey("Presentoirs")) return;
            for (BsonValue value : root.getArray("Presentoirs")) {
                BsonDocument d = value.asDocument();
                Presentoir p = new Presentoir(d.getString("World").getValue(), d.getInt32("X").getValue(), d.getInt32("Y").getValue(), d.getInt32("Z").getValue());
                p.owner = UUID.fromString(d.getString("Owner").getValue());
                p.ownerName = d.getString("OwnerName").getValue();
                p.yaw = (float) d.getDouble("Yaw").getValue();
                d.getDocument("Items").forEach((slot, stack) ->
                        p.items.put(Slot.valueOf(slot), ItemStack.CODEC.decode(stack, EmptyExtraInfo.EMPTY)));
                if (!p.items.isEmpty()) STANDS.put(p.key(), p);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Presentoir] chargement", e);
        }
    }
}
