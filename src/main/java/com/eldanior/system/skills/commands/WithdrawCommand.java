package com.eldanior.system.skills.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public class WithdrawCommand extends AbstractPlayerCommand {

    private static final LinkedHashMap<String, Long> COINS = new LinkedHashMap<>();

    static {
        COINS.put("Elda_Zenith_Coins",   10000L);
        COINS.put("Elda_Diamond_Coins",   1000L);
        COINS.put("Elda_Gold_Coins",       100L);
        COINS.put("Elda_Silver_Coins",      10L);
        COINS.put("Elda_Copper_Coins",       1L);
    }

    private final RequiredArg<Integer> amountArg;

    public WithdrawCommand() {
        super("bankGive", "Retire de l'argent de ta banque sous forme de pieces");
        this.amountArg = this.withRequiredArg("montant", "Montant a retirer", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (player == null || data == null) return;

        long amount = this.amountArg.get(ctx).longValue();

        if (amount <= 0) {
            player.sendMessage(Message.raw("Le montant doit etre superieur a 0."));
            return;
        }

        if (data.getMoney() < amount) {
            player.sendMessage(Message.raw("Solde insuffisant. Vous avez " + data.getMoney() + " Eldan."));
            return;
        }

        LinkedHashMap<String, Integer> breakdown = decompose(amount);

        CombinedItemContainer combined = InventoryComponent.getCombined(store, ref, InventoryComponent.HOTBAR_FIRST);

        int freeSlots = 0;
        for (short i = 0; i < combined.getCapacity(); i++) {
            if (ItemStack.isEmpty(combined.getItemStack(i))) freeSlots++;
        }

        if (freeSlots < breakdown.size()) {
            player.sendMessage(Message.raw("<color:red>Inventaire plein. Il vous faut " + breakdown.size()
                    + " slots libres (vous en avez " + freeSlots + ").</color>"));
            return;
        }

        data.removeMoney(amount);

        StringBuilder summary = new StringBuilder("<color:green>Retrait de " + amount + " Eldan :</color>\n");
        for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            ItemStack stack = new ItemStack(entry.getKey(), entry.getValue(), 0.0, 0.0, null);
            combined.addItemStack(stack, true, false, true);
            summary.append("  ").append(entry.getValue()).append("x ").append(getCoinLabel(entry.getKey())).append("\n");
        }
        summary.append("Solde restant : ").append(data.getMoney()).append(" Eldan");
        player.sendMessage(Message.raw(summary.toString()));
    }

    private LinkedHashMap<String, Integer> decompose(long amount) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        long remaining = amount;
        for (Map.Entry<String, Long> coin : COINS.entrySet()) {
            if (remaining <= 0) break;
            long count = remaining / coin.getValue();
            if (count > 0) {
                while (count > 0) {
                    int stackSize = (int) Math.min(count, 64);
                    result.merge(coin.getKey(), stackSize, Integer::sum);
                    count -= stackSize;
                }
                remaining %= coin.getValue();
            }
        }
        return result;
    }

    private String getCoinLabel(String itemId) {
        return switch (itemId) {
            case "Elda_Zenith_Coins"  -> "Zenith";
            case "Elda_Diamond_Coins" -> "Diamond";
            case "Elda_Gold_Coins"    -> "Gold";
            case "Elda_Silver_Coins"  -> "Silver";
            case "Elda_Copper_Coins"  -> "Copper";
            default -> itemId;
        };
    }
}