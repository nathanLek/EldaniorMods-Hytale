package com.eldanior.system.Inventory.gui;

import com.eldanior.system.Inventory.components.PlayerPersonalChestData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

public class PersonalChestPage extends InteractiveCustomUIPage<PersonalChestPage.ChestEventData> {

    private final PlayerPersonalChestData chestData;

    public PersonalChestPage(@Nonnull PlayerRef playerRef, @Nonnull PlayerPersonalChestData chestData) {
        super(playerRef, CustomPageLifetime.CanDismiss, ChestEventData.CODEC);
        this.chestData = chestData;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        // 1. Charger le fichier UI
        commands.append("PersonalChest.ui");

        List<ItemStack> items = chestData.getStoredItems();

        // 2. Boucle sur les 27 slots
        for (int i = 0; i < 27; i++) {
            String slotRoot = "#Slot" + i;
            String iconPath = slotRoot + " #Icon";
            String qtyPath = slotRoot + " #Quantity";

            if (i < items.size() && items.get(i) != null && !items.get(i).isEmpty()) {
                // --- CAS : ITEM PRÉSENT ---
                ItemStack item = items.get(i);
                String itemId = item.getItemId();
                int quantity = item.getQuantity();

                commands.set(iconPath + ".Visible", true);
                commands.set(iconPath + ".ItemId", itemId);

                if (quantity > 1) {
                    commands.set(qtyPath + ".Visible", true);
                    commands.set(qtyPath + ".Text", Message.raw(String.valueOf(quantity)));
                } else {
                    commands.set(qtyPath + ".Visible", false);
                }

                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        slotRoot,
                        EventData.of("Action", "click").append("SlotIndex", String.valueOf(i))
                );

            } else {
                // --- CAS : SLOT VIDE ---
                commands.set(iconPath + ".Visible", false);
                commands.set(qtyPath + ".Visible", false);
            }
        }

        // 3. Binding du bouton fermer
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose", EventData.of("Action", "close"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ChestEventData data) {
        if ("close".equals(data.action)) {
            this.close();
        } else if ("click".equals(data.action)) {
            System.out.println("Slot cliqué: " + data.slotIndex);
        }
    }

    public static class ChestEventData {
        public static final BuilderCodec<ChestEventData> CODEC = BuilderCodec.builder(ChestEventData.class, ChestEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action)
                .add()
                .append(new KeyedCodec<>("SlotIndex", Codec.STRING), (d, v) -> d.slotIndex = v, d -> d.slotIndex)
                .add()
                .build();

        public String action;
        public String slotIndex;
    }
}