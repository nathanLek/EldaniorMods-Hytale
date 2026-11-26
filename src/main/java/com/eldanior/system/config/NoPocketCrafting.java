package com.eldanior.system.config;

import com.hypixel.hytale.builtin.crafting.window.FieldCraftingWindow;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.window.CraftRecipeAction;
import com.hypixel.hytale.protocol.packets.window.WindowAction;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Fabrication rapide (« Fieldcraft », le marteau de l'inventaire) desactivee pour tout le monde (2026-11-25) : il faut un etabli.
 *
 * Le client demande la fenetre PocketCrafting a chaque ouverture de l'inventaire (Window.CLIENT_REQUESTABLE_WINDOW_TYPES,
 * rempli par CraftingPlugin). 1re version (refuser d'ouvrir la fenetre) : l'inventaire entier ne s'ouvrait plus. Maintenant la
 * fenetre s'ouvre normalement et seule l'action de fabrication (CraftRecipeAction, la seule qu'elle recoit) est refusee.
 */
public final class NoPocketCrafting {

    private NoPocketCrafting() {}

    /** Fenetre de fabrication rapide qui s'ouvre (l'inventaire en depend) mais ne fabrique rien. */
    static final class NoCraft extends FieldCraftingWindow {
        @Override
        public void handleAction(Ref<EntityStore> ref, Store<EntityStore> store, WindowAction action) {
            if (action instanceof CraftRecipeAction) {
                try {
                    PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
                    if (pr != null) pr.sendMessage(Message.raw("La fabrication rapide est désactivée : utilisez un établi.").color("#e0a050"));
                } catch (Exception ignored) { }
                return;
            }
            super.handleAction(ref, store, action);
        }
    }

    /** A appeler apres le demarrage des plugins (CraftingPlugin a enregistre sa fenetre), puis a chaque monde demarre. */
    public static void apply() {
        Window.CLIENT_REQUESTABLE_WINDOW_TYPES.put(WindowType.PocketCrafting, NoCraft::new);
    }
}
