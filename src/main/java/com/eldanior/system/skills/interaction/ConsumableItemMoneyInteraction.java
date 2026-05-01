package com.eldanior.system.skills.interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.CoinItemRegistry;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class ConsumableItemMoneyInteraction extends SimpleInteraction {

    public ConsumableItemMoneyInteraction() { super(); }

    public static final BuilderCodec<ConsumableItemMoneyInteraction> CODEC =
            BuilderCodec.builder(ConsumableItemMoneyInteraction.class, ConsumableItemMoneyInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun || type != InteractionType.Use) return;

        var playerRef = context.getOwningEntity();
        Player player = playerRef.getStore().getComponent(playerRef, Player.getComponentType());
        PlayerLevelData data = playerRef.getStore().getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());

        if (player == null || data == null) return;

        ItemStack heldItem = player.getInventory().getHotbar().getItemStack(context.getHeldItemSlot());
        if (heldItem == null) return;

        String itemId = heldItem.getItemId();

        CoinItemRegistry.getValueFor(itemId).ifPresent(coinValue -> {
            data.addMoney(coinValue);

            // Hook progression quete COLLECTION + notification or
            try {
                var pRefComp = playerRef.getStore().getComponent(playerRef, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
                if (pRefComp != null) {
                                        java.util.UUID uuid = UUIDExtractor.getUUID(pRefComp);
                    com.eldanior.system.quest.QuestManager.onGoldGained(uuid, coinValue);
                    com.eldanior.system.Leveling.utils.NotificationHelper.sendNotification(pRefComp,
                            "<color:gold>+" + coinValue + " Or</color> <color:gray>| Solde : " + data.getMoney() + "</color>",
                            com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Success);
                }
            } catch (Exception e) { EldaniorLogger.error("ConsumableItemMoneyInteraction", e); }

            int slot = context.getHeldItemSlot();
            EldaniorLogger.SCHEDULER.schedule(() -> {
                    try {
                        player.getInventory().getHotbar().removeItemStackFromSlot((short) slot, 1, true, false);
                    } catch (Exception e) { EldaniorLogger.error("ConsumableItemMoneyInteraction", e); }
                }, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
        });
    }
}