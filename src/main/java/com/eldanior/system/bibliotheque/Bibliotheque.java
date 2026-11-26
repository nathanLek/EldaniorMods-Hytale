package com.eldanior.system.bibliotheque;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.ItemSlotFilter;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Bibliotheque evolutive (Eldanior_Bibliotheque) : un contenant de 18 cases reserve aux ouvrages.
 * Vide, le bloc montre l'etagere Furniture_Village_Shelf_Full (modele de base) ; des qu'un ouvrage y est range,
 * il passe a l'etat "Pleine" (modele de Furniture_Village_Bookcase). Le contenu est celui du bloc
 * (ItemContainerBlock vanilla) : sauvegarde avec le monde et lache au sol quand on casse le bloc.
 *
 * Le contenant est ouvert ici et non par Open_Container : l'interaction vanilla pose les etats
 * OpenWindow/CloseWindow et effacerait l'etat "Pleine".
 */
public final class Bibliotheque {

    public static final String BLOCK_ID = "Eldanior_Bibliotheque";
    static final String STATE_PLEINE = "Pleine";
    private static final String STATE_VIDE = "default";

    /** Mots cherches dans l'identifiant ou le modele d'un objet. Ajouter ici les futurs types d'ouvrages. */
    private static final List<String> OUVRAGES = List.of(
            "parchemin", "manuel", "livre", "rouleau", "grimoire",
            "book", "scroll", "spellbook", "recipe", "rune_page", "skill_page");

    private static final ItemSlotFilter FILTRE = Bibliotheque::accepts;

    private Bibliotheque() {}

    public static boolean isLibrary(BlockType type) {
        Item item = type != null ? type.getItem() : null;
        return item != null && BLOCK_ID.equals(item.getId());
    }

    /** Parchemins, manuels, livres... Les blocs sont refuses (sinon "Bookcase" passerait pour un livre). */
    public static boolean accepts(Item item) {
        if (item != null && com.eldanior.system.histoires.Livres.isLivre(item.getId())) return true;   // histoires et Tomes : blocs, mais des livres
        if (item == null || item.hasBlockType()) return false;
        String id = String.valueOf(item.getId()).toLowerCase(Locale.ROOT);
        String model = item.getModel() != null ? item.getModel().toLowerCase(Locale.ROOT) : "";
        for (String mot : OUVRAGES) if (id.contains(mot) || model.contains(mot)) return true;
        return false;
    }

    /** Ouvre la bibliotheque comme un coffre (meme fenetre que les coffres vanilla). Thread du monde. */
    static void open(World world, Ref<EntityStore> ref, Player player, PlayerRef pr, UUID uuid, int x, int y, int z) {
        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
        if (!isLibrary(type)) return;
        ItemContainerBlock box = BlockModule.getComponent(ItemContainerBlock.getComponentType(), world, x, y, z);
        if (box == null) {
            pr.sendMessage(Message.raw("Cette bibliothèque n'a pas de rangement : casse-la et repose-la.").color("#e8c060"));
            return;
        }
        SimpleItemContainer books = box.getItemContainer();
        for (short slot = 0; slot < books.getCapacity(); slot++) books.setSlotFilter(FilterActionType.ADD, slot, FILTRE);   // filtres non sauvegardes : remis a chaque ouverture

        ContainerBlockWindow window = new ContainerBlockWindow(x, y, z, WorldBlockUtil.getRotationIndex(world, x, y, z), type, books);
        if (box.getWindows().putIfAbsent(uuid, window) != null) return;   // deja ouverte par ce joueur
        Store<EntityStore> store = ref.getStore();
        if (!player.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window)) {
            box.getWindows().remove(uuid, window);
            return;
        }
        EventRegistration<?, ?> change = books.registerChangeEvent(e -> world.execute(() -> refresh(world, x, y, z)));
        window.registerCloseEvent(e -> {
            box.getWindows().remove(uuid, window);
            change.unregister();
            world.execute(() -> refresh(world, x, y, z));
        });
    }

    /** Etagere si vide, bibliotheque remplie sinon. Ne touche au bloc que si l'etat doit changer. */
    static void refresh(World world, int x, int y, int z) {
        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
        if (!isLibrary(type)) return;
        ItemContainerBlock box = BlockModule.getComponent(ItemContainerBlock.getComponentType(), world, x, y, z);
        if (box == null) return;
        BlockType base = BlockType.getAssetMap().getAsset(BLOCK_ID);
        BlockType pleine = base != null ? base.getBlockForState(STATE_PLEINE) : null;
        if (pleine == null) return;
        boolean wantFull = !box.getItemContainer().isEmpty();
        boolean isFull = pleine.getId().equals(type.getId());
        if (wantFull == isFull) return;
        ChunkStore chunks = world.getChunkStore();
        Ref<ChunkStore> section = chunks.getChunkSectionReferenceAtBlock(x, y, z);
        if (section == null || !section.isValid()) return;
        BlockOperations.setBlockInteractionState(chunks, section, x, y, z, type, wantFull ? STATE_PLEINE : STATE_VIDE, false);
    }
}
