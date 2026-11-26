package com.eldanior.system.anchors;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.skills.runes.RuneManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Ancres : un Cercle d'Ancrage pose au sol (3 x 3, bloc pose = centre) et nomme par son proprietaire. Destination de la Porte
 * des Mondes (meme monde seulement). Limite par joueur : 1, ou {@link #PORTE_MONDES_ANCHORS} s'il possede la Porte des Mondes
 * (plus {@link #bonusAnchors}, point d'accroche des futures competences). Le cercle est donne avec toute rune d'Espace.
 * Sauvegarde : eldanior_data/ancres.json. Un cercle disparu (explosion, sol retire...) libere sa place au controle suivant.
 */
public final class AnchorManager {

    private AnchorManager() {}

    public static final String BLOCK_ID = "Eldanior_Cercle_Ancrage";
    public static final String ESPACE_CATEGORY = "Eldanior_Runes.Espace";
    public static final String PORTE_MONDES_RUNE = "Rune_PorteMondes";

    // ===== Reglages =====
    public static final int BASE_ANCHORS = 1, PORTE_MONDES_ANCHORS = 3;
    public static final int NAME_MIN = 2, NAME_MAX = 24;
    private static final long GRANT_EVERY_MS = 5_000;

    /** Une ancre posee. lastUsed : derniere ouverture de porte vers elle (ordre de la liste, destination par defaut). */
    public static final class Anchor {
        public final UUID owner;
        public final String world;
        public final int x, y, z;
        public volatile String ownerName, name;
        public volatile long lastUsed;

        Anchor(UUID owner, String ownerName, String name, String world, int x, int y, int z, long lastUsed) {
            this.owner = owner; this.ownerName = ownerName; this.name = name; this.world = world;
            this.x = x; this.y = y; this.z = z; this.lastUsed = lastUsed;
        }

        public String key() { return AnchorManager.key(world, x, y, z); }

        /** Point d'arrivee : centre du cercle, juste au-dessus du sol peint. */
        public Vector3d arrival() { return new Vector3d(x + 0.5, y + 0.1, z + 0.5); }

        public String coords() { return x + ", " + y + ", " + z; }
    }

    private static final Map<String, Anchor> ANCHORS = new ConcurrentHashMap<>();
    /** Choix de destination de la Porte des Mondes : joueur -> cle de l'ancre ("" = fenetre fermee sans choix). */
    private static final Map<UUID, String> CHOICES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastGrantCheck = new ConcurrentHashMap<>();
    private static Path file;

    static String key(String world, int x, int y, int z) { return world + ":" + x + ":" + y + ":" + z; }

    // ===== Cycle de vie =====

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("ancres.json");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                validateAll();
            } catch (Exception e) {
                EldaniorLogger.error("[Ancres] controle", e);
            }
        }, 20, 10, TimeUnit.SECONDS);
        EldaniorLogger.info("[Ancres] " + ANCHORS.size() + " ancre(s) chargee(s).");
    }

    public static void handleDisconnect(UUID uuid) {
        CHOICES.remove(uuid);
        lastGrantCheck.remove(uuid);
    }

    // ===== Regles =====

    public static boolean isCircle(BlockType type) { return type != null && BLOCK_ID.equals(type.getId()); }

    public static boolean isCircle(ItemStack stack) { return !ItemStack.isEmpty(stack) && BLOCK_ID.equals(stack.getItemId()); }

    /** Nombre d'ancres que le joueur peut poser. */
    public static int maxAnchors(Ref<EntityStore> ref, Store<EntityStore> store) {
        int max = RuneManager.ownsRune(ref, store, PORTE_MONDES_RUNE) ? PORTE_MONDES_ANCHORS : BASE_ANCHORS;
        return max + bonusAnchors(ref, store);
    }

    /** Ancres supplementaires (future competence de teleportation : brancher ici). */
    public static int bonusAnchors(Ref<EntityStore> ref, Store<EntityStore> store) { return 0; }

    /** Ancres du joueur, la plus recemment utilisee (ou posee) en premier. world null : tous les mondes. */
    public static List<Anchor> anchorsOf(UUID owner, String world) {
        List<Anchor> list = new ArrayList<>();
        for (Anchor a : ANCHORS.values())
            if (a.owner.equals(owner) && (world == null || a.world.equals(world))) list.add(a);
        list.sort(Comparator.comparingLong((Anchor a) -> a.lastUsed).reversed().thenComparing(a -> a.name));
        return list;
    }

    public static int countOf(UUID owner) { return anchorsOf(owner, null).size(); }

    public static Anchor find(String key) { return key != null ? ANCHORS.get(key) : null; }

    public static Anchor at(String world, Vector3i base) { return ANCHORS.get(key(world, base.x(), base.y(), base.z())); }

    /** Bloc central d'un cercle vise sur l'une de ses 8 cases de remplissage. */
    static Vector3i base(World world, Vector3i at) {
        int filler = WorldBlockUtil.getFiller(world, at.x(), at.y(), at.z());
        if (filler <= 0) return new Vector3i(at.x(), at.y(), at.z());
        return new Vector3i(at.x() - FillerBlockUtil.unpackX(filler), at.y() - FillerBlockUtil.unpackY(filler), at.z() - FillerBlockUtil.unpackZ(filler));
    }

    /** null si le nom convient, sinon la raison du refus. */
    public static String checkName(UUID owner, String name, Anchor self) {
        if (name == null || name.length() < NAME_MIN || name.length() > NAME_MAX)
            return "Le nom doit compter de " + NAME_MIN + " à " + NAME_MAX + " caractères.";
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && " -'_.!".indexOf(c) < 0) return "Lettres, chiffres, espaces et - ' _ . ! seulement.";
        }
        for (Anchor a : anchorsOf(owner, null))
            if (a != self && a.name.equalsIgnoreCase(name)) return "Vous avez déjà une ancre nommée « " + a.name + " ».";
        return null;
    }

    public static String cleanName(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }

    private static String defaultName(UUID owner, String ownerName) {
        String base = "Ancre de " + (ownerName != null && !ownerName.isBlank() ? ownerName : "voyageur");
        if (base.length() > NAME_MAX) base = base.substring(0, NAME_MAX);
        String name = base;
        for (int n = 2; checkName(owner, name, null) != null && n < 100; n++) {
            String suffix = " " + n;
            name = (base.length() + suffix.length() > NAME_MAX ? base.substring(0, NAME_MAX - suffix.length()) : base) + suffix;
        }
        return name;
    }

    // ===== Pose, nom, retrait =====

    /** Le cercle vient d'etre pose (bloc verifie). Thread du monde. Renvoie l'ancre creee (nom par defaut). */
    static Anchor placed(World world, Vector3i at, UUID owner, String ownerName) {
        Anchor a = new Anchor(owner, ownerName, defaultName(owner, ownerName), world.getName(), at.x(), at.y(), at.z(), System.currentTimeMillis());
        ANCHORS.put(a.key(), a);
        save();
        return a;
    }

    static void rename(Anchor a, String name) {
        a.name = name;
        save();
    }

    static void removed(Anchor a) {
        if (ANCHORS.remove(a.key(), a)) save();
    }

    public static void markUsed(Anchor a) {
        a.lastUsed = System.currentTimeMillis();
        save();
    }

    /** Le cercle est-il toujours la ? false seulement si le chunk est charge et que le bloc a disparu (l'ancre est alors retiree). */
    public static boolean stillThere(World world, Anchor a) {
        BlockType type = WorldBlockUtil.getBlockType(world, a.x, a.y, a.z);
        if (type == null || isCircle(type)) return true;                                // chunk non charge : on fait confiance
        removed(a);
        PlayerRef owner = Universe.get().getPlayer(a.owner);
        if (owner != null) owner.sendMessage(Message.raw("Votre ancre « " + a.name + " » a été effacée.").color("#e8c060"));
        return false;
    }

    private static void validateAll() {
        Map<String, List<Anchor>> byWorld = new HashMap<>();
        for (Anchor a : ANCHORS.values()) byWorld.computeIfAbsent(a.world, w -> new ArrayList<>()).add(a);
        byWorld.forEach((name, anchors) -> {
            World world = Universe.get().getWorld(name);
            if (world != null) world.execute(() -> anchors.forEach(a -> { if (ANCHORS.get(a.key()) == a) stillThere(world, a); }));
        });
    }

    // ===== Choix de destination (Porte des Mondes) =====

    public static void clearChoice(UUID uuid) { CHOICES.remove(uuid); }

    static void choose(UUID uuid, String key) { CHOICES.put(uuid, key != null ? key : ""); }

    /** Choix fait dans la fenetre : cle de l'ancre, "" si fermee sans choix, null si pas encore de reponse. Consomme le choix. */
    public static String takeChoice(UUID uuid) { return CHOICES.remove(uuid); }

    // ===== Don du cercle avec les runes d'Espace =====

    /**
     * Toutes les 5 s (appele par RuneSystem) : un joueur qui possede une rune d'Espace recoit des Cercles d'Ancrage jusqu'a avoir,
     * entre ceux qu'il porte et ceux qu'il a poses, autant que sa limite d'ancres. Jamais jete au sol : inventaire plein = plus tard.
     */
    public static void grantCircles(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef pr) {
        UUID uuid = pr.getUuid();
        long now = System.currentTimeMillis();
        Long last = lastGrantCheck.get(uuid);
        if (last != null && now - last < GRANT_EVERY_MS) return;
        lastGrantCheck.put(uuid, now);
        if (!RuneManager.ownsRuneInCategory(ref, store, ESPACE_CATEGORY)) return;
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        var inv = player.getInventory();
        int held = countHeld(inv.getCombinedBackpackStorageHotbar());
        int missing = maxAnchors(ref, store) - held - countOf(uuid);
        if (missing <= 0) return;
        ItemStack stack = new ItemStack(BLOCK_ID, missing);
        if (!inv.getCombinedHotbarFirst().canAddItemStack(stack)) return;
        inv.getCombinedHotbarFirst().addItemStack(stack);
        pr.sendMessage(Message.raw(missing > 1 ? "Vous recevez " + missing + " Cercles d'Ancrage." : "Vous recevez un Cercle d'Ancrage.").color("#c8a8ff")
                .insert(Message.raw(" Posez-le au sol puis nommez-le : il devient une ancre, point d'arrivée de la Porte des Mondes.").color("#a9b8c8")));
    }

    private static int countHeld(com.hypixel.hytale.server.core.inventory.container.ItemContainer container) {
        int[] n = {0};
        container.forEach((slot, stack) -> { if (isCircle(stack)) n[0] += stack.getQuantity(); });
        return n[0];
    }

    // ===== Sauvegarde =====

    private static void save() {
        if (file == null) return;
        BsonArray all = new BsonArray();
        for (Anchor a : ANCHORS.values()) {
            BsonDocument d = new BsonDocument();
            d.put("Owner", new BsonString(a.owner.toString()));
            d.put("OwnerName", new BsonString(a.ownerName != null ? a.ownerName : ""));
            d.put("Name", new BsonString(a.name));
            d.put("World", new BsonString(a.world));
            d.put("X", new BsonInt32(a.x));
            d.put("Y", new BsonInt32(a.y));
            d.put("Z", new BsonInt32(a.z));
            d.put("LastUsed", new BsonInt64(a.lastUsed));
            all.add(d);
        }
        BsonDocument root = new BsonDocument("Ancres", all);
        EldaniorLogger.SCHEDULER.execute(() -> {                                        // file unique : ecritures dans l'ordre
            try {
                Files.createDirectories(file.getParent());
                BsonUtil.writeDocument(file, root).join();
            } catch (Exception e) {
                EldaniorLogger.error("[Ancres] sauvegarde", e);
            }
        });
    }

    private static void load() {
        if (!Files.exists(file)) return;
        try {
            BsonDocument root = BsonUtil.readDocumentNow(file);
            if (root == null || !root.containsKey("Ancres")) return;
            for (BsonValue value : root.getArray("Ancres")) {
                BsonDocument d = value.asDocument();
                Anchor a = new Anchor(UUID.fromString(d.getString("Owner").getValue()), d.getString("OwnerName").getValue(),
                        d.getString("Name").getValue(), d.getString("World").getValue(), d.getInt32("X").getValue(),
                        d.getInt32("Y").getValue(), d.getInt32("Z").getValue(), d.getInt64("LastUsed").getValue());
                ANCHORS.put(a.key(), a);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Ancres] chargement", e);
        }
    }
}
