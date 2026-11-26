package com.eldanior.system.poche;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Panneau « Poche dimensionnelle » du menu Systeme (#TabPoche de System/SystemPage.ui, 2026-11-16) : il remplace le panneau
 * de l'onglet ouvert quand on clique la case de la poche (#PocheSlot, en haut a droite du personnage, sur la ligne du Niveau), sans quitter le menu.
 * Deux grilles, faites comme celles des runes (RunesTab : ItemGridSlot activables, evenements SlotClicking / Dropped lus dans
 * le JSON brut) : #PocheGrid (la poche, 4 x 9) et #PocheInvGrid (reserve, barre d'action puis sac a dos du joueur).
 * Clic = l'objet passe dans l'autre grille ; glisser-deposer = a la case visee (echange si elle est prise).
 */
public final class PocheTab {

    private PocheTab() {}

    /** Joueur -> [grille du dernier depot, heure] : le client envoie un clic juste apres chaque depot (meme regle que RunesTab). */
    private static final Map<PlayerRef, Object[]> LAST_DROP = Collections.synchronizedMap(new WeakHashMap<>());

    private static ItemContainer inventory(Player player) {
        return player.getInventory().getCombinedStorageHotbarBackpack();
    }

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());
        ItemContainer poche = pr != null ? PocheDimensionnelle.container(pr.getUuid()) : null;
        boolean has = poche != null && player != null;
        ui.set("#PocheGrids.Visible", has);
        ui.set("#PocheInfo.Text", has
                ? "Cliquez un objet pour le faire passer de la poche à votre inventaire (ou l'inverse), ou faites-le glisser. "
                  + "Ce qui est rangé ici ne se perd jamais, même à la mort."
                : "Vous n'avez pas de poche dimensionnelle. Achetez-en une chez un Maroquinier (10 pièces impériales), puis faites "
                  + "clic droit avec elle en main : elle s'équipera dans cette case.");
        if (!has) return;
        ui.set("#PocheGrid.Slots", grid(poche));
        ui.set("#PocheInvGrid.Slots", grid(inventory(player)));
        int used = 0;
        for (short s = 0; s < poche.getCapacity(); s++) if (!ItemStack.isEmpty(poche.getItemStack(s))) used++;
        ui.set("#PocheCount.Text", used + " / " + poche.getCapacity() + " cases");
    }

    /** Icone de la case du menu : la poche si elle est equipee, sinon case vide. */
    public static void slot(UICommandBuilder ui, PlayerRef pr) {
        boolean has = pr != null && PocheDimensionnelle.isEquipped(pr.getUuid());
        ui.set("#PocheSlotIcon.Visible", has);
        if (has) ui.set("#PocheSlotIcon.ItemId", PocheDimensionnelle.ITEM_ID);
    }

    private static ItemGridSlot[] grid(ItemContainer c) {
        ItemGridSlot[] out = new ItemGridSlot[c.getCapacity()];
        for (short s = 0; s < c.getCapacity(); s++) {
            ItemStack st = c.getItemStack(s);
            out[s] = ItemStack.isEmpty(st) ? new ItemGridSlot() : new ItemGridSlot(st);
            out[s].setActivatable(true);                   // sans lui le client n'envoie aucun clic (RunesTab)
        }
        return out;
    }

    /** poche_click, poche_inv_click, poche_drop, poche_inv_drop. */
    public static void handle(Ref<EntityStore> ref, Store<EntityStore> store, String action, String json) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());
        ItemContainer poche = pr != null ? PocheDimensionnelle.container(pr.getUuid()) : null;
        if (poche == null || player == null || json == null) return;
        ItemContainer inv = inventory(player);
        boolean inPoche = action.equals("poche_click") || action.equals("poche_drop");
        long now = System.currentTimeMillis();
        if (action.endsWith("_drop")) LAST_DROP.put(pr, new Object[]{inPoche, now});
        else {
            Object[] last = LAST_DROP.remove(pr);
            if (last != null && last[0].equals(inPoche) && now - (Long) last[1] < 500) return;
        }
        int slot = findInt(json, "SlotId", "SlotIndex", "Slot", "TargetSlotIndex", "Index");
        int source = findInt(json, "DragSourceSlotId", "SourceSlotId", "SourceSlotIndex", "DragSlotIndex", "SourceSlot", "FromSlot");
        String item = findString(json, "DragItemStackId", "ItemStackId", "ItemId");
        ItemContainer here = inPoche ? poche : inv, other = inPoche ? inv : poche;
        if (action.endsWith("_click")) { move(pr, here, slot, other, -1); return; }
        if (source == slot && holds(here, slot, item)) move(pr, here, slot, other, -1);       // clic arrive comme un depot sur place
        else if (holds(other, source, item)) move(pr, other, source, here, slot);             // depuis l'autre grille
        else if (holds(here, source, item)) move(pr, here, source, here, slot);               // dans la meme grille
        else EldaniorLogger.info("[Poche] evenement non reconnu " + action + " : " + json);
    }

    private static boolean holds(ItemContainer c, int s, String itemId) {
        if (s < 0 || s >= c.getCapacity()) return false;
        ItemStack st = c.getItemStack((short) s);
        return !ItemStack.isEmpty(st) && (itemId == null || itemId.equals(st.getItemId()));
    }

    /**
     * Deplace la pile de from[fs] vers to (case ts, ou la premiere qui l'accepte si ts < 0). Case visee prise par un autre
     * objet : echange. Rien n'est retire avant d'etre pose ailleurs.
     */
    private static void move(PlayerRef pr, ItemContainer from, int fs, ItemContainer to, int ts) {
        if (fs < 0 || fs >= from.getCapacity() || (from == to && fs == ts)) return;
        ItemStack st = from.getItemStack((short) fs);
        if (ItemStack.isEmpty(st)) return;
        if (PocheDimensionnelle.ITEM_ID.equals(st.getItemId()) && to != from && from != null && isPoche(to)) {
            pr.sendMessage(Message.raw("Une poche dimensionnelle ne se range pas dans une autre."));
            return;
        }
        if (ts >= 0 && ts < to.getCapacity()) {
            ItemStack target = to.getItemStack((short) ts);
            if (!ItemStack.isEmpty(target) && !target.getItemId().equals(st.getItemId())) {       // echange des deux cases
                if (PocheDimensionnelle.ITEM_ID.equals(target.getItemId()) && isPoche(from)) return;
                to.setItemStackForSlot((short) ts, st);
                from.setItemStackForSlot((short) fs, target);
                return;
            }
            var tx = to.addItemStackToSlot((short) ts, st);
            if (tx != null && tx.succeeded()) { rest(from, fs, tx.getRemainder()); return; }
        }
        var tx = to.addItemStack(st);
        if (tx == null || !tx.succeeded()) {
            pr.sendMessage(Message.raw(isPoche(to) ? "La poche dimensionnelle est pleine." : "Votre inventaire est plein."));
            return;
        }
        rest(from, fs, tx.getRemainder());
    }

    private static boolean isPoche(ItemContainer c) {
        return c instanceof com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer && c.getCapacity() == PocheDimensionnelle.CAPACITY;
    }

    private static void rest(ItemContainer from, int fs, ItemStack rest) {
        if (ItemStack.isEmpty(rest)) from.removeItemStackFromSlot((short) fs);
        else from.setItemStackForSlot((short) fs, rest);
    }

    private static int findInt(String json, String... keys) {
        for (String k : keys) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(k) + "\"\\s*:\\s*\"?(-?\\d+)",
                    java.util.regex.Pattern.CASE_INSENSITIVE).matcher(json);
            if (m.find()) return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    private static String findString(String json, String... keys) {
        for (String k : keys) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(k) + "\"\\s*:\\s*\"([^\"]+)\"",
                    java.util.regex.Pattern.CASE_INSENSITIVE).matcher(json);
            if (m.find()) return m.group(1);
        }
        return null;
    }
}
