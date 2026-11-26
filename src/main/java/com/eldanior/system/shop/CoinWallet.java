package com.eldanior.system.shop;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bourse en pieces du sac (sac, reserve, barre d'action) : valeur totale en cuivres, paiement avec monnaie rendue (les pieces
 * sont reprises puis la difference rendue dans les plus grosses pieces), affichage lisible. Utilisee par les boutiques des PNJ.
 */
public final class CoinWallet {

    private CoinWallet() {}

    /** Pieces, de la plus grosse a la plus petite : identifiant -> valeur en cuivres (CoinItemRegistry). */
    public static final Map<String, Long> COINS = new LinkedHashMap<>();
    private static final Map<String, String[]> NAMES = new LinkedHashMap<>();

    static {
        COINS.put("Elda_Imperial_Coins", 1_000_000L);                       // 2026-11-16
        COINS.put("Elda_Astral_Coins", 100_000L);
        COINS.put("Elda_Zenith_Coins", 10_000L);
        COINS.put("Elda_Diamond_Coins", 1_000L);
        COINS.put("Elda_Gold_Coins", 100L);
        COINS.put("Elda_Silver_Coins", 10L);
        COINS.put("Elda_Copper_Coins", 1L);
        NAMES.put("Elda_Imperial_Coins", new String[]{"impériale", "impériales"});
        NAMES.put("Elda_Astral_Coins", new String[]{"astrale", "astrales"});
        NAMES.put("Elda_Zenith_Coins", new String[]{"zénith", "zéniths"});
        NAMES.put("Elda_Diamond_Coins", new String[]{"diamant", "diamants"});
        NAMES.put("Elda_Gold_Coins", new String[]{"or", "or"});
        NAMES.put("Elda_Silver_Coins", new String[]{"argent", "argent"});
        NAMES.put("Elda_Copper_Coins", new String[]{"cuivre", "cuivres"});
    }

    private static ItemContainer bag(Player player) {
        return player.getInventory().getCombinedBackpackStorageHotbar();
    }

    /** Valeur des pieces portees, en cuivres. */
    public static long value(Player player) {
        ItemContainer bag = bag(player);
        long total = 0;
        for (short s = 0; s < bag.getCapacity(); s++) {
            ItemStack st = bag.getItemStack(s);
            if (st == null || st.isEmpty()) continue;
            Long v = COINS.get(st.getItemId());
            if (v != null) total += v * st.getQuantity();
        }
        return total;
    }

    /** Valeur en cuivres d'une liste (objet, quantite) de pieces ; 0 si l'une n'est pas une piece. */
    public static long valueOf(String itemId, int quantity) {
        Long v = COINS.get(itemId);
        return v == null ? 0 : v * quantity;
    }

    /**
     * Preleve amount cuivres dans les pieces portees, sans convertir le reste de la bourse : les plus petites pieces d'abord,
     * puis une plus grosse si besoin, dont la monnaie est rendue (en pieces plus petites). Les pieces restent la ou elles sont.
     * Renvoie la monnaie rendue (en cuivres), ou -1 (rien n'est touche) si la bourse ne suffit pas.
     */
    public static long pay(Player player, long amount) {
        if (amount <= 0) return 0;
        if (value(player) < amount) return -1;
        ItemContainer bag = bag(player);
        java.util.List<String> ascending = new java.util.ArrayList<>(COINS.keySet());
        java.util.Collections.reverse(ascending);
        long remaining = amount;
        for (String coin : ascending) {
            long worth = COINS.get(coin);
            for (short s = 0; s < bag.getCapacity() && remaining > 0; s++) {
                ItemStack st = bag.getItemStack(s);
                if (st == null || st.isEmpty() || !coin.equals(st.getItemId())) continue;
                long need = (remaining + worth - 1) / worth;                         // pieces de cette valeur a prendre
                int take = (int) Math.min(st.getQuantity(), need);
                if (take >= st.getQuantity()) bag.removeItemStackFromSlot(s);
                else bag.setItemStackForSlot(s, new ItemStack(coin, st.getQuantity() - take));
                remaining -= take * worth;
            }
            if (remaining <= 0) break;
        }
        long change = -remaining;
        receive(player, change);
        return change;
    }

