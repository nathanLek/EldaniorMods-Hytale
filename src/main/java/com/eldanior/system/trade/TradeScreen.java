package com.eldanior.system.trade;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TradeScreen extends InteractiveCustomUIPage<TradeScreen.TradeEventData> {

    private final UUID myUUID;
    private final TradeSession session;

    public TradeScreen(@Nonnull PlayerRef playerRef, UUID myUUID, TradeSession session) {
        super(playerRef, CustomPageLifetime.CanDismiss, TradeEventData.CODEC);
        this.myUUID = myUUID;
        this.session = session;
        TradeManager.registerScreen(myUUID, this);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Trade/Trade.ui");

        // Noms des joueurs
        Player me = store.getComponent(ref, Player.getComponentType());
        String myName = me != null ? me.getPlayerRef().getUsername() : "Moi";
        UUID otherUUID = session.getOther(myUUID);
        PlayerRef otherRef = Universe.get().getPlayer(otherUUID);
        String otherName = otherRef != null ? otherRef.getUsername() : "Autre";

        ui.set("#MyName.Text", myName);
        ui.set("#OtherName.Text", otherName);

        // Remplir les slots
        refreshAllSlots(ui);

        // Remplir la hotbar
        if (me != null) {
            refreshHotbar(ui, me);
        }

        // Event bindings - mes slots (clic = reprendre l'item)
        for (int i = 0; i < TradeSession.SLOTS_PER_PLAYER; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#MySlot" + i,
                    EventData.of("Action", "take_my").append("Param", String.valueOf(i)));
        }

        // Event bindings - hotbar (clic = deposer dans l'echange)
        for (int i = 0; i < 9; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#THotbar" + i,
                    EventData.of("Action", "deposit").append("Param", String.valueOf(i)));
        }

        // Boutons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnValidate",
                EventData.of("Action", "validate"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCancel",
                EventData.of("Action", "cancel"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull TradeEventData data) {
        if (data.action == null || session.isCancelled()) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        switch (data.action) {
            case "deposit" -> {
                if (data.param == null) return;
                int hotbarSlot = Integer.parseInt(data.param);
                handleDeposit(player, hotbarSlot);
            }
            case "take_my" -> {
                if (data.param == null) return;
                int tradeSlot = Integer.parseInt(data.param);
                handleTakeBack(player, tradeSlot);
            }
            case "validate" -> handleValidate(player);
            case "cancel" -> handleCancel(player);
        }
    }

    private void handleDeposit(Player player, int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot >= 9) return;

        ItemContainer hotbar = player.getInventory().getHotbar();
        ItemStack item = hotbar.getItemStack((short) hotbarSlot);
        if (item == null || item.isEmpty()) return;

        int emptySlot = session.firstEmptySlot(myUUID);
        if (emptySlot == -1) return; // Slots pleins

        session.setItem(myUUID, emptySlot, item);
        hotbar.removeItemStackFromSlot((short) hotbarSlot);

        // Refresh les deux cotes
        UICommandBuilder update = new UICommandBuilder();
        refreshAllSlots(update);
        refreshHotbar(update, player);
        refreshValidation(update);
        this.sendUpdate(update);

        // Notifier l'autre joueur
        notifyOther();
    }

    private void handleTakeBack(Player player, int tradeSlot) {
        // Verifier qu'il y a de la place dans la hotbar avant de reprendre
        if (TradeManager.countEmptySlots(myUUID) <= 0) {
            player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§cVotre hotbar est pleine !"));
            return;
        }

        ItemStack item = session.takeItem(myUUID, tradeSlot);
        if (item == null || item.isEmpty()) return;

        player.getInventory().getHotbar().addItemStack(item);

        UICommandBuilder update = new UICommandBuilder();
        refreshAllSlots(update);
        refreshHotbar(update, player);
        refreshValidation(update);
        this.sendUpdate(update);

        notifyOther();
    }

    private void handleValidate(Player player) {
        boolean bothDone = session.validate(myUUID);

        if (bothDone) {
            // Les deux ont valide → executer l'echange (ferme aussi les fenetres)
            TradeManager.endTrade(session, true);

            player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§a§lEchange effectue avec succes !"));

            UUID otherUUID = session.getOther(myUUID);
            PlayerRef otherRef = Universe.get().getPlayer(otherUUID);
            if (otherRef != null) {
                otherRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("§a§lEchange effectue avec succes !"));
            }
        } else {
            UICommandBuilder update = new UICommandBuilder();
            refreshValidation(update);
            update.set("#TradeStatus.Text", "En attente de validation de l'autre joueur...");
            this.sendUpdate(update);

            notifyOther();
        }
    }

    private void handleCancel(Player player) {
        UUID otherUUID = session.getOther(myUUID);
        // endTrade ferme les fenetres des deux joueurs
        TradeManager.endTrade(session, false);

        player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§7Echange annule."));
        PlayerRef otherRef = Universe.get().getPlayer(otherUUID);
        if (otherRef != null) {
            otherRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("§c" + player.getPlayerRef().getUsername() + " a annule l'echange."));
        }
    }

    private void refreshAllSlots(UICommandBuilder ui) {
        ItemStack[] myItems = session.getMyItems(myUUID);
        ItemStack[] otherItems = session.getOtherItems(myUUID);

        for (int i = 0; i < TradeSession.SLOTS_PER_PLAYER; i++) {
            renderSlot(ui, "#MySlot" + i, myItems[i]);
            renderSlot(ui, "#OtherSlot" + i, otherItems[i]);
        }
    }

    private void refreshHotbar(UICommandBuilder ui, Player player) {
        ItemContainer hotbar = player.getInventory().getHotbar();
        for (int i = 0; i < 9; i++) {
            ItemStack item = hotbar.getItemStack((short) i);
            renderSlot(ui, "#THotbar" + i, item);
        }
    }

    private void refreshValidation(UICommandBuilder ui) {
        boolean myValid = session.isValidated(myUUID);
        UUID otherUUID = session.getOther(myUUID);
        boolean otherValid = session.isValidated(otherUUID);

        ui.set("#MyValidLabel.Text", myValid ? "Valide" : "Non valide");
        ui.set("#MyValidLabel.Style.TextColor", myValid ? "#4CAF50" : "#cc4444");
        ui.set("#OtherValidLabel.Text", otherValid ? "Valide" : "Non valide");
        ui.set("#OtherValidLabel.Style.TextColor", otherValid ? "#4CAF50" : "#cc4444");

        if (myValid && !otherValid) {
            ui.set("#TradeStatus.Text", "En attente de validation de l'autre joueur...");
        } else if (!myValid) {
            ui.set("#TradeStatus.Text", "Deposez vos items et validez l'echange.");
        }
    }

    private void renderSlot(UICommandBuilder ui, String slotId, ItemStack item) {
        boolean hasItem = item != null && !item.isEmpty();
        ui.set(slotId + " #Icon.Visible", hasItem);
        if (hasItem) {
            ui.set(slotId + " #Icon.ItemId", item.getItemId());
            ui.set(slotId + " #Qty.Text", item.getQuantity() > 1 ? String.valueOf(item.getQuantity()) : "");
        } else {
            ui.set(slotId + " #Qty.Text", "");
        }
    }

    private void notifyOther() {
        UUID otherUUID = session.getOther(myUUID);
        TradeScreen otherScreen = TradeManager.getScreen(otherUUID);
        if (otherScreen != null) {
            otherScreen.refreshFromOther();
        }
    }

    /**
     * Appele par l'autre joueur pour rafraichir notre vue des items et validations.
     */
    public void closeScreen() {
        this.close();
    }

    public void refreshFromOther() {
        UICommandBuilder update = new UICommandBuilder();
        refreshAllSlots(update);
        refreshValidation(update);
        this.sendUpdate(update);
    }

    public static class TradeEventData {
        public String action;
        public String param;

        public static final BuilderCodec<TradeEventData> CODEC =
                BuilderCodec.builder(TradeEventData.class, TradeEventData::new)
                        .addField(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action)
                        .addField(new KeyedCodec<>("Param", Codec.STRING), (d, v) -> d.param = v, d -> d.param)
                        .build();
    }
}