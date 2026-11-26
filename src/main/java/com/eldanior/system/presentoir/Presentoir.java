package com.eldanior.system.presentoir;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Presentoir garni. Un presentoir vide n'existe que comme bloc : aucune donnee. Modifie uniquement dans le thread du monde.
 * Les objets gardent l'ordre de depot : la main vide reprend le dernier pose.
 */
final class Presentoir {

    enum Slot {
        /** ARME : tout objet qui n'est pas une armure (nom garde : c'est la cle des presentoirs deja sauvegardes). */
        ARME, TETE, TORSE, MAINS, JAMBES;

        /** Emplacement de l'objet, null s'il ne s'expose pas : il lui faut son propre modele, ce que n'ont pas les blocs. */
        static Slot of(Item item) {
            if (item == null || item.getModel() == null) return null;
            if (item.getArmor() != null) {
                String slot = item.getArmor().getArmorSlot().name().toLowerCase(Locale.ROOT);
                if (slot.contains("head")) return TETE;
                if (slot.contains("chest")) return TORSE;
                if (slot.contains("hand")) return MAINS;
                if (slot.contains("leg")) return JAMBES;
                return null;
            }
            return ARME;
        }
    }

    final String world;
    final int x, y, z;
    final Map<Slot, ItemStack> items = new LinkedHashMap<>();
    UUID owner;
    String ownerName;
    float yaw;
    Ref<EntityStore> entity;

    Presentoir(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    String key() { return key(world, x, y, z); }

    static String key(String world, int x, int y, int z) { return world + ":" + x + "," + y + "," + z; }

    /** Ce qui est montre : une entite d'une autre apparence est perimee. */
    String look() {
        StringBuilder s = new StringBuilder().append(yaw);
        items.forEach((slot, stack) -> s.append('|').append(slot).append('=').append(stack.getItemId()));
        return s.toString();
    }

    boolean hasArmor() {
        for (Slot s : items.keySet()) if (s != Slot.ARME) return true;
        return false;
    }

    /** Objet expose hors armure, s'il existe encore dans le jeu. */
    Item object() {
        ItemStack stack = items.get(Slot.ARME);
        return stack != null ? stack.getItem() : null;
    }

    boolean entityValid() { return entity != null && entity.isValid(); }
}
