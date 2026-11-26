package com.eldanior.system.armors;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bonus de tenue complete : Tenue Ancestrale du Dieu-Dragon d'Or (Armor_Kimono_Divin_*).
 *
 * Les 4 pieces portees : effet Armure_Dieu_Dragon (+15 % de vitesse) et sauts illimites dans les airs.
 * Le nombre de sauts en l'air est lu par le client sur l'objet porte (ItemArmor.MovementSettings.ExtraJumpCount) et ne peut pas
 * etre change par joueur : le hakama (1 saut de plus) est donc echange contre le hakama eveille (sauts illimites), identique a
 * l'oeil, tant que la tenue est complete. Des qu'elle ne l'est plus, tout hakama eveille trouve chez le joueur (pieces portees,
 * barre, sac, sac a dos, utilitaire) redevient un hakama normal, durabilite et metadonnees conservees.
 */
public class ArmorSetBonusSystem extends DelayedEntitySystem<EntityStore> {

    private static final float INTERVAL_SECONDS = 0.5f;

    private static final String PREFIX = "Armor_Kimono_Divin_";
    private static final String LEGS = PREFIX + "Legs";
    private static final String LEGS_AWAKENED = PREFIX + "Legs_Eveil";
    private static final String EFFECT = "Armure_Dieu_Dragon";
    /** Ordre des emplacements d'armure : Head, Chest, Hands, Legs (ItemArmorSlot). */
    private static final String[] PIECES = {PREFIX + "Head", PREFIX + "Chest", PREFIX + "Hands", LEGS};
    private static final short LEGS_SLOT = 3;

    public ArmorSetBonusSystem() { super(INTERVAL_SECONDS); }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;
        InventoryComponent armorSection = store.getComponent(ref, InventoryComponent.Armor.getComponentType());
        if (armorSection == null) return;
        ItemContainer armor = armorSection.getInventory();

        boolean complete = isComplete(armor);
        if (complete) {
            ItemStack legs = armor.getItemStack(LEGS_SLOT);
            if (legs != null && LEGS.equals(legs.getItemId())) {
                armor.setItemStackForSlot(LEGS_SLOT, swap(legs, LEGS_AWAKENED));
                armorSection.markDirty();
            }
            if (!EffectsManager.hasEffect(ref, EFFECT, store)) EffectsManager.applyInfiniteEffect(ref, EFFECT, store);
        } else {
            revertAwakened(ref, store);
            if (EffectsManager.hasEffect(ref, EFFECT, store)) EffectsManager.removeEffect(ref, EFFECT, store);
        }
    }

    private static boolean isComplete(ItemContainer armor) {
        for (short slot = 0; slot < PIECES.length; slot++) {
            ItemStack stack = armor.getItemStack(slot);
            if (stack == null || stack.isEmpty()) return false;
            String id = stack.getItemId();
            if (!PIECES[slot].equals(id) && !(slot == LEGS_SLOT && LEGS_AWAKENED.equals(id))) return false;
        }
        return true;
    }

    /** Rend un hakama normal a la place de chaque hakama eveille, ou qu'il soit dans l'inventaire du joueur. */
    private static void revertAwakened(Ref<EntityStore> ref, Store<EntityStore> store) {
        for (ComponentType<EntityStore, ? extends InventoryComponent> type : sections()) {
            InventoryComponent section = store.getComponent(ref, type);
            if (section == null) continue;
            ItemContainer container = section.getInventory();
            Map<Short, ItemStack> replace = new HashMap<>();
            container.forEach((slot, stack) -> {
                if (stack != null && LEGS_AWAKENED.equals(stack.getItemId())) replace.put(slot, swap(stack, LEGS));
            });
            if (replace.isEmpty()) continue;
            replace.forEach(container::setItemStackForSlot);
            section.markDirty();
        }
    }

    private static ItemStack swap(ItemStack from, String id) {
        return new ItemStack(id, Math.max(1, from.getQuantity()), from.getDurability(), from.getMaxDurability(), from.getMetadata());
    }

    private static List<ComponentType<EntityStore, ? extends InventoryComponent>> sections() {
        return List.of(
                InventoryComponent.Armor.getComponentType(),
                InventoryComponent.Hotbar.getComponentType(),
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Backpack.getComponentType(),
                InventoryComponent.Utility.getComponentType());
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Player.getComponentType(); }
}
