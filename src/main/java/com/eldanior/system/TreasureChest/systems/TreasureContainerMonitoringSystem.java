package com.eldanior.system.TreasureChest.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.components.OpenedContainerComponent;
import com.eldanior.system.TreasureChest.components.PlayerChestData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TreasureContainerMonitoringSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, OpenedContainerComponent> containerComponentType;

    public TreasureContainerMonitoringSystem(ComponentType<EntityStore, OpenedContainerComponent> type) {
        this.containerComponentType = type;
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
        OpenedContainerComponent monitor = archetypeChunk.getComponent(index, this.containerComponentType);
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());

        if (player == null || monitor == null) return;

        World world = player.getWorld();

        // ✅ Utilisation de TA méthode (issue de TreasureChestBreakBlockEvent) au lieu de world.getState()
        assert world != null;
        ItemContainerBlock container = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(), world, monitor.getX(), monitor.getY(), monitor.getZ()
        );

        boolean stillOpen = false;
        if (container != null) {
            if (!container.getWindows().isEmpty()) {
                stillOpen = true;
            }
        }

        if (!stillOpen) {
            PlayerChestData playerChestData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerChestDataType());

            if (container != null) {
                if (playerChestData != null) {
                    List<ItemStack> itemsRemaining = new ArrayList<>();

                    // On récupère l'état EXACT du coffre au moment de la fermeture
                    // Utilisation de short pour la boucle comme dans ton GenerateTreasureCommand
                    for (short i = 0; i < container.getCapacity(); i++) {
                        ItemStack stack = container.getItemContainer().getItemStack(i);
                        if (stack != null) {
                            itemsRemaining.add(stack);
                        }
                    }

                    // MISE À JOUR DE LA PERSISTANCE (Si vide, on enregistre une liste vide)
                    playerChestData.setInventory(monitor.getX(), monitor.getY(), monitor.getZ(), world.getName(), itemsRemaining);
                    commandBuffer.replaceComponent(playerRef, EldaniorSystem.get().getPlayerChestDataType(), playerChestData);

                    // On nettoie le monde physique
                    container.getItemContainer().clear();
                }
            }
            commandBuffer.removeComponent(playerRef, this.containerComponentType);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), this.containerComponentType);
    }
}