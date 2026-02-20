package com.eldanior.system.TreasureChest.events;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.TreasureChest.components.OpenedContainerComponent;
import com.eldanior.system.TreasureChest.components.PlayerChestData;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.eldanior.system.config.configs.LootTableConfig;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings({"deprecation", "removal", "unchecked", "ConstantConditions"})
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
        com.hypixel.hytale.server.core.universe.world.meta.BlockState blockState = player.getWorld().getState(target.getX(), target.getY(), target.getZ(), true);

        if (blockState instanceof ItemContainerState containerState) {
            TreasureChestTemplate template = containerState.getReference().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);

            if (event.getInteractionType().toString().equals("Use") && template != null && template.hasTemplate(target.getX(), target.getY(), target.getZ())) {

                PlayerChestData playerData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerChestDataType());
                if (playerData == null) {
                    playerData = new PlayerChestData();
                    commandBuffer.addComponent(playerRef, EldaniorSystem.get().getPlayerChestDataType(), playerData);
                }

                String worldName = player.getWorld().getName();

                List<ItemStack> savedInventory = playerData.getInventory(target.getX(), target.getY(), target.getZ(), worldName);
                boolean discovered = playerData.isDiscovered(target.getX(), target.getY(), target.getZ(), worldName);

                if (savedInventory == null || savedInventory.isEmpty()) {

                    String droplist = template.getDropList(target.getX(), target.getY(), target.getZ());
                    LootTableConfig table = LootTableConfig.getById(droplist);

                    long currentTime = System.currentTimeMillis();
                    long lastLootTime = playerData.getLastLootTime(target.getX(), target.getY(), target.getZ(), worldName);
                    long cooldownMillis = table.getCooldownMillis();

                    // Si jamais découvert OU le cooldown est passé
                    if (!discovered || (currentTime - lastLootTime) >= cooldownMillis) {

                        List<ItemStack> rawLoot = table.generateLoot(ThreadLocalRandom.current().nextLong());
                        List<ItemStack> finalSelection = new ArrayList<>();

                        if (rawLoot != null && !rawLoot.isEmpty()) {
                            int targetCount = ThreadLocalRandom.current().nextInt(1, 5);
                            Collections.shuffle(rawLoot);

                            for (int i = 0; i < targetCount && i < rawLoot.size(); i++) {
                                finalSelection.add(rawLoot.get(i));
                            }

                            shuffleAndFill(containerState, finalSelection);

                            playerData.setDiscovered(target.getX(), target.getY(), target.getZ(), worldName, true);
                            playerData.setInventory(target.getX(), target.getY(), target.getZ(), worldName, finalSelection);
                            playerData.setLastLootTime(target.getX(), target.getY(), target.getZ(), worldName, currentTime);
                            commandBuffer.replaceComponent(playerRef, EldaniorSystem.get().getPlayerChestDataType(), playerData);

                            // ==========================================
                            // NOTIFICATION : SUCCÈS NOUVEAU LOOT
                            // ==========================================
                            String msgSuccess = "<color:gold>Trésor découvert !</color> <color:gray>(+" + finalSelection.size() + " objets)</color>";
                            NotificationHelper.sendNotification(player.getPlayerRef(), msgSuccess, NotificationStyle.Success);

                        }
                    } else {
                        // Le cooldown n'est pas terminé : on vide le conteneur
                        containerState.getItemContainer().clear();

                        // Calcul du temps restant pour un affichage propre (ex: 4m 53s)
                        long timeLeftSeconds = (cooldownMillis - (currentTime - lastLootTime)) / 1000;
                        long minutes = timeLeftSeconds / 60;
                        long seconds = timeLeftSeconds % 60;
                        String timeFormatted = (minutes > 0 ? minutes + "m " : "") + seconds + "s";

                        // ==========================================
                        // NOTIFICATION : COOLDOWN ACTIF (COFFRE VIDE)
                        // ==========================================
                        String msgCooldown = "<color:red>Ce coffre est vide...</color> <color:gray>(Recharge dans " + timeFormatted + ")</color>";
                        // Note: Utilise NotificationStyle.Warning ou Error si tu les as dans ton Enum
                        NotificationHelper.sendNotification(player.getPlayerRef(), msgCooldown, NotificationStyle.Warning);
                    }
                } else {
                    // Si l'inventaire contient encore des objets, on les affiche sans spam de notification
                    fillFixed(containerState, savedInventory);
                }

                // On s'assure que le monitor est présent pour sauver à la fermeture
                commandBuffer.addComponent(playerRef, EldaniorSystem.OPENED_CONTAINER_TYPE, new OpenedContainerComponent(target.getX(), target.getY(), target.getZ()));
            }
        }
    }

    private void fillFixed(ItemContainerState container, List<ItemStack> items) {
        container.getItemContainer().clear();
        for (int i = 0; i < container.getItemContainer().getCapacity() && i < items.size(); i++) {
            if (items.get(i) != null) container.getItemContainer().setItemStackForSlot((short) i, items.get(i));
        }
    }

    private void shuffleAndFill(ItemContainerState container, List<ItemStack> stacks) {
        short capacity = container.getItemContainer().getCapacity();
        List<Short> slots = new ArrayList<>();
        for (short s = 0; s < capacity; s++) slots.add(s);
        Collections.shuffle(slots, ThreadLocalRandom.current());

        container.getItemContainer().clear();
        for (int idx = 0; idx < stacks.size() && idx < slots.size(); idx++) {
            if (stacks.get(idx) != null) {
                container.getItemContainer().setItemStackForSlot(slots.get(idx), stacks.get(idx));
                System.out.println("[DEBUG ELDANIOR] Ajout item: " + stacks.get(idx).getItemId());
            }
        }
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}