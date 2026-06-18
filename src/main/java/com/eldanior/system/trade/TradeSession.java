package com.eldanior.system.trade;

import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.UUID;

public class TradeSession {

    public static final int SLOTS_PER_PLAYER = 12; // 2 colonnes x 6 lignes

    private final UUID player1;
    private final UUID player2;
    private final ItemStack[] player1Items;
    private final ItemStack[] player2Items;
    private volatile boolean player1Validated;
    private volatile boolean player2Validated;
    private volatile boolean cancelled;

    public TradeSession(UUID player1, UUID player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Items = new ItemStack[SLOTS_PER_PLAYER];
        this.player2Items = new ItemStack[SLOTS_PER_PLAYER];
        this.player1Validated = false;
        this.player2Validated = false;
        this.cancelled = false;
    }

    public UUID getPlayer1() { return player1; }
    public UUID getPlayer2() { return player2; }
    public boolean isCancelled() { return cancelled; }

    public UUID getOther(UUID self) {
        return self.equals(player1) ? player2 : player1;
    }

    public ItemStack[] getMyItems(UUID self) {
        return self.equals(player1) ? player1Items : player2Items;
    }

    public ItemStack[] getOtherItems(UUID self) {
        return self.equals(player1) ? player2Items : player1Items;
    }

    public boolean setItem(UUID self, int slot, ItemStack item) {
        if (slot < 0 || slot >= SLOTS_PER_PLAYER) return false;
        // Reset validation quand un item change
        player1Validated = false;
        player2Validated = false;
        ItemStack[] items = getMyItems(self);
        items[slot] = item;
        return true;
    }

    public ItemStack takeItem(UUID self, int slot) {
        if (slot < 0 || slot >= SLOTS_PER_PLAYER) return null;
        ItemStack[] items = getMyItems(self);
        ItemStack taken = items[slot];
        items[slot] = null;
        player1Validated = false;
        player2Validated = false;
        return taken;
    }

    public boolean validate(UUID self) {
        if (self.equals(player1)) player1Validated = true;
        else if (self.equals(player2)) player2Validated = true;
        return player1Validated && player2Validated;
    }

    public boolean isValidated(UUID self) {
        return self.equals(player1) ? player1Validated : player2Validated;
    }

    public boolean isBothValidated() {
        return player1Validated && player2Validated;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public int firstEmptySlot(UUID self) {
        ItemStack[] items = getMyItems(self);
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null || items[i].isEmpty()) return i;
        }
        return -1;
    }
}
