package com.eldanior.system.skills.system;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Empeche le joueur de jeter une rune (tout item portant un bloc "Ability").
 *
 * On intercepte la DEMANDE de drop (avant retrait du slot) : l'annuler laisse la rune en place,
 * sans risque de perte. Le drop a la mort est gere cote JSON via "DropOnDeath": false.
 */
public class RuneDropProtectionSystem extends EntityEventSystem<EntityStore, DropItemEvent.PlayerRequest> {

    public RuneDropProtectionSystem() {
        super(DropItemEvent.PlayerRequest.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl DropItemEvent.PlayerRequest event) {

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        ItemContainer section = InventoryUtils.getSectionById(ref, event.getInventorySectionId(), store);
        if (section == null) return;

        ItemStack stack = section.getItemStack(event.getSlotId());
        if (ItemStack.isEmpty(stack) || stack.getItem() == null || stack.getItem().getAbility() == null) return;

        event.setCancelled(true);
        PlayerRef netRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (netRef != null) {
            NotificationHelper.sendWarning(netRef, "<color:red>Les runes ne peuvent pas etre jetees.</color>");
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
