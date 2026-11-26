package com.eldanior.system.poche;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Poche dimensionnelle (2026-11-16) : un rangement de 4 rangees de 9 (CAPACITY) propre a chaque joueur, HORS de son inventaire.
 * Le jeu ne la vide donc jamais a la mort (DeathSystems ne lache que l'inventaire), contrairement au sac a dos et a la bourse.
 *
 * Retours du 2026-11-16 (2e serie) :
 *  - elle s'achete (10 pieces imperiales, chez le Maroquinier : NpcTrades.Metier.MAROQUINIER) et ne se donne plus ;
 *  - elle ne se porte pas dans l'emplacement d'utilitaire du jeu (deja pris par un sac) : clic droit avec l'objet = elle
 *    s'equipe dans SA case du menu Systeme (en haut a droite du personnage, sur la ligne du Niveau, #PocheSlot), l'objet est consomme ; une poche
 *    equipee ne se perd jamais ;
 *  - un clic sur cette case remplace le panneau du menu par la poche (PocheTab : grille de la poche + inventaire), sans
 *    quitter le menu.
 * A l'arrivee, le sac a dos du jeu est debloque au niveau 1 (9 cases, comme Upgrade_Backpack_1) ; /es setlevel 1 le ramene a
 * ce niveau (resetBackpack). Contenu : eldanior_data/poches/<uuid>.bson, ecrit 2 s apres chaque changement.
 * Objet et textures : scripts/poche/gen_poche.py.
 */
public final class PocheDimensionnelle {

    private PocheDimensionnelle() {}

    public static final String ITEM_ID = "Eldanior_Poche_Dimensionnelle";
    /** 4 rangees de 9 (le grand sac a dos du jeu en a 2). */
    public static final short CAPACITY = 36;
    /** Sac a dos du jeu debloque a l'arrivee : niveau 1 (= Upgrade_Backpack_1). */
    public static final short BACKPACK_LEVEL_1 = 9;
    /** Prix de la poche chez le Maroquinier : 10 000 000 cuivres (10 pieces imperiales). */
    public static final long PRICE = 10_000_000L;

    private static final Map<UUID, Poche> POCHES = new ConcurrentHashMap<>();
    /** Pas de poche dans la poche. */
    private static final com.hypixel.hytale.server.core.inventory.container.filter.ItemSlotFilter NO_POCHE = PocheDimensionnelle::accepts;

    static boolean accepts(com.hypixel.hytale.server.core.asset.type.item.config.Item item) {
        return item == null || !ITEM_ID.equals(item.getId());
    }

    private static Path dir;

    /** Une poche chargee : contenu, equipee ou non, sauvegarde en attente. */
    private static final class Poche {
        final SimpleItemContainer items = new SimpleItemContainer(CAPACITY);
        volatile boolean equipped;
        volatile boolean saving;
    }

    public static void init(Path pluginDataDir) {
        dir = pluginDataDir.resolve("eldanior_data").resolve("poches");
    }

    private static Poche poche(UUID uuid) {
        return POCHES.computeIfAbsent(uuid, PocheDimensionnelle::load);
    }

    private static Poche load(UUID uuid) {
        Poche p = new Poche();
        Path file = dir != null ? dir.resolve(uuid + ".bson") : null;
        if (file != null && Files.exists(file)) {
            try {
                BsonDocument root = BsonUtil.readDocumentNow(file);
                if (root != null) {
                    p.equipped = root.containsKey("Equipped") && root.getBoolean("Equipped").getValue();
                    if (root.containsKey("Slots")) root.getDocument("Slots").forEach((slot, stack) -> {
                        short s = Short.parseShort(slot);
                        if (s >= 0 && s < CAPACITY) p.items.setItemStackForSlot(s, ItemStack.CODEC.decode(stack, EmptyExtraInfo.EMPTY));
                    });
                }
            } catch (Exception e) {
                EldaniorLogger.error("[Poche] chargement " + uuid, e);
            }
        }
        for (short s = 0; s < CAPACITY; s++) p.items.setSlotFilter(FilterActionType.ADD, s, NO_POCHE);   // filtres non sauvegardes
        p.items.registerChangeEvent(e -> saveLater(uuid, p));
        return p;
    }

    private static void saveLater(UUID uuid, Poche p) {
        if (p.saving) return;
        p.saving = true;
        EldaniorLogger.SCHEDULER.schedule(() -> { p.saving = false; save(uuid, p); }, 2, TimeUnit.SECONDS);
    }

    private static void save(UUID uuid, Poche p) {
        if (dir == null) return;
        try {
            BsonDocument slots = new BsonDocument();
            for (short s = 0; s < CAPACITY; s++) {
                ItemStack stack = p.items.getItemStack(s);
                if (stack != null && !stack.isEmpty()) slots.put(String.valueOf(s), ItemStack.CODEC.encode(stack, EmptyExtraInfo.EMPTY));
            }
            BsonDocument root = new BsonDocument("Equipped", new BsonBoolean(p.equipped));
            root.put("Slots", slots);
            Files.createDirectories(dir);
            BsonUtil.writeDocument(dir.resolve(uuid + ".bson"), root).join();
        } catch (Exception e) {
            EldaniorLogger.error("[Poche] sauvegarde " + uuid, e);
        }
    }

    /** La poche du joueur est-elle equipee (case du menu Systeme) ? */
    public static boolean isEquipped(UUID uuid) {
        return uuid != null && poche(uuid).equipped;
    }

    /** Contenu de la poche equipee, null si le joueur n'en a pas. */
    public static SimpleItemContainer container(UUID uuid) {
        Poche p = uuid != null ? poche(uuid) : null;
        return p != null && p.equipped ? p.items : null;
    }

    /**
     * Clic droit avec l'objet (OuvrirPocheInteraction) : l'equipe dans la case du menu (l'objet tenu est consomme) si le joueur
     * n'en a pas encore, puis ouvre le menu Systeme sur la poche. Thread du monde.
     */
    public static void useItem(Ref<EntityStore> ref, Store<EntityStore> store, int heldSlot) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        Poche p = poche(pr.getUuid());
        if (!p.equipped) {
            var hotbar = player.getInventory().getHotbar();
            ItemStack held = heldSlot >= 0 && heldSlot < hotbar.getCapacity() ? hotbar.getItemStack((short) heldSlot) : null;
            if (held == null || held.isEmpty() || !ITEM_ID.equals(held.getItemId())) return;
            if (held.getQuantity() > 1) hotbar.setItemStackForSlot((short) heldSlot, held.withQuantity(held.getQuantity() - 1));
            else hotbar.removeItemStackFromSlot((short) heldSlot);
            p.equipped = true;
            save(pr.getUuid(), p);
            NotificationHelper.sendSuccess(pr, "Poche dimensionnelle équipée : sa case est dans le menu Système, à droite de votre "
                    + "personnage, sous le niveau. Son contenu ne se perd jamais, même à la mort.");
        } else {
            NotificationHelper.sendWarning(pr, "Vous avez déjà une poche dimensionnelle équipée : celle-ci reste dans votre sac.");
        }
        open(ref, store);
    }

    /** Ouvre le menu Systeme directement sur la poche. Thread du monde. */
    public static void open(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        player.getPageManager().openCustomPage(ref, store, new com.eldanior.system.gui.SystemScreen(pr).openOnPoche());
    }

    /** Arrivee du joueur (PlayerLoginSystem, thread du monde) : sac a dos du jeu debloque au niveau 1. */
    public static void onLogin(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            if (ref == null || !ref.isValid()) return;
            InventoryComponent.Backpack backpack = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());
            if (backpack != null && backpack.getInventory() != null && backpack.getInventory().getCapacity() < BACKPACK_LEVEL_1)
                backpack.resize(BACKPACK_LEVEL_1, new ArrayList<>());
        } catch (Exception e) {
            EldaniorLogger.error("[Poche] arrivee du joueur", e);
        }
    }

    /**
     * /es setlevel 1 (remise a zero) : le sac a dos du jeu revient au niveau 1 (9 cases). Les objets des cases retirees vont
     * dans l'inventaire, le reste tombe aux pieds du joueur. Thread du monde.
     */
    public static void resetBackpack(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            InventoryComponent.Backpack backpack = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());
            Player player = store.getComponent(ref, Player.getComponentType());
            if (backpack == null || backpack.getInventory() == null || player == null) return;
            if (backpack.getInventory().getCapacity() == BACKPACK_LEVEL_1) return;
            List<ItemStack> removed = new ArrayList<>();
            backpack.resize(BACKPACK_LEVEL_1, removed);
            giveOrDrop(ref, store, player, removed);
        } catch (Exception e) {
            EldaniorLogger.error("[Poche] remise du sac a dos", e);
        }
    }

    /**
     * /es setlevel 1 (2026-11-16, demande de l'utilisateur) : la poche dimensionnelle est supprimee. Elle quitte sa case du menu
     * Systeme et les poches non equipees de l'inventaire disparaissent aussi : le joueur doit en retrouver ou en racheter une.
     * Son contenu n'est pas perdu : rendu dans l'inventaire, le surplus tombe aux pieds du joueur. Thread du monde.
     */
    public static void resetPoche(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            if (player == null || pr == null) return;
            var bag = player.getInventory().getCombinedBackpackStorageHotbar();
            int spare = 0;
            for (short s = 0; s < bag.getCapacity(); s++) {                              // poches non equipees de l'inventaire
                ItemStack st = bag.getItemStack(s);
                if (st != null && !st.isEmpty() && ITEM_ID.equals(st.getItemId())) { spare += st.getQuantity(); bag.removeItemStackFromSlot(s); }
            }
            Poche p = poche(pr.getUuid());
            List<ItemStack> content = new ArrayList<>();
            for (short s = 0; s < CAPACITY; s++) {
                ItemStack st = p.items.getItemStack(s);
                if (st != null && !st.isEmpty()) content.add(st);
                p.items.removeItemStackFromSlot(s);
            }
            boolean had = p.equipped;
            p.equipped = false;
            save(pr.getUuid(), p);
            giveOrDrop(ref, store, player, content);
            if (had || spare > 0 || !content.isEmpty())
                NotificationHelper.sendWarning(pr, "Votre poche dimensionnelle a disparu" + (content.isEmpty() ? "." : " : son contenu est revenu dans votre sac.")
                        + " Il faudra en retrouver ou en acheter une.");
            EldaniorLogger.info("[Poche] remise a zero de " + pr.getUsername() + " : equipee " + had + ", " + spare + " en reserve, " + content.size() + " pile(s) rendue(s)");
        } catch (Exception e) {
            EldaniorLogger.error("[Poche] remise a zero de la poche", e);
        }
    }

    /** Objets rendus dans l'inventaire ; ce qui ne rentre pas tombe aux pieds du joueur. Thread du monde. */
    private static void giveOrDrop(Ref<EntityStore> ref, Store<EntityStore> store, Player player, List<ItemStack> stacks) {
        List<ItemStack> left = new ArrayList<>();
        var bag = player.getInventory().getCombinedBackpackStorageHotbar();
        for (ItemStack st : stacks) {
            if (st == null || st.isEmpty()) continue;
            var tx = bag.addItemStack(st);
            ItemStack rest = tx != null && tx.succeeded() ? tx.getRemainder() : st;
            if (rest != null && !rest.isEmpty()) left.add(rest);
        }
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        HeadRotation head = store.getComponent(ref, HeadRotation.getComponentType());
        if (!left.isEmpty() && t != null && head != null)
            store.addEntities(ItemComponent.generateItemDrops(store, left, new org.joml.Vector3d(t.getPosition()).add(0, 1, 0), head.getRotation()),
                    AddReason.SPAWN);
    }

    /** Arret du serveur : toutes les poches chargees. */
    public static void saveAll() {
        for (Map.Entry<UUID, Poche> e : POCHES.entrySet()) save(e.getKey(), e.getValue());
    }

    /** Deconnexion : sauvee et dechargee. */
    public static void unload(UUID uuid) {
        Poche p = POCHES.remove(uuid);
        if (p != null) save(uuid, p);
    }
}