    /** Donne value cuivres en pieces, des plus grosses aux plus petites (monnaie, rachat). */
    public static void receive(Player player, long value) {
        give(bag(player), value);
    }

    /**
     * Donne value cuivres en pieces et renvoie la valeur qui n'a pas pu etre rangee (sac plein), 0 si tout est passe.
     * Pour la banque : ce qui reste est recredite au compte au lieu d'etre perdu.
     */
    public static long receiveOrReturn(Player player, long value) {
        return give(bag(player), value);
    }

    /** Donne value cuivres dans les plus grosses pieces ; renvoie la valeur non rangee (sac plein). */
    private static long give(ItemContainer bag, long value) {
        long lost = 0;
        for (Map.Entry<String, Long> c : COINS.entrySet()) {
            long n = value / c.getValue();
            value -= n * c.getValue();
            while (n > 0) {
                int q = (int) Math.min(n, 999);
                var res = bag.addItemStack(new ItemStack(c.getKey(), q));
                ItemStack rest = res != null ? res.getRemainder() : null;
                if (res == null || !res.succeeded()) lost += (rest != null && !rest.isEmpty() ? rest.getQuantity() : q) * c.getValue();
                else if (rest != null && !rest.isEmpty()) lost += rest.getQuantity() * c.getValue();
                n -= q;
            }
        }
        return lost;
    }

    /** Nombre de pieces de ce type portees. */
    public static int count(Player player, String coin) {
        ItemContainer bag = bag(player);
        int n = 0;
        for (short s = 0; s < bag.getCapacity(); s++) {
            ItemStack st = bag.getItemStack(s);
            if (st != null && !st.isEmpty() && coin.equals(st.getItemId())) n += st.getQuantity();
        }
        return n;
    }

    /** Retire qty pieces de ce type (sans monnaie) ; false si le joueur en porte moins (rien n'est touche). */
    public static boolean takeCoins(Player player, String coin, int qty) {
        if (count(player, coin) < qty) return false;
        ItemContainer bag = bag(player);
        for (short s = 0; s < bag.getCapacity() && qty > 0; s++) {
            ItemStack st = bag.getItemStack(s);
            if (st == null || st.isEmpty() || !coin.equals(st.getItemId())) continue;
            int t = Math.min(qty, st.getQuantity());
            if (t >= st.getQuantity()) bag.removeItemStackFromSlot(s);
            else bag.setItemStackForSlot(s, new ItemStack(coin, st.getQuantity() - t));
            qty -= t;
        }
        return true;
    }

    /** Nom d'une piece : « or », « cuivres »... */
    public static String coinName(String coin, long n) {
        String[] name = NAMES.get(coin);
        return name == null ? coin : n > 1 ? name[1] : name[0];
    }

    /** « 13 zéniths 2 diamants » (deux plus grosses pieces non nulles). */
    public static String format(long value) {
        if (value <= 0) return "0 cuivre";
        StringBuilder sb = new StringBuilder();
        int parts = 0;
        for (Map.Entry<String, Long> c : COINS.entrySet()) {
            long n = value / c.getValue();
            if (n == 0) continue;
            value -= n * c.getValue();
            String[] name = NAMES.get(c.getKey());
            if (sb.length() > 0) sb.append(' ');
            sb.append(n).append(' ').append(n > 1 ? name[1] : name[0]);
            if (++parts == 2) break;
        }
        return sb.toString();
    }

    /** Plus grosse piece qui entre dans value (icone du prix). */
    public static String coinFor(long value) {
        for (Map.Entry<String, Long> c : COINS.entrySet()) if (value >= c.getValue()) return c.getKey();
        return "Elda_Copper_Coins";
    }
}
