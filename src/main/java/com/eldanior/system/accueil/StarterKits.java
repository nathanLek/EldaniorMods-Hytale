package com.eldanior.system.accueil;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Kits de depart des 5 classes de base (2026-11-25) : donnes une seule fois, a la fin de l'accueil (Welcome.finish), jamais au
 * choix de classe de la statue. Objets vanilla de cuivre et de tissu/cuir ; les manuels des runes de classe sont remis a part
 * (RuneManager.grantClassRunes). Ce qui ne rentre pas dans l'inventaire tombe aux pieds du joueur.
 */
public final class StarterKits {

    private StarterKits() {}

    /** Un objet du kit, et son libelle pour la fenetre d'accueil. */
    public record Entry(String itemId, int qty, String label) {}

    /** Commun a toutes les classes : vivres, soins, lumiere, outils. */
    private static final List<Entry> COMMON = List.of(
            new Entry("Food_Bread", 8, "Pain"),
            new Entry("Bandage_Crude", 5, "Bandage rudimentaire"),
            new Entry("Potion_Health_Small", 3, "Petite potion de soin"),
            new Entry("Furniture_Crude_Torch", 10, "Torche rudimentaire"),
            new Entry("Tool_Pickaxe_Crude", 1, "Pioche rudimentaire"),
            new Entry("Tool_Hatchet_Crude", 1, "Hachette rudimentaire"));

    private static List<Entry> leather() {
        return List.of(new Entry("Armor_Leather_Light_Head", 1, "Capuche de cuir"),
                new Entry("Armor_Leather_Light_Chest", 1, "Veste de cuir"),
                new Entry("Armor_Leather_Light_Hands", 1, "Gants de cuir"),
                new Entry("Armor_Leather_Light_Legs", 1, "Jambières de cuir"));
    }

    /** Equipement propre a la classe (en premier dans la barre d'action). */
    public static List<Entry> classItems(String classId) {
        return switch (classId == null ? "" : classId) {
            case "warrior" -> List.of(
                    new Entry("Weapon_Sword_Copper", 1, "Épée de cuivre"),
                    new Entry("Weapon_Shield_Copper", 1, "Bouclier de cuivre"),
                    new Entry("Armor_Copper_Head", 1, "Casque de cuivre"),
                    new Entry("Armor_Copper_Chest", 1, "Plastron de cuivre"),
                    new Entry("Armor_Copper_Hands", 1, "Gantelets de cuivre"),
                    new Entry("Armor_Copper_Legs", 1, "Jambières de cuivre"));
            case "mage" -> List.of(
                    new Entry("Weapon_Staff_Copper", 1, "Bâton de cuivre"),
                    new Entry("Weapon_Spellbook_Grimoire_Brown", 1, "Grimoire"),
                    new Entry("Armor_Cloth_Linen_Head", 1, "Capuche de lin"),
                    new Entry("Armor_Cloth_Linen_Chest", 1, "Robe de lin"),
                    new Entry("Armor_Cloth_Linen_Hands", 1, "Gants de lin"),
                    new Entry("Armor_Cloth_Linen_Legs", 1, "Chausses de lin"));
            case "archer" -> concat(List.of(
                    new Entry("Weapon_Shortbow_Copper", 1, "Arc court de cuivre"),
                    new Entry("Weapon_Arrow_Crude", 100, "Flèches rudimentaires"),
                    new Entry("Weapon_Daggers_Crude", 1, "Dagues rudimentaires")), leather());
            case "assassin" -> concat(List.of(
                    new Entry("Weapon_Daggers_Copper", 1, "Dagues de cuivre"),
                    new Entry("Weapon_Sword_Crude", 1, "Épée rudimentaire")), leather());
            case "merchant" -> concat(List.of(
                    new Entry("Weapon_Spear_Copper", 1, "Lance de cuivre"),
                    new Entry("Food_Bread", 12, "Pain (provisions du marchand)")), leather());
            default -> List.of();
        };
    }

    /** Kit complet d'une classe : son equipement puis le commun. */
    public static List<Entry> kit(String classId) {
        return concat(classItems(classId), COMMON);
    }

    private static List<Entry> concat(List<Entry> a, List<Entry> b) {
        List<Entry> out = new ArrayList<>(a);
        out.addAll(b);
        return out;
    }

    /** Donne le kit (thread du monde du joueur). Un objet inconnu est saute (journal). */
    public static void give(Ref<EntityStore> ref, Store<EntityStore> store, String classId) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getInventory() == null) return;
        var bag = player.getInventory().getCombinedHotbarFirst();
        List<ItemStack> left = new ArrayList<>();
        for (Entry e : kit(classId)) {
            if (Item.getAssetMap().getAsset(e.itemId()) == null) {
                EldaniorLogger.warn("[Accueil] objet du kit introuvable : " + e.itemId());
                continue;
            }
            ItemStack st = new ItemStack(e.itemId(), e.qty());
            var tx = bag.addItemStack(st);
            ItemStack rest = tx != null && tx.succeeded() ? tx.getRemainder() : st;
            if (rest != null && !rest.isEmpty()) left.add(rest);
        }
        if (left.isEmpty()) return;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        HeadRotation head = store.getComponent(ref, HeadRotation.getComponentType());
        if (t != null && head != null)
            store.addEntities(ItemComponent.generateItemDrops(store, left, new org.joml.Vector3d(t.getPosition()).add(0, 1, 0),
                    head.getRotation()), AddReason.SPAWN);
    }
}
