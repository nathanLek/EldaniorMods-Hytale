package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Inventory.components.PlayerPersonalChestData;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class InventaireTab {

    public static void populate(UICommandBuilder ui, UIEventBuilder events,
                                Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerPersonalChestData chestData = getChestData(ref, store);

        // Chest slots
        for (int i = 0; i < PlayerPersonalChestData.CHEST_SIZE; i++) {
            String slotId = "#InvSlot" + i;
            ItemStack item = chestData.getItem(i);
            renderSlot(ui, slotId, item);

            if (events != null) {
                events.addEventBinding(CustomUIEventBindingType.Activating, slotId,
                        EventData.of("Action", "inv_take").append("Param", String.valueOf(i)));
            }
        }

        // Hotbar slots
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            ItemContainer hotbar = player.getInventory().getHotbar();
            for (int i = 0; i < 9; i++) {
                String slotId = "#InvHotbar" + i;
                ItemStack item = hotbar.getItemStack((short) i);
                renderSlot(ui, slotId, item);

                if (events != null) {
                    events.addEventBinding(CustomUIEventBindingType.Activating, slotId,
                            EventData.of("Action", "inv_deposit").append("Param", String.valueOf(i)));
                }
            }
        }
    }

    public static void refreshOnly(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        populate(ui, null, ref, store);
    }

    private static void renderSlot(UICommandBuilder ui, String slotId, ItemStack item) {
        String iconPath = slotId + " #Icon";
        String qtyPath = slotId + " #Qty";

        boolean hasItem = item != null && !item.isEmpty();
        ui.set(iconPath + ".Visible", hasItem);

        if (hasItem) {
            ui.set(iconPath + ".ItemId", item.getItemId());
            ui.set(qtyPath + ".Text", item.getQuantity() > 1 ? String.valueOf(item.getQuantity()) : "");
            ui.set(slotId + ".TooltipText", buildTooltip(item));
        } else {
            ui.set(qtyPath + ".Text", "");
            ui.set(slotId + ".TooltipText", "");
        }
    }

    private static String buildTooltip(ItemStack stack) {
        StringBuilder sb = new StringBuilder();

        // === NOM ===
        String name = getTranslatedName(stack);
        sb.append(name);

        try {
            Item itemConfig = stack.getItem();
            if (itemConfig == null) return name;

            // === RARETE ===
            try {
                int qualityIdx = itemConfig.getQualityIndex();
                var qualityMap = ItemQuality.getAssetMap();
                if (qualityMap != null) {
                    ItemQuality quality = qualityMap.getAsset(qualityIdx);
                    if (quality != null) {
                        String rarityName = translate(quality.getLocalizationKey());
                        if (rarityName == null) rarityName = formatItemName(quality.getId());
                        sb.append("\n").append(rarityName);
                    }
                }
            } catch (Exception ignored) {}

            // === DESCRIPTION ===
            try {
                String descKey = itemConfig.getDescriptionTranslationKey();
                String desc = translate(descKey);
                if (desc != null && !desc.isEmpty()) {
                    sb.append("\n");
                    sb.append("\n\"").append(desc).append("\"");
                }
            } catch (Exception ignored) {}

            // === INFOS GENERALES ===
            boolean hasInfo = false;

            if (stack.getQuantity() > 1) {
                if (!hasInfo) { sb.append("\n\n---- Infos ----"); hasInfo = true; }
                sb.append("\nQuantite : ").append(stack.getQuantity()).append(" / ").append(itemConfig.getMaxStack());
            }

            int level = itemConfig.getItemLevel();
            if (level > 0) {
                if (!hasInfo) { sb.append("\n\n---- Infos ----"); hasInfo = true; }
                sb.append("\nNiveau : ").append(level);
            }

            double dur = stack.getDurability();
            double maxDur = stack.getMaxDurability();
            if (maxDur > 0) {
                if (!hasInfo) { sb.append("\n\n---- Infos ----"); hasInfo = true; }
                int pct = (int) ((dur / maxDur) * 100);
                sb.append("\nDurabilite : ").append((int) dur).append(" / ").append((int) maxDur)
                  .append(" (").append(pct).append("%)");
            }

            // === EQUIPEMENT ===
            boolean hasEquip = false;

            if (itemConfig.getArmor() != null) {
                var armor = itemConfig.getArmor();
                sb.append("\n\n---- Armure ----");
                hasEquip = true;
                if (armor.getArmorSlot() != null) {
                    sb.append("\nEmplacement : ").append(formatItemName(armor.getArmorSlot().name()));
                }
                double resist = armor.getBaseDamageResistance();
                if (resist > 0) {
                    sb.append("\nResistance : +").append((int) resist);
                }
            }

            if (itemConfig.getWeapon() != null) {
                if (!hasEquip) sb.append("\n\n---- Equipement ----");
                sb.append("\nType : Arme");
            }

            if (itemConfig.getTool() != null) {
                if (!hasEquip) sb.append("\n\n---- Equipement ----");
                sb.append("\nType : Outil");
            }

            if (itemConfig.isConsumable()) {
                sb.append("\n\nConsommable");
            }

        } catch (Exception ignored) {}

        return sb.toString();
    }

    private static String getTranslatedName(ItemStack stack) {
        try {
            Item itemConfig = stack.getItem();
            if (itemConfig != null) {
                String key = itemConfig.getTranslationKey();
                String translated = translate(key);
                if (translated != null) return translated;
            }
        } catch (Exception ignored) {}
        return formatItemName(stack.getItemId());
    }

    private static String translate(String key) {
        if (key == null || key.isEmpty()) return null;
        try {
            String result = I18nModule.get().getMessage("en_us", key);
            if (result != null && !result.isEmpty() && !result.equals(key)) return result;
        } catch (Exception ignored) {}
        return null;
    }

    private static String formatItemName(String itemId) {
        // "Hytale:iron_sword" -> "Iron Sword"
        String id = itemId;
        int colon = id.indexOf(':');
        if (colon >= 0) id = id.substring(colon + 1);

        String[] parts = id.replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    public static boolean handleTake(Ref<EntityStore> ref, Store<EntityStore> store, int slotIndex) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || slotIndex < 0 || slotIndex >= PlayerPersonalChestData.CHEST_SIZE) return false;

        PlayerPersonalChestData chestData = getChestData(ref, store);
        ItemStack item = chestData.getItem(slotIndex);
        if (item == null || item.isEmpty()) return false;

        var result = player.getInventory().getHotbar().addItemStack(item);
        if (result.succeeded()) {
            chestData.clearSlot(slotIndex);
            persistChest(ref, store, chestData);
            return true;
        }
        return false;
    }

    public static boolean handleDeposit(Ref<EntityStore> ref, Store<EntityStore> store, int hotbarSlot) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || hotbarSlot < 0 || hotbarSlot >= 9) return false;

        ItemContainer hotbar = player.getInventory().getHotbar();
        ItemStack item = hotbar.getItemStack((short) hotbarSlot);
        if (item == null || item.isEmpty()) return false;

        PlayerPersonalChestData chestData = getChestData(ref, store);
        int emptySlot = chestData.findFirstEmptySlot();
        if (emptySlot == -1) return false;

        chestData.setItem(emptySlot, item);
        hotbar.removeItemStackFromSlot((short) hotbarSlot);
        persistChest(ref, store, chestData);
        return true;
    }

    private static PlayerPersonalChestData getChestData(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerPersonalChestData> type = EldaniorSystem.get().getPlayerPersonalChestDataType();
        PlayerPersonalChestData data = store.getComponent(ref, type);
        if (data == null) {
            data = new PlayerPersonalChestData();
            store.putComponent(ref, type, data);
        }
        return data;
    }

    private static void persistChest(Ref<EntityStore> ref, Store<EntityStore> store, PlayerPersonalChestData data) {
        ComponentType<EntityStore, PlayerPersonalChestData> type = EldaniorSystem.get().getPlayerPersonalChestDataType();
        store.putComponent(ref, type, data);
    }
}
