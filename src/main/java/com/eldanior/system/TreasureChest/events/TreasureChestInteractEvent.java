package com.eldanior.system.TreasureChest.events;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.systems.LuckSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.TreasureChest.components.OpenedContainerComponent;
import com.eldanior.system.TreasureChest.components.PlayerChestData;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.LootTableConfig;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TreasureChestInteractEvent extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public TreasureChestInteractEvent() {
        super(UseBlockEvent.Pre.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl UseBlockEvent.Pre event) {
        Ref<EntityStore> playerRef = event.getContext().getEntity();
        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        Vector3i target = event.getTargetBlock();
        World world = player.getWorld();

        assert world != null;
        ItemContainerBlock container = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(),
                world,
                target.getX(), target.getY(), target.getZ()
        );
        if (container == null) return;

        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);

        if (!event.getInteractionType().toString().equals("Use")) return;
        if (!template.hasTemplate(target.getX(), target.getY(), target.getZ())) return;

        PlayerChestData playerData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerChestDataType());
        if (playerData == null) {
            playerData = new PlayerChestData();
            commandBuffer.addComponent(playerRef, EldaniorSystem.get().getPlayerChestDataType(), playerData);
        }

        String worldName = world.getName();
        List<ItemStack> savedInventory = playerData.getInventory(target.getX(), target.getY(), target.getZ(), worldName);
        boolean discovered = playerData.isDiscovered(target.getX(), target.getY(), target.getZ(), worldName);

        SimpleItemContainer itemContainer = container.getItemContainer();

        if (savedInventory == null || savedInventory.isEmpty()) {
            String droplist = template.getDropList(target.getX(), target.getY(), target.getZ());
            LootTableConfig table = LootTableConfig.getById(droplist);

            long currentTime = System.currentTimeMillis();
            long lastLootTime = playerData.getLastLootTime(target.getX(), target.getY(), target.getZ(), worldName);
            long cooldownMillis = table.getCooldownMillis();

            if (!discovered || (currentTime - lastLootTime) >= cooldownMillis) {
                List<ItemStack> rawLoot = table.generateLoot(ThreadLocalRandom.current().nextLong());

                if (!rawLoot.isEmpty()) {
                    PlayerLevelData levelData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
                    float luckBonus = LuckSystem.getLootQualityBonus(levelData);

                    int targetCount = ThreadLocalRandom.current().nextInt(1, 6);
                    float preciseExtra = (float) Math.sqrt(Math.max(0, luckBonus)) / 10.0f;
                    int extraItems = (int) preciseExtra;

                    if (ThreadLocalRandom.current().nextFloat() < (preciseExtra - extraItems)) {
                        extraItems++;
                    }

                    targetCount = Math.min(targetCount + extraItems, rawLoot.size());
                    Collections.shuffle(rawLoot);

                    List<ItemStack> finalSelection = new ArrayList<>(rawLoot.subList(0, targetCount));

                    shuffleAndFill(itemContainer, finalSelection);

                    playerData.setDiscovered(target.getX(), target.getY(), target.getZ(), worldName, true);
                    playerData.setInventory(target.getX(), target.getY(), target.getZ(), worldName, finalSelection);
                    playerData.setLastLootTime(target.getX(), target.getY(), target.getZ(), worldName, currentTime);
                    commandBuffer.replaceComponent(playerRef, EldaniorSystem.get().getPlayerChestDataType(), playerData);

                    String msgSuccess = "<color:gold>Trésor découvert !</color> <color:gray>(+" + finalSelection.size() + " objets)</color>";
                    PlayerRef pRef = store.getComponent(playerRef, PlayerRef.getComponentType());
                    assert pRef != null;
                    NotificationHelper.sendNotification(pRef, msgSuccess, NotificationStyle.Success);
                }
            } else {
                itemContainer.clear();

                long timeLeftSeconds = (cooldownMillis - (currentTime - lastLootTime)) / 1000;
                long minutes = timeLeftSeconds / 60;
                long seconds = timeLeftSeconds % 60;
                String timeFormatted = (minutes > 0 ? minutes + "m " : "") + seconds + "s";

                String msgCooldown = "<color:red>Ce coffre est vide...</color> <color:gray>(Recharge dans " + timeFormatted + ")</color>";
                PlayerRef pRef = store.getComponent(playerRef, PlayerRef.getComponentType());
                assert pRef != null;
                NotificationHelper.sendNotification(pRef, msgCooldown, NotificationStyle.Warning);
            }
        } else {
            fillFixed(itemContainer, savedInventory);
        }

        commandBuffer.addComponent(playerRef, EldaniorSystem.OPENED_CONTAINER_TYPE,
                new OpenedContainerComponent(target.getX(), target.getY(), target.getZ()));
    }

    private void fillFixed(SimpleItemContainer itemContainer, List<ItemStack> items) {
        itemContainer.clear();
        for (int i = 0; i < itemContainer.getCapacity() && i < items.size(); i++) {
            if (items.get(i) != null) itemContainer.setItemStackForSlot((short) i, items.get(i));
        }
    }

    private void shuffleAndFill(SimpleItemContainer itemContainer, List<ItemStack> stacks) {
        short capacity = itemContainer.getCapacity();
        List<Short> slots = new ArrayList<>();
        for (short s = 0; s < capacity; s++) slots.add(s);
        Collections.shuffle(slots, ThreadLocalRandom.current());

        itemContainer.clear();
        for (int idx = 0; idx < stacks.size() && idx < slots.size(); idx++) {
            if (stacks.get(idx) != null) {
                itemContainer.setItemStackForSlot(slots.get(idx), stacks.get(idx));
                System.out.println("[DEBUG ELDANIOR] Ajout item: " + stacks.get(idx).getItemId());
            }
        }
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}