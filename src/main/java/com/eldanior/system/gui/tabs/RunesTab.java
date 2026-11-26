package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.runes.RuneManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runes actives du menu systeme (2026-11-12, remplace /es runes), sur le modele de la fenetre des runes du jeu.
 *
 * Grilles de System/SystemPage.ui remplies ici (#Grille.Slots = ItemGridSlot[], rendu des cases du jeu, infobulle de l'objet) :
 * #RuneAbilityGrid = InventoryComponent.AbilitySlots (2 lignes de 3 : rune principale AbilityCoreSlotFilter + 2 soutiens
 * AbilitySupportSlotFilter), #RuneBagGrid = RuneBag (50 places). Une grille reliee par InventorySectionId reste vide dans une
 * page de mod (essai du 2026-11-12) ; IsAbilitiesSection y est refuse (« failed customUI »). Chaque case est envoyee avec
 * IsActivatable = true : a false (valeur par defaut d'ItemGridSlot) le client n'envoyait ni clic ni depot (essai du 2026-11-12).
 *
 * Gestes : clic sur une rune du sac = posee dans la 1re case libre qui l'accepte ; clic sur une case = rangee dans le sac ;
 * glisser-deposer entre les deux grilles, ou dans le sac pour le reordonner. Le client n'envoie pas l'etat de Maj : un clic
 * arrive comme un depot sur sa propre case (SlotIndex = SourceSlotId) suivi d'un SlotClicking, ignore (LAST_DROP). Regle : une rune ne quitte sa case qu'une fois posee ailleurs (addItemStackToSlot
 * applique les filtres du jeu ; les premieres versions retiraient d'abord et perdaient la rune refusee).
 * Les donnees des evenements de grille (cles du client : SlotIndex, DragSourceSlotId, DragItemStackId...) sont lues dans le
 * JSON brut que SystemScreen.handleDataEvent(String) recoit, et journalisees (« [Runes] evenement ») pour ajuster les cles.
 * (La sonde reseau InventoryOpenProbe ne voyait pas ces paquets : donnees nulles a l'essai du 2026-11-12.)
 */
public final class RunesTab {

    private RunesTab() {}

    /** Joueur -> [grille du dernier depot ("slot"/"bag"), heure] : le client envoie un clic juste apres chaque depot. */
    private static final Map<PlayerRef, Object[]> LAST_DROP = Collections.synchronizedMap(new WeakHashMap<>());

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        InventoryComponent.AbilitySlots slots = store.getComponent(ref, InventoryComponent.AbilitySlots.getComponentType());
        InventoryComponent.RuneBag bag = store.getComponent(ref, InventoryComponent.RuneBag.getComponentType());
        ItemContainer sc = slots != null ? slots.getInventory() : null, bc = bag != null ? bag.getInventory() : null;
        ui.set("#RuneAbilityGrid.Slots", grid(sc));
        ui.set("#RuneBagGrid.Slots", grid(bc));
        int equipped = count(sc), inBag = count(bc);
        ui.set("#SkillCount.Text", "RUNES ACTIVES  ·  " + equipped + " équipée" + (equipped > 1 ? "s" : "") + "  ·  " + inBag + " dans le sac");
        ui.set("#RuneEquippedCount.Text", equipped + " / " + (sc != null ? sc.getCapacity() : 0));
        ui.set("#RuneBagCount.Text", inBag == 0 ? "Vide : lisez un parchemin de rune pour en apprendre une"
                : inBag + " / " + (bc != null ? bc.getCapacity() : 0) + " places");
    }

    private static ItemGridSlot[] grid(ItemContainer c) {
        if (c == null) return new ItemGridSlot[0];
        ItemGridSlot[] out = new ItemGridSlot[c.getCapacity()];
        for (short s = 0; s < c.getCapacity(); s++) {
            ItemStack st = c.getItemStack(s);
            out[s] = ItemStack.isEmpty(st) ? new ItemGridSlot() : new ItemGridSlot(st);
            out[s].setActivatable(true);       // IsActivatable vaut false par defaut : sans lui le client n'envoie aucun clic
        }
        return out;
    }

    private static int count(ItemContainer c) {
        if (c == null) return 0;
        int n = 0;
        for (short s = 0; s < c.getCapacity(); s++) {
            ItemStack st = c.getItemStack(s);
            if (!ItemStack.isEmpty(st) && RuneManager.isRune(st)) n++;
        }
        return n;
    }

    // ===================================== gestes =====================================

    /** Traite rune_bag_click, rune_slot_click, rune_slot_drop, rune_bag_drop. */
    public static void handle(Ref<EntityStore> ref, Store<EntityStore> store, String action, String json) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        InventoryComponent.AbilitySlots slots = store.getComponent(ref, InventoryComponent.AbilitySlots.getComponentType());
        InventoryComponent.RuneBag bag = store.getComponent(ref, InventoryComponent.RuneBag.getComponentType());
        EldaniorLogger.info("[Runes] evenement " + action + " : " + json);
        if (pr == null || slots == null || bag == null || json == null) return;
        String grid = action.startsWith("rune_slot") ? "slot" : "bag";
        long now = System.currentTimeMillis();
        if (action.endsWith("_drop")) LAST_DROP.put(pr, new Object[]{grid, now});
        else {                                                              // clic emis au relachement d'un depot : ignore
            Object[] last = LAST_DROP.remove(pr);
            if (last != null && grid.equals(last[0]) && now - (Long) last[1] < 500) return;
        }
        int slot = findInt(json, "SlotId", "SlotIndex", "Slot", "TargetSlotIndex", "Index");
        int source = findInt(json, "DragSourceSlotId", "SourceSlotId", "SourceSlotIndex", "DragSlotIndex", "SourceSlot", "FromSlot");
        String item = findString(json, "DragItemStackId", "ItemStackId", "ItemId");
        ItemContainer bc = bag.getInventory(), sc = slots.getInventory();
        switch (action) {
            // clic simple (ou clic droit) sans depot juste avant : envoi rapide
            case "rune_bag_click" -> equip(pr, ref, store, slots, bag, slot, -1);
            case "rune_slot_click" -> unequip(pr, slots, bag, slot, -1);
            // Un clic sur une case arrive comme un depot sur la case de depart (SlotIndex = SourceSlotId, meme rune) :
            // envoi rapide. Sinon : glisser-deposer depuis l'autre grille, ou deplacement dans la meme grille.
            case "rune_slot_drop" -> {
                if (source == slot && holds(sc, slot, item)) unequip(pr, slots, bag, slot, -1);
                else if (holds(bc, source, item)) equip(pr, ref, store, slots, bag, source, slot);
                else if (holds(sc, source, item)) moveAbility(pr, slots, source, slot);
                else { int from = find(bc, item); if (from >= 0) equip(pr, ref, store, slots, bag, from, slot); }
            }
            case "rune_bag_drop" -> {
                if (source == slot && holds(bc, slot, item)) equip(pr, ref, store, slots, bag, slot, -1);
                else if (holds(sc, source, item)) unequip(pr, slots, bag, source, slot);
                else if (holds(bc, source, item)) swapInBag(bag, source, slot);
                else { int from = find(sc, item); if (from >= 0) unequip(pr, slots, bag, from, slot); }
            }
            default -> { }
        }
    }

    /** La case `s` de `c` porte-t-elle l'objet `itemId` (n'importe quel objet si l'id est inconnu) ? */
    private static boolean holds(ItemContainer c, int s, String itemId) {
        if (s < 0 || s >= c.getCapacity()) return false;
        ItemStack st = c.getItemStack((short) s);
        return !ItemStack.isEmpty(st) && (itemId == null || itemId.equals(st.getItemId()));
    }

    /** Reordonne le sac : echange les cases `from` et `to` (meme conteneur, rien ne peut se perdre). */
    private static void swapInBag(InventoryComponent.RuneBag bag, int from, int to) {
        ItemContainer bc = bag.getInventory();
        if (from == to || to < 0 || to >= bc.getCapacity()) return;
        ItemStack a = bc.getItemStack((short) from), b = bc.getItemStack((short) to);
        if (ItemStack.isEmpty(b)) bc.removeItemStackFromSlot((short) from);
        else bc.setItemStackForSlot((short) from, b);
        bc.setItemStackForSlot((short) to, a);
        bag.markDirty();
    }

    /** Deplace une rune equipee vers une autre case de competence libre (filtres du jeu appliques). */
    private static void moveAbility(PlayerRef pr, InventoryComponent.AbilitySlots slots, int from, int to) {
        ItemContainer sc = slots.getInventory();
        if (from == to || to < 0 || to >= sc.getCapacity()) return;
        if (!ItemStack.isEmpty(sc.getItemStack((short) to))) {
            pr.sendMessage(Message.raw("Cette case est déjà prise : rangez d'abord sa rune."));
            return;
        }
        ItemStack st = sc.getItemStack((short) from);
        var tx = sc.addItemStackToSlot((short) to, st);
        if (tx == null || !tx.succeeded() || !ItemStack.isEmpty(tx.getRemainder())) {
            if (tx != null && tx.succeeded()) sc.removeItemStackFromSlot((short) to);   // posee en partie : on annule
            pr.sendMessage(Message.raw("Cette case n'accepte pas cette rune (principale à gauche, soutiens à droite)."));
            return;
        }
        sc.removeItemStackFromSlot((short) from);
        slots.markDirty();
    }

    /** Pose la rune de la case `from` du sac : dans la case `target`, ou dans la 1re case libre qui l'accepte (target < 0). */
    private static void equip(PlayerRef pr, Ref<EntityStore> ref, Store<EntityStore> store, InventoryComponent.AbilitySlots slots,
                              InventoryComponent.RuneBag bag, int from, int target) {
        ItemContainer bc = bag.getInventory(), sc = slots.getInventory();
        if (from < 0 || from >= bc.getCapacity()) return;
        ItemStack st = bc.getItemStack((short) from);
        if (ItemStack.isEmpty(st) || !RuneManager.isRune(st)) return;               // deja deplacee (clic puis glisser)
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data != null && !RuneManager.canUseRune(st.getItemId(), data)) {
            pr.sendMessage(Message.raw("Votre classe ne peut pas utiliser cette rune."));
            return;
        }
        for (short s = 0; s < sc.getCapacity(); s++) {
            if (target >= 0 && s != target) continue;
            if (!ItemStack.isEmpty(sc.getItemStack(s))) {
                if (target >= 0) pr.sendMessage(Message.raw("Cette case est déjà prise : rangez d'abord sa rune."));
                continue;
            }
            var tx = sc.addItemStackToSlot(s, st);                                         // filtres de la case appliques
            if (tx == null || !tx.succeeded()) continue;
            ItemStack rest = tx.getRemainder();
            if (ItemStack.isEmpty(rest)) bc.removeItemStackFromSlot((short) from);       // posee : on la retire du sac
            else bc.setItemStackForSlot((short) from, rest);
            slots.markDirty();
            bag.markDirty();
            EldaniorLogger.info("[Runes] " + st.getItemId() + " : sac " + from + " -> competence " + s);
            return;
        }
        pr.sendMessage(Message.raw(target >= 0 ? "Cette case n'accepte pas cette rune (principale à gauche, soutiens à droite d'une "
                + "rune principale compatible)." : "Aucune case libre n'accepte cette rune (rune principale déjà équipée, ou pas de "
                + "rune principale compatible pour un soutien)."));
    }

    /** Range la rune de la case `from` dans le sac (case `target` du sac si libre, sinon la premiere place). */
    private static void unequip(PlayerRef pr, InventoryComponent.AbilitySlots slots, InventoryComponent.RuneBag bag, int from, int target) {
        ItemContainer bc = bag.getInventory(), sc = slots.getInventory();
        if (from < 0 || from >= sc.getCapacity()) return;
        ItemStack st = sc.getItemStack((short) from);
        if (ItemStack.isEmpty(st)) return;
        boolean toTarget = target >= 0 && target < bc.getCapacity() && ItemStack.isEmpty(bc.getItemStack((short) target));
        boolean ok;
        ItemStack rest;
        if (toTarget) {
            var tx = bc.addItemStackToSlot((short) target, st);
            ok = tx != null && tx.succeeded();
            rest = ok ? tx.getRemainder() : null;
        } else {
            var tx = bc.addItemStack(st);
            ok = tx != null && tx.succeeded();
            rest = ok ? tx.getRemainder() : null;
        }
        if (!ok) {
            pr.sendMessage(Message.raw("Le sac de runes est plein."));
            return;
        }
        if (ItemStack.isEmpty(rest)) sc.removeItemStackFromSlot((short) from);            // rangee : on vide la case
        else sc.setItemStackForSlot((short) from, rest);
        slots.markDirty();
        bag.markDirty();
        EldaniorLogger.info("[Runes] " + st.getItemId() + " : competence " + from + " -> sac");
    }

    private static int find(ItemContainer c, String itemId) {
        if (itemId == null) return -1;
        for (short s = 0; s < c.getCapacity(); s++) {
            ItemStack st = c.getItemStack(s);
            if (!ItemStack.isEmpty(st) && itemId.equals(st.getItemId())) return s;
        }
        return -1;
    }

    /** Premier entier trouve pour l'une des cles (casse ignoree) dans le JSON brut de l'evenement ; -1 sinon. */
    static int findInt(String json, String... keys) {
        for (String k : keys) {
            Matcher m = Pattern.compile("\"" + Pattern.quote(k) + "\"\\s*:\\s*\"?(-?\\d+)", Pattern.CASE_INSENSITIVE).matcher(json);
            if (m.find()) return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    static String findString(String json, String... keys) {
        for (String k : keys) {
            Matcher m = Pattern.compile("\"" + Pattern.quote(k) + "\"\\s*:\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(json);
            if (m.find()) return m.group(1);
        }
        return null;
    }
}
