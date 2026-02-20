package com.eldanior.system.config.configs;

import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum LootTableConfig {

    // === FORET ===
    FOREST_COMMON("treasure_forest_common", 300,
            new LootEntry("Weapon_Sword_Crude",           1, 1,  1, 30),
            new LootEntry("Plant_Fruit_Apple",           5, 1,  20, 100),
            new LootEntry("Potion_Regen_Stamina_Small",           1, 1,  3, 15),
            new LootEntry("Potion_Health_Small",           1, 1,  3, 15),
            new LootEntry("Weapon_Shield_Rusty",          1, 1,  1,  60),
            new LootEntry("Weapon_Axe_Tribal",      1, 1,  1,  40),
            new LootEntry("Weapon_Shortbow_Bomb",   1,  1,  1,  35),
            new LootEntry("Food_Chicken_Raw",   5,  1,  5,  100)
    ),

    DEFAULT("treasure_default", 300,
            new LootEntry("Weapon_Sword_Crude",           1, 1,  1, 30),
            new LootEntry("Plant_Fruit_Apple",           5, 1,  20, 100),
            new LootEntry("Tool_Repair_kit_Iron",           5, 1,  3, 20),
            new LootEntry("Potion_Regen_Stamina_Small",           1, 1,  3, 15),
            new LootEntry("Potion_Health_Small",           1, 1,  3, 15),
            new LootEntry("Weapon_Shield_Rusty",          1, 1,  1,  60),
            new LootEntry("Weapon_Axe_Tribal",      1, 1,  4,  40),
            new LootEntry("Weapon_Shortbow_Bomb",   1,  1,  1,  35),
            new LootEntry("Food_Chicken_Raw",   5,  1,  5,  100)
    );

    private final String tableId;
    private final long cooldownSeconds;
    private final LootEntry[] entries;

    LootTableConfig(String tableId, long cooldownSeconds, LootEntry... entries) {
        this.tableId = tableId;
        this.cooldownSeconds = cooldownSeconds;
        this.entries = entries;
    }

    public String getTableId() {
        return tableId;
    }

    public long getCooldownMillis() {
        return cooldownSeconds * 1000L;
    }

    public LootEntry[] getEntries() {
        return entries;
    }

    public List<ItemStack> generateLoot(long seed) {
        Random rng = new Random(seed);
        List<ItemStack> result = new ArrayList<>();

        for (LootEntry entry : entries) {
            if (rng.nextInt(100) < entry.dropChance()) {
                int quantity = entry.minQuantity();
                if (entry.maxQuantity() > entry.minQuantity()) {
                    quantity += rng.nextInt(entry.maxQuantity() - entry.minQuantity() + 1);
                }
                result.add(new ItemStack(entry.itemId(), quantity));
            }
        }

        return result;
    }

    public static LootTableConfig getById(String tableId) {
        for (LootTableConfig table : values()) {
            // On compare le tableId défini dans le constructeur ("treasure_forest_common")
            if (table.tableId.equalsIgnoreCase(tableId)) {
                return table;
            }
        }
        return DEFAULT;
    }


    public record LootEntry(String itemId, int maxStack, int minQuantity, int maxQuantity, int dropChance) {

    }
}