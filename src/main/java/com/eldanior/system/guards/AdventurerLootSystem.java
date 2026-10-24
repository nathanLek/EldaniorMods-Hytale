package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Butin des aventuriers recrutes : chaque objet au sol (entite avec ItemComponent) regarde, une fois par seconde, si un
 * aventurier recrute au calme (sans cible, pas en repli) est a moins de LOOT_RANGE blocs. Si oui, l'objet va dans
 * l'inventaire de son joueur (s'il y a la place ; sinon il reste au sol).
 */
public class AdventurerLootSystem extends EntityTickingSystem<EntityStore> {

    private static final double LOOT_RANGE = 6.0;
    private static final long CHECK_MS = 1_000;
    private static final Map<Ref<EntityStore>, Long> checked = new ConcurrentHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> item = chunk.getReferenceTo(index);
        if (item == null || !item.isValid()) return;
        long now = System.currentTimeMillis();
        Long last = checked.get(item);
        if (last != null && now - last < CHECK_MS) return;
        checked.put(item, now);
        if (checked.size() > 512) checked.keySet().removeIf(r -> !r.isValid());
        try {
            ItemComponent ic = store.getComponent(item, ItemComponent.getComponentType());
            ItemStack stack = ic != null ? ic.getItemStack() : null;
            TransformComponent t = store.getComponent(item, TransformComponent.getComponentType());
            World world = store.getExternalData().getWorld();
            if (stack == null || ItemStack.isEmpty(stack) || t == null || world == null) return;
            for (GuardManager.Post p : GuardManager.getPosts()) {
                if (p.owner == null || p.guard == null || !p.world.equals(world.getName()) || AdventurerBehavior.isRetreating(p)) continue;
                Ref<EntityStore> companion = world.getEntityStore().getRefFromUUID(p.guard);
                if (companion == null || !companion.isValid() || companion.getStore() != store) continue;
                TransformComponent ct = store.getComponent(companion, TransformComponent.getComponentType());
                if (ct == null || ct.getPosition().distance(t.getPosition()) > LOOT_RANGE) continue;
                if (!AdventurerBehavior.calm(companion, store)) continue;                          // en combat : plus tard
                PlayerRef pr = Universe.get().getPlayer(p.owner);
                Ref<EntityStore> owner = pr != null ? pr.getReference() : null;
                if (owner == null || !owner.isValid() || owner.getStore() != store) continue;
                CombinedItemContainer bag = InventoryComponent.getCombined(store, owner, InventoryComponent.HOTBAR_FIRST);
                if (bag == null || !bag.addItemStack(stack, true, false, true).succeeded()) return;   // inventaire plein
                cb.tryRemoveEntity(item, RemoveReason.REMOVE);
                checked.remove(item);
                EldaniorLogger.info("[Aventurier] butin : " + stack.getItemId() + " ramasse pour " + pr.getUsername());
                AdventurerBehavior.talk(p, "J'ai ramassé quelque chose pour vous.", false);
                return;
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Aventurier] butin", e);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return ItemComponent.getComponentType();
    }
}