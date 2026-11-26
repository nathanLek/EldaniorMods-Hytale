package com.eldanior.system.monsterloot;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.util.List;

/**
 * Butin propre a chaque monstre : a la mort, tire sa table Server/Drops/Butin/Drop_Butin_*.json et fait tomber le resultat.
 * S'ajoute aux drops vanilla. Appele par DeathXPSystem pour les kills d'un joueur ou d'un aventurier recrute.
 * Catalogue, items et tables : scripts/butin/gen_butin.py.
 */
public final class MonsterLoot {

    private MonsterLoot() {
    }

    public static void drop(Ref<EntityStore> victim, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        NPCEntity npc = store.getComponent(victim, NPCEntity.getComponentType());
        if (npc == null) return;
        String list = MonsterLootTable.listFor(npc.getNPCTypeId());
        if (list == null) return;
        ItemModule items = ItemModule.get();
        if (items == null || !items.isEnabled()) return;
        List<ItemStack> drops = items.getRandomItemDrops(list);
        if (drops.isEmpty()) return;
        TransformComponent transform = store.getComponent(victim, TransformComponent.getComponentType());
        if (transform == null) return;
        Vector3d pos = new Vector3d(transform.getPosition()).add(0, 1, 0);
        Holder<EntityStore>[] holders = ItemComponent.generateItemDrops(store, drops, pos, Rotation3f.ZERO);
        cb.addEntities(holders, AddReason.SPAWN);
    }
}
