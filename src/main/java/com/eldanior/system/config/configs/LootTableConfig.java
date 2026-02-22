package com.eldanior.system.config.configs;

import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum LootTableConfig {

    // === FORET ===
    FOREST_COMMON("donjon_common", 300,
            new LootEntry("Weapon_Sword_Crude",           1, 1,  1, 30),
            new LootEntry("Plant_Fruit_Apple",           5, 1,  20, 100),
            new LootEntry("Potion_Regen_Stamina_Small",           1, 1,  3, 15),
            new LootEntry("Potion_Health_Small",           1, 1,  3, 15),
            new LootEntry("Weapon_Shield_Rusty",          1, 1,  1,  60),
            new LootEntry("Weapon_Axe_Tribal",      1, 1,  1,  40),
            new LootEntry("Weapon_Shortbow_Bomb",   1,  1,  1,  35),
            new LootEntry("Food_Chicken_Raw",   5,  1,  5,  100)
    ),

    DEFAULT("default", 10,
            new LootEntry("Plant_Fruit_Apple",           5, 1,  20, 100),
            new LootEntry("Plant_Crop_Corn_Item",           5, 1,  20, 100),
            new LootEntry("Food_Chicken_Raw",   5,  1,  5,  100),

            new LootEntry("Armor_Leather_Light_Hands",   1,  1,  1,  10),
            new LootEntry("Armor_Leather_Light_Chest",   1,  1,  1,  10),
            new LootEntry("Armor_Leather_Light_Legs",   1,  1,  1,  10),
            new LootEntry("Armor_Leather_Light_Head",   1,  1,  1,  10),

            new LootEntry("Weapon_Sword_Crude",           1, 1,  1, 30),
            new LootEntry("Weapon_Shield_Rusty",          1, 1,  1,  40),
            new LootEntry("Weapon_Axe_Tribal",      1, 1,  1,  40),
            new LootEntry("Weapon_Shortbow_Bomb",   1,  1,  1,  35),
            new LootEntry("Weapon_Shield_Orbis_Knight",   1,  1,  1,  1),
            new LootEntry("Weapon_Arrow_Clearshot",   1,  1,  15,  35),
            new LootEntry("Weapon_Axe_Crude",   1,  1,  1,  35),
            new LootEntry("Weapon_Axe_Copper",   1,  1,  1,  25),
            new LootEntry("Weapon_Axe_Iron_Rusty",   1,  1,  1,  35),
            new LootEntry("Weapon_Battleaxe_Copper",   1,  1,  1,  25),
            new LootEntry("Weapon_Battleaxe_Crude",   1,  1,  1,  35),
            new LootEntry("Weapon_Battleaxe_Scarab",   1,  1,  1,  15),
            new LootEntry("Weapon_Battleaxe_Wood_Fence",   1,  1,  1,  35),
            new LootEntry("Weapon_Club_Crude",   1,  1,  1,  35),
            new LootEntry("Weapon_Daggers_Copper",   1,  1,  1,  25),
            new LootEntry("Weapon_Daggers_Crude",   1,  1,  1,  35),
            new LootEntry("Weapon_Longsword_Scarab",   1,  1,  1,  15),
            new LootEntry("Weapon_Longsword_Crude",   1,  1,  1,  35),
            new LootEntry("Weapon_Longsword_Copper",   1,  1,  1,  25),
            new LootEntry("Weapon_Mace_Crude",   1,  1,  1,  35),
            new LootEntry("Weapon_Mace_Copper",   1,  1,  1,  25),
            new LootEntry("Weapon_Spear_Copper",   1,  1,  1,  25),
            new LootEntry("Weapon_Spear_Crude",   1,  1,  1,  35),
            new LootEntry("Weapon_Sword_Copper",   1,  1,  1,  25),
            new LootEntry("Weapon_Longsword_Tribal",   1,  1,  1,  10),

            new LootEntry("PortalKey_Dungeon_V1",           1,  1,  1,  0.02),
            new LootEntry("Deco_Treasure",           1,  1,  10,  10),
            new LootEntry("Ore_Copper",           1,  1,  1,  2),
            new LootEntry("Potion_Regen_Stamina_Small",           1, 1,  3, 15),
            new LootEntry("Potion_Health_Small",           1, 1,  3, 15),
            new LootEntry("Ore_Iron",           1,  1,  1,  0.5)


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


    public record LootEntry(String itemId, int maxStack, int minQuantity, int maxQuantity, double dropChance) {

    }
}