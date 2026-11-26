package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.shop.CoinWallet;
import com.eldanior.system.skills.runes.spells.LuoKatanas;
import com.hypixel.hytale.builtin.adventure.shop.barter.BarterItemStack;
import com.hypixel.hytale.builtin.adventure.shop.barter.BarterShopAsset;
import com.hypixel.hytale.builtin.adventure.shop.barter.BarterShopState;
import com.hypixel.hytale.builtin.adventure.shop.barter.BarterTrade;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Boutique d'un PNJ (Npc/Shop.ui, scripts/npcs/gen_shop_ui.py). Refaite le 2026-11-18 comme la fenetre de l'intendant (habillage
 * de la fenetre Chapitres) : a droite les categories (icone + nom), la replique du marchand, 4 tuiles de chiffres et une grille de
 * cartes d'articles ; a gauche la fiche de l'article choisi, la quantite, le total et Acheter / Vendre. Donnees, stocks et
 * reassorts : ceux d'une BarterShop du jeu (BarterShopState) ; le prix (pieces de la BarterShop) est paye avec les pieces du sac
 * (CoinWallet : les plus petites d'abord, monnaie rendue). Onglet Rachat : le marchand rachete les objets du joueur au quart de
 * leur valeur.
 */
public class ShopPage extends InteractiveCustomUIPage<ShopPage.ShopEventData> {

    private static final int CELLS = 20, TABS = 11;
    /** Paires de pieces de la bourse et du prix, pieces d'une carte, lignes du panneau d'ingredients (scripts/npcs/gen_shop_ui.py). */
    private static final int WALLET = 7, PRICE = 6, CELL_COINS = 3, ING = 4, FACTS = 6;
    private static final String OK = "#3fbf6a", KO = "#cc3361", COIN = "#e8a93b", GREY = "#77879D", TAB_OFF = "#96a9be", TAB_ON = "#f2c45e";
    static final String[] CATEGORIES = {"Tout", "Lames", "Distance", "Boucliers", "Tenues", "Remèdes", "Mets et thés", "Outils",
            "Matériaux", "Divers", "Rachat"};
    private static final int SELL = 10;
    /** Le marchand rachete a ce prix (part de la valeur de l'objet). */
    private static final double BUYBACK = 0.25;

    private final String shopId, title, theme;
    private int tab, page, selected = -1;
    /** Quantite choisie (barre de quantite) : lots a acheter ou objets a vendre. */
    private int qty = 1;
    private String sellSelected;
    private String message = "";
    /** Niveau du joueur : les articles de plus de LEVEL_MARGIN niveaux au-dessus ne sont pas proposes. */
    private int playerLevel = 1, hidden;
    private static final int LEVEL_MARGIN = 20;

    /**
     * Ce que le marchand rachete, en plus de sa liste de rachat (echanges « pieces contre objet » de sa BarterShop) :
     * null = les categories qu'il vend ; ALL = tout (marchand ambulant).
     */
    public record Buys(String label, java.util.function.Predicate<String> accepts) {}
    public static final Buys ALL = new Buys("un peu de tout", id -> true);
    private final Buys buys;
    /** Tous les articles, quel que soit le niveau du joueur (forge legendaire : on montre ce qu'on peut un jour commander). */
    private boolean allLevels;

    public ShopPage allLevels() {
        this.allLevels = true;
        return this;
    }

    /**
     * Prix de rachat propre a ce marchand (cuivres, a partir de l'identifiant), a la place du bareme buyback(id) ; null : le
     * bareme. Maitre de guilde (2026-11-25) : butins de monstres payes le double.
     */
    private java.util.function.ToLongFunction<String> pricing;

    public ShopPage pricing(java.util.function.ToLongFunction<String> f) {
        this.pricing = f;
        return this;
    }

    /** Filtre des articles montres (et achetables), par identifiant d'objet : Sensei = manuels que le joueur peut apprendre. */
    private java.util.function.Predicate<String> only;

    public ShopPage only(java.util.function.Predicate<String> filter) {
        this.only = filter;
        return this;
    }

    /** Reserve de la famille, ouverte depuis la fenetre de l'intendant (categorie Achats) : tout le stock, pas de rachat. */
    public static ShopPage reserve(@Nonnull PlayerRef playerRef, String shopId) {
        ShopPage p = new ShopPage(playerRef, shopId, "Intendant — réserve de la famille", "#c9a24a", LegendarySmiths.BUYS_NOTHING).allLevels();
        p.reserve = true;
        return p;
    }

    /** Reserve de l'intendant : pas de taxe du commerce. */
    private boolean reserve;

    public ShopPage(@Nonnull PlayerRef playerRef, String shopId, String title, String theme) {
        this(playerRef, shopId, title, theme, null);
    }

    public ShopPage(@Nonnull PlayerRef playerRef, String shopId, String title, String theme, Buys buys) {
        super(playerRef, CustomPageLifetime.CanDismiss, ShopEventData.CODEC);
        this.shopId = shopId;
        this.title = title;
        this.theme = theme != null ? theme : "#c9a24a";
        this.buys = buys;
    }

    /** Categorie d'un objet, d'apres son identifiant (1 a 9). */
    static int category(String id) {
        if (id.startsWith("Weapon_Shield")) return 3;
        if (id.startsWith("Weapon_Shortbow") || id.startsWith("Weapon_Crossbow") || id.startsWith("Weapon_Bow") || id.startsWith("Weapon_Staff")
                || id.startsWith("Weapon_Wand") || id.startsWith("Weapon_Handgun") || id.contains("_Handgun_")) return 2;
        if (id.startsWith("Weapon_") || id.startsWith("Eldanior_Weapon_") || id.startsWith("Artefact_")) return 1;
        if (id.startsWith("Armor_")) return 4;
        if (id.startsWith("Potion_") || id.startsWith("Eld_Potion_") || id.startsWith("Luo_Onguent_") || id.startsWith("Bandage_")
                || id.startsWith("Consomable_") || id.startsWith("Elixir_")) return 5;
        if (id.startsWith("Food_") || id.startsWith("Luo_The_")) return 6;
        if (id.startsWith("Tool_")) return 7;
        if (id.startsWith("Ingredient_") || id.startsWith("Ore_") || id.startsWith("Plant_") || id.startsWith("Fish_") || id.startsWith("Butin_")
                || id.startsWith("Bar_")) return 8;
        return 9;
    }

    private static String qualityId(Item item) {
        try {
            ItemQuality q = ItemQuality.getAssetMap().getAsset(item.getQualityIndex());
            return q != null ? q.getId() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String qualityColor(Item item) {
        return switch (qualityId(item)) {
            case "Uncommon" -> "#5fc95f";
            case "Rare" -> "#3498db";
            case "Epic" -> "#9b59b6";
            case "Unique" -> "#e74c3c";
            case "Legendary" -> "#f1c40f";
            case "Divin" -> "#00ffff";
            default -> "#c9d4e0";
        };
    }

    private static String qualityName(Item item) {
        return switch (qualityId(item)) {
            case "Common" -> "Commun";
            case "Uncommon" -> "Peu commun";
            case "Rare" -> "Rare";
            case "Epic" -> "Épique";
            case "Unique" -> "Unique";
            case "Legendary" -> "Légendaire";
            case "Divin" -> "Divin";
            default -> "";
        };
    }

    /** Part en pieces du prix d'un echange, en cuivres. */
    private static long price(BarterTrade t) {
        long v = 0;
        for (BarterItemStack in : t.getInput()) v += CoinWallet.valueOf(in.getItemId(), in.getQuantity());
        return v;
    }

    /** Part en objets (troc) du prix d'un echange. */
    private static List<BarterItemStack> barter(BarterTrade t) {
        List<BarterItemStack> out = new ArrayList<>();
        for (BarterItemStack in : t.getInput()) if (!CoinWallet.COINS.containsKey(in.getItemId())) out.add(in);
        return out;
    }

    /** Echange ou le marchand donne des pieces contre des objets : c'est un rachat (onglet Rachat), pas un article. */
    private static boolean isBuyback(BarterTrade t) {
        return CoinWallet.COINS.containsKey(t.getOutput().getItemId()) && !barter(t).isEmpty();
    }

    private static int count(Player player, String id) {
        if (player == null) return 0;
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        int n = 0;
        for (short s = 0; s < bag.getCapacity(); s++) {
            ItemStack st = bag.getItemStack(s);
            if (st != null && !st.isEmpty() && id.equals(st.getItemId())) n += st.getQuantity();
        }
        return n;
    }

    private static void take(Player player, String id, int qty) {
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        for (short s = 0; s < bag.getCapacity() && qty > 0; s++) {
            ItemStack st = bag.getItemStack(s);
            if (st == null || st.isEmpty() || !id.equals(st.getItemId())) continue;
            int t = Math.min(qty, st.getQuantity());
            if (t >= st.getQuantity()) bag.removeItemStackFromSlot(s);
            else bag.setItemStackForSlot(s, new ItemStack(id, st.getQuantity() - t));
            qty -= t;
        }
    }

    /** Montant decompose en pieces, les plus grosses d'abord : (identifiant de la piece, nombre). */
    private static List<Map.Entry<String, Long>> split(long value) {
        List<Map.Entry<String, Long>> out = new ArrayList<>();
        for (Map.Entry<String, Long> c : CoinWallet.COINS.entrySet()) {
            long n = value / c.getValue();
            if (n > 0) {
                out.add(Map.entry(c.getKey(), n));
                value -= n * c.getValue();
            }
        }
        return out;
    }

    /** Pieces vraiment portees (sac, reserve, barre), les plus grosses d'abord. */
    private static List<Map.Entry<String, Long>> purse(Player player) {
        List<Map.Entry<String, Long>> out = new ArrayList<>();
        if (player == null) return out;
        for (String coin : CoinWallet.COINS.keySet()) {
            int n = CoinWallet.count(player, coin);
            if (n > 0) out.add(Map.entry(coin, (long) n));
        }
        return out;
    }

    /** Remplit une rangee de pieces (#<prefix>Icon<j> + #<prefix>N<j>) ; color null : couleur du .ui. */
    private static void showCoins(UICommandBuilder ui, String prefix, int slots, List<Map.Entry<String, Long>> coins, String color) {
        for (int j = 0; j < slots; j++) {
            boolean on = j < coins.size();
            ui.set("#" + prefix + "Icon" + j + ".Visible", on);
            ui.set("#" + prefix + "N" + j + ".Visible", on);
            if (!on) continue;
            ui.set("#" + prefix + "Icon" + j + ".ItemId", coins.get(j).getKey());
            ui.set("#" + prefix + "N" + j + ".Text", String.valueOf(coins.get(j).getValue()));
            if (color != null) ui.set("#" + prefix + "N" + j + ".Style.TextColor", color);
        }
    }

    /** Nombre de lots que le joueur peut payer (bourse et objets du troc), sans tenir compte du stock. */
    private static int affordable(BarterTrade t, Player player, long wallet) {
        long unit = price(t);
        long n = unit > 0 ? wallet / unit : Integer.MAX_VALUE;
        for (BarterItemStack in : barter(t)) n = Math.min(n, count(player, in.getItemId()) / Math.max(1, in.getQuantity()));
        return (int) Math.max(0, Math.min(n, Integer.MAX_VALUE));
    }

    /** Panneau de droite « Ingredients requis » pour lots lots : visible seulement si l'article demande des objets en plus des pieces. */
    private void showIngredients(UICommandBuilder ui, BarterTrade t, Player player, int lots) {
        List<BarterItemStack> need = t == null ? List.of() : barter(t);
        ui.set("#ShopIngPanel.Visible", !need.isEmpty());
        if (need.isEmpty()) return;
        lots = Math.max(1, lots);
        ui.set("#ShopIngCap.Text", lots > 1 ? "Ingrédients requis pour " + lots + " lots" : "Ingrédients requis");
        int missing = 0;
        for (int i = 0; i < need.size(); i++) {
            BarterItemStack in = need.get(i);
            int have = count(player, in.getItemId()), req = Math.max(1, in.getQuantity()) * lots;
            boolean ok = have >= req;
            if (!ok) missing++;
            if (i >= ING) continue;
            ui.set("#ShopIngIcon" + i + ".ItemId", in.getItemId());
            ui.set("#ShopIngName" + i + ".Text", itemName(in.getItemId()));
            ui.set("#ShopIngHave" + i + ".Text", ok ? "Dans votre sac : " + have : "Vous en avez " + have + ", il en manque " + (req - have));
            ui.set("#ShopIngHave" + i + ".Style.TextColor", ok ? OK : KO);
            ui.set("#ShopIngBar" + i + ".Background", ok ? OK : KO);
            ui.set("#ShopIngQty" + i + ".Text", "x" + req);
        }
        for (int i = 0; i < ING; i++) ui.set("#ShopIng" + i + ".Visible", i < need.size());
        ui.set("#ShopIngState.Text", (missing == 0 ? "Vous avez tous les ingrédients."
                : missing == 1 ? "Il vous manque 1 ingrédient." : "Il vous manque " + missing + " ingrédients.")
                + (need.size() > ING ? " (+" + (need.size() - ING) + " non affiché(s))" : ""));
        ui.set("#ShopIngState.Style.TextColor", missing == 0 ? OK : KO);
    }

    /** Prix lisible : « 3 or », « 2 x Laine », « 1 argent + 2 x Laine ».
     *  Toujours une chaine simple : un Message compose (raw + insert) dans un .Text fait planter le client
     *  (« No parameterless constructor defined for type 'System.String' ») et le deconnecte. */
    private String priceText(BarterTrade t) {
        long coins = price(t);
        StringBuilder s = new StringBuilder(coins > 0 ? CoinWallet.format(coins) : "");
        for (BarterItemStack in : barter(t)) {
            if (s.length() > 0) s.append(" + ");
            s.append(in.getQuantity()).append(" x ").append(itemName(in.getItemId()));
        }
        return s.toString();
    }

    /** Nom traduit d'un objet dans la langue du joueur (repli : anglais, puis identifiant). */
    private String itemName(String id) {
        Item it = Item.getAssetMap().getAsset(id);
        if (it == null) return id;
        String key = it.getTranslationKey();
        try {
            com.hypixel.hytale.server.core.modules.i18n.I18nModule i18n = com.hypixel.hytale.server.core.modules.i18n.I18nModule.get();
            String lang = playerRef.getLanguage();
            String name = lang != null ? i18n.getMessage(lang, key) : null;
            if (name == null || name.isEmpty() || name.equals(key)) name = i18n.getMessage("en-US", key);
            if (name != null && !name.isEmpty() && !name.equals(key)) return name;
        } catch (Exception ignored) { }
        return id.replace('_', ' ');
    }

    /** Prix de rachat d'un objet (cuivres), meme bareme que les boutiques (scripts/guards/gen_marchand_luo.py), au quart ; 0 : invendable. */
    static long buyback(String id) {
        Item item = Item.getAssetMap().getAsset(id);
        if (item == null || CoinWallet.COINS.containsKey(id) || !sellable(id, item)) return 0;
        int level = item.getItemLevel();
        if (level <= 0) level = switch (qualityId(item)) {
            case "Uncommon" -> 25; case "Rare" -> 80; case "Epic" -> 180; case "Unique" -> 350; case "Legendary" -> 500; default -> 10;
        };
        int cat = category(id);
        boolean gear = cat <= 4 || cat == 7;
        double value = 40 * Math.pow(level, 1.3) * (gear ? 1.0 : 0.08);
        return Math.max(1, Math.round(value * BUYBACK));
    }

    /** Objets que les marchands ne rachetent pas : techniques, divins, reliques et objets du Clan Luo, plaques, cles, postes. */
    private static boolean sellable(String id, Item item) {
        String q = qualityId(item);
        if (q.equals("Technical") || q.equals("Developer") || q.equals("Template") || q.equals("Divin")) return false;
        if (id.startsWith("Eldanior_Plaque_") || id.startsWith("Eldanior_Cle_") || id.equals(NpcPostBlocks.ITEM) || id.startsWith("Armor_Kimono_")) return false;
        return !LuoKatanas.isClanItem(id);
    }

    private record View(BarterShopAsset asset, BarterShopState state, Instant now, BarterTrade[] trades, int[] stock) {}

    private View view(Store<EntityStore> store) {
        BarterShopAsset asset = BarterShopAsset.getAssetMap().getAsset(shopId);
        if (asset == null) return null;
        BarterShopState state = BarterShopState.get();
        Instant now = BarterShopState.getShopGameTime(store);
        return new View(asset, state, now, state.getResolvedTrades(asset, now), state.getStockArray(asset, now));
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Npc/Shop.ui");
        for (int i = 0; i < TABS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ShopTab" + i, EventData.of("Action", "tab").append("Index", String.valueOf(i)), false);
        for (int i = 0; i < CELLS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ShopCell" + i, EventData.of("Action", "cell").append("Index", String.valueOf(i)), false);
        for (String[] b : new String[][]{{"#ShopPrev", "prev"}, {"#ShopNext", "next"}, {"#ShopBuy1", "buy"}, {"#ShopQMinus", "qminus"},
                {"#ShopQPlus", "qplus"}, {"#ShopQ10", "q10"}, {"#ShopQMax", "qmax"}, {"#ShopClose", "close"}})
            events.addEventBinding(CustomUIEventBindingType.Activating, b[0], EventData.of("Action", b[1]), false);
        for (int i = 0; i < TABS; i++) ui.set("#ShopTabLine" + i + ".Background", theme);
        for (int i = 0; i < CELLS; i++)                                               // cadre de la carte choisie : couleur du marchand
            for (String side : new String[]{"T", "B", "L", "R"}) ui.set("#ShopCellSel" + side + i + ".Background", theme);
        fill(ui, ref, store);
    }

    /** Icone d'un onglet : le premier article de la categorie (Tout : le premier article ; Rachat : une piece d'or). */
    private String tabIcon(View v, int cat) {
        if (cat == SELL) return "Elda_Gold_Coins";
        for (BarterTrade t : v.trades()) {
            if (t == null || isBuyback(t)) continue;
            String id = t.getOutput().getItemId();
            if (Item.getAssetMap().getAsset(id) == null || only != null && !only.test(id)) continue;
            if (cat == 0 || category(id) == cat) return id;
        }
        return "Elda_Gold_Coins";
    }

    /** Replique du marchand selon l'onglet. */
    private String quote(View v, int cat, int count) {
        if (reserve) return "La réserve de la maison. Tout ce qui sort d'ici est noté au livre de comptes.";
        if (cat == SELL) {
            String what = buysLabel(v);
            return "Montrez-moi ce que vous portez. Je rachète " + (what.isEmpty() ? "seulement ce qui est sur ma liste" : what) + ".";
        }
        if (count == 0) return hidden > 0 ? "Ce que j'ai là n'est pas encore pour vous. Revenez quand vous aurez plus d'expérience."
                : "Je n'ai plus rien de ce genre pour l'instant. Repassez après le réassort.";
        return switch (cat) {
            case 1 -> "Des lames bien trempées. Prenez-la en main avant de choisir.";
            case 2 -> "Arcs, arbalètes, bâtons : de quoi frapper avant d'être frappé.";
            case 3 -> "Un bon bouclier vaut mieux que deux potions.";
            case 4 -> "Des tenues solides, taillées pour la route.";
            case 5 -> "Remèdes et onguents. Mieux vaut en avoir sur soi avant d'en avoir besoin.";
            case 6 -> "De quoi tenir la route : le ventre plein, on se bat mieux.";
            case 7 -> "Des outils honnêtes, pour la mine, le bois et les champs.";
            case 8 -> "Matériaux de première qualité, au juste poids.";
            case 9 -> "Un peu de tout. Fouillez, vous trouverez peut-être votre bonheur.";
            default -> "Bienvenue ! Prenez votre temps, tout ce qui est exposé est à vendre.";
        };
    }

    private static void stat(UICommandBuilder ui, int i, String value, String label) {
        ui.set("#ShopStat" + i + ".Visible", value != null);
        if (value == null) return;
        ui.set("#ShopStatV" + i + ".Text", value);
        ui.set("#ShopStatL" + i + ".Text", label);
    }

    /** Onglets : Tout, les categories presentes, Rachat (index dans CATEGORIES). */
    private List<Integer> tabs(View v) {
        List<Integer> out = new ArrayList<>(List.of(0));
        for (int c = 1; c < SELL; c++) {
            for (BarterTrade t : v.trades()) {
                if (t != null && !isBuyback(t) && category(t.getOutput().getItemId()) == c) { out.add(c); break; }
            }
        }
        if (buys == null || !buys.label().isEmpty() || !buyList(v).isEmpty()) out.add(SELL);   // rien a racheter : pas d'onglet
        return out;
    }

    private int currentCategory(List<Integer> tabs) {
        return tabs.get(Math.max(0, Math.min(tab, tabs.size() - 1)));
    }

    /** Index des articles de l'onglet courant : objets connus, hors rachats, au niveau du joueur. */
    private List<Integer> items(View v, int cat) {
        List<Integer> out = new ArrayList<>();
        hidden = 0;
        for (int i = 0; i < v.trades().length; i++) {
            BarterTrade t = v.trades()[i];
            Item item = t != null ? Item.getAssetMap().getAsset(t.getOutput().getItemId()) : null;
            if (item == null || isBuyback(t)) continue;
            if (cat != 0 && category(t.getOutput().getItemId()) != cat) continue;
            if (only != null && !only.test(t.getOutput().getItemId())) continue;
            if (!allLevels && item.getItemLevel() > playerLevel + LEVEL_MARGIN) { hidden++; continue; }
            out.add(i);
        }
        return out;
    }

    /** Prix de rachat (cuivres par objet) de la liste du marchand : echanges « pieces contre objet » de sa BarterShop. */
    private static Map<String, Long> buyList(View v) {
        Map<String, Long> out = new LinkedHashMap<>();
        if (v == null) return out;
        for (BarterTrade t : v.trades()) {
            if (t == null || !isBuyback(t)) continue;
            long coins = CoinWallet.valueOf(t.getOutput().getItemId(), t.getOutput().getQuantity());
            for (BarterItemStack in : barter(t)) {
                if (Item.getAssetMap().getAsset(in.getItemId()) == null) continue;
                out.putIfAbsent(in.getItemId(), Math.max(1, coins / Math.max(1, in.getQuantity())));
            }
        }
        return out;
    }

    /** Categories que le marchand vend (hors Tout et Rachat) : ce qu'il rachete par defaut. */
    private static java.util.Set<Integer> soldCategories(View v) {
        java.util.Set<Integer> out = new java.util.HashSet<>();
        if (v != null) for (BarterTrade t : v.trades()) if (t != null && !isBuyback(t)) out.add(category(t.getOutput().getItemId()));
        return out;
    }

    private boolean accepts(View v, String id) {
        if (buys != null) return buys.accepts().test(id);
        return soldCategories(v).contains(category(id));
    }

    /** Ce que le marchand rachete, en clair : « lames, boucliers, tenues ». */
    private String buysLabel(View v) {
        if (buys != null) return buys.label();
        List<Integer> cats = new ArrayList<>(soldCategories(v));
        java.util.Collections.sort(cats);
        List<String> names = new ArrayList<>();
        for (int c : cats) names.add(CATEGORIES[c].toLowerCase());
        return String.join(", ", names);
    }

    /** Prix de rachat d'un objet chez ce marchand (sa liste d'abord, sinon le bareme) ; 0 : il ne le rachete pas. */
    private long sellPrice(View v, String id) {
        Long listed = buyList(v).get(id);
        if (listed != null) return listed;
        if (!accepts(v, id)) return 0;
        return pricing != null && buyback(id) > 0 ? pricing.applyAsLong(id) : buyback(id);
    }

    /**
     * Onglet Rachat : la liste de rachat du marchand (meme les objets que le joueur n'a pas), puis les objets portes qu'il
     * accepte (son metier). Identifiant -> quantite portee (sac, reserve, barre). Objets portes d'abord.
     */
    private Map<String, Integer> sellables(View v, Player player) {
        Map<String, Integer> owned = new LinkedHashMap<>();
        if (player != null) {
            ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
            for (short s = 0; s < bag.getCapacity(); s++) {
                ItemStack st = bag.getItemStack(s);
                if (st != null && !st.isEmpty()) owned.merge(st.getItemId(), st.getQuantity(), Integer::sum);
            }
        }
        Map<String, Integer> out = new LinkedHashMap<>();
        for (String id : buyList(v).keySet()) out.put(id, owned.getOrDefault(id, 0));
        for (Map.Entry<String, Integer> e : owned.entrySet())
            if (!out.containsKey(e.getKey()) && accepts(v, e.getKey()) && buyback(e.getKey()) > 0) out.put(e.getKey(), e.getValue());
        return out;
    }

    private List<String> sellOrder(View v, Player player) {
        Map<String, Integer> m = sellables(v, player);
        List<String> ids = new ArrayList<>(m.keySet());
        ids.sort((a, b) -> (m.get(a) > 0) != (m.get(b) > 0) ? (m.get(a) > 0 ? -1 : 1)
                : category(a) != category(b) ? category(a) - category(b) : a.compareTo(b));
        return ids;
    }

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        int cut = title.indexOf(" — ");
        ui.set("#ShopTitle.Text", cut > 0 ? title.substring(0, cut) : title);
        ui.set("#ShopSub.Text", cut > 0 ? title.substring(cut + 3) : "Boutique");
        View v = view(store);
        if (v == null) {
            clearDetail(ui, "Boutique indisponible");
            ui.set("#ShopMsg.Text", "Cette boutique n'existe pas (" + shopId + ").");
            return;
        }
        List<Integer> tabs = tabs(v);
        tab = Math.max(0, Math.min(tab, tabs.size() - 1));
        for (int i = 0; i < TABS; i++) {
            boolean shown = i < tabs.size();
            ui.set("#ShopTab" + i + ".Visible", shown);
            if (!shown) continue;
            boolean on = i == tab;
            ui.set("#ShopTabIcon" + i + ".ItemId", tabIcon(v, tabs.get(i)));
            ui.set("#ShopTabText" + i + ".Text", CATEGORIES[tabs.get(i)]);
            ui.set("#ShopTabText" + i + ".Style.TextColor", on ? TAB_ON : TAB_OFF);
            ui.set("#ShopTabGlow" + i + ".Visible", on);
            ui.set("#ShopTabLine" + i + ".Visible", on);
        }
        ui.set("#ShopGridTitle.Text", CATEGORIES[currentCategory(tabs)]);
        Player player = store.getComponent(ref, Player.getComponentType());
        var levelData = store.getComponent(ref, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
        playerLevel = levelData != null ? Math.max(1, levelData.getLevel()) : 1;
        long wallet = player != null ? CoinWallet.value(player) : 0;
        List<Map.Entry<String, Long>> held = purse(player);
        showCoins(ui, "ShopWallet", WALLET, held, null);
        ui.set("#ShopWalletEmpty.Visible", held.isEmpty());
        ui.set("#ShopAccount.Text", levelData != null && levelData.getMoney() > 0
                ? "·   Banque : " + CoinWallet.format(levelData.getMoney()) : "");
        ui.set("#ShopMsg.Text", message);
        if (currentCategory(tabs) == SELL) fillSell(ui, v, player);
        else fillBuy(ui, v, currentCategory(tabs), wallet, player);
    }

    private void pageControls(UICommandBuilder ui, int count, String noun) {
        int pages = Math.max(1, (count + CELLS - 1) / CELLS);
        page = Math.max(0, Math.min(page, pages - 1));
        ui.set("#ShopFooter.Text", count + " " + noun + (count > 1 ? "s" : ""));
        ui.set("#ShopPager.Visible", pages > 1);
        ui.set("#ShopPageLabel.Text", (page + 1) + " / " + pages);
    }

    /** Quantite choisie, ramenee entre 1 et max (au moins 1 pour l'affichage), barre de quantite et total. */
    private void quantity(UICommandBuilder ui, int max, long unit, boolean troc) {
        qty = Math.max(1, Math.min(qty, Math.max(1, max)));
        ui.set("#ShopQty.Text", String.valueOf(qty));
        List<Map.Entry<String, Long>> total = split(unit * qty);
        for (int j = 0; j < PRICE; j++) {                                              // cases de recompense (2026-11-19)
            boolean on = j < total.size();
            ui.set("#ShopTotalSlot" + j + ".Visible", on);
            if (!on) continue;
            ui.set("#ShopTotalIcon" + j + ".ItemId", total.get(j).getKey());
            ui.set("#ShopTotalQty" + j + ".Text", String.valueOf(total.get(j).getValue()));
        }
        ui.set("#ShopTotalText.Text", total.isEmpty() ? (troc ? "Troc" : max <= 0 && unit == 0 ? "" : "Gratuit") : "");
    }

    /** Carte d'article : icone, nom (couleur de qualite), liseret de qualite, prix en pieces, etiquette, cadre si choisie. */
    private void card(UICommandBuilder ui, int k, String id, String name, Item item, List<Map.Entry<String, Long>> pc, String coinColor,
                      String info, String tag, String tagColor, boolean chosen) {
        String q = item != null ? qualityColor(item) : "#c9d4e0";
        ui.set("#ShopCellIcon" + k + ".ItemId", id);
        ui.set("#ShopCellName" + k + ".Text", name);
        ui.set("#ShopCellName" + k + ".Style.TextColor", q);
        ui.set("#ShopCellBar" + k + ".Background", q);
        showCoins(ui, "ShopCell" + k + "C", CELL_COINS, pc.size() > CELL_COINS ? pc.subList(0, CELL_COINS) : pc, coinColor);
        ui.set("#ShopCellInfo" + k + ".Text", pc.size() > CELL_COINS ? "+" : info);
        ui.set("#ShopCellInfo" + k + ".Style.TextColor", coinColor);
        ui.set("#ShopCellTag" + k + ".Text", tag);
        ui.set("#ShopCellTag" + k + ".Style.TextColor", tagColor);
        ui.set("#ShopCellSel" + k + ".Visible", chosen);
    }

    private void fillBuy(UICommandBuilder ui, View v, int cat, long wallet, Player player) {
        ui.set("#ShopTotalCap.Text", "Total");
        List<Integer> items = items(v, cat);
        pageControls(ui, items.size(), "article");
        ui.set("#ShopQuote.Text", "« " + quote(v, cat, items.size()) + " »");
        if (selected < 0 || !items.contains(selected)) selected = items.isEmpty() ? -1 : items.get(Math.min(page * CELLS, items.size() - 1));
        int reach = 0;
        for (int idx : items) {
            BarterTrade t = v.trades()[idx];
            if ((idx < v.stock().length ? v.stock()[idx] : 0) > 0 && affordable(t, player, wallet) > 0) reach++;
        }
        for (int k = 0; k < CELLS; k++) {
            int n = page * CELLS + k;
            boolean shown = n < items.size();
            ui.set("#ShopCell" + k + ".Visible", shown);
            if (!shown) continue;
            int idx = items.get(n);
            BarterTrade t = v.trades()[idx];
            String id = t.getOutput().getItemId();
            Item item = Item.getAssetMap().getAsset(id);
            int stock = idx < v.stock().length ? v.stock()[idx] : 0, lot = t.getOutput().getQuantity();
            boolean troc = !barter(t).isEmpty();
            String color = stock <= 0 ? KO : affordable(t, player, wallet) > 0 ? COIN : "#8a6a6a";
            List<String> tag = new ArrayList<>();
            if (stock <= 0) tag.add("épuisé");
            else tag.add("stock " + stock);
            if (lot > 1) tag.add("lot de " + lot);
            if (troc) tag.add("+ ingrédients");
            if (item != null && item.getItemLevel() > 0) tag.add("niv. " + item.getItemLevel());
            card(ui, k, id, itemName(id), item, split(price(t)), color, troc && price(t) == 0 ? "troc" : price(t) == 0 ? "gratuit" : "",
                    String.join("  ·  ", tag), stock <= 0 ? KO : troc ? COIN : GREY, idx == selected);
        }
        ui.set("#ShopEmpty.Visible", items.isEmpty());
        ui.set("#ShopEmpty.Text", hidden > 0 ? hidden + " article(s) réservé(s) aux aventuriers plus expérimentés." : "Rien à vendre dans cette catégorie.");

        String refresh = "—";
        try {
            Instant next = v.state().getOrCreateShopState(v.asset(), v.now()).getNextRefreshTime();
            long h = next != null ? Math.max(0, Duration.between(v.now(), next).toHours()) : -1;
            refresh = h < 0 ? "—" : h < 1 ? "< 1 h" : h + " h";
        } catch (Exception ignored) { }
        stat(ui, 0, String.valueOf(items.size()), "Articles");
        stat(ui, 1, String.valueOf(reach), "À votre portée");
        stat(ui, 2, refresh, "Réassort dans");
        if (hidden > 0) stat(ui, 3, String.valueOf(hidden), "Au-dessus de votre niveau");
        else if (reserve) stat(ui, 3, "Maison", "Réserve, sans taxe");
        else stat(ui, 3, Math.round(com.eldanior.system.territory.ParcelEconomyManager.TRADE_TAX * 100) + " %", "Taxe du commerce (incluse)");

        if (selected < 0) {
            clearDetail(ui, hidden > 0 ? "Rien à votre niveau ici" : "Rien à vendre ici");
            showIngredients(ui, null, player, 1);
            quantity(ui, 0, 0, false);
            ui.set("#ShopBuy1.Text", "Acheter");
            return;
        }
        BarterTrade t = v.trades()[selected];
        String id = t.getOutput().getItemId();
        int stock = selected < v.stock().length ? v.stock()[selected] : 0;
        long unit = price(t);
        boolean troc = !barter(t).isEmpty();
        detail(ui, id, t.getOutput().getQuantity());
        List<Map.Entry<String, Long>> pc = split(unit);
        showCoins(ui, "ShopPriceC", PRICE, pc, wallet >= unit ? COIN : KO);
        ui.set("#ShopPriceText.Text", pc.isEmpty() ? (troc ? "Troc" : "Gratuit") : "");
        ui.set("#ShopPriceCap.Text", t.getOutput().getQuantity() > 1 ? "Prix du lot" : "Prix");
        int can = Math.min(affordable(t, player, wallet), stock);
        Item item = Item.getAssetMap().getAsset(id);
        String q = qualityName(item);
        List<String[]> facts = new ArrayList<>();
        facts.add(new String[]{"Catégorie", CATEGORIES[category(id)], null});
        facts.add(new String[]{"Qualité", q.isEmpty() ? "—" : q, qualityColor(item)});
        facts.add(new String[]{"Niveau", item.getItemLevel() > 0 ? String.valueOf(item.getItemLevel()) : "—",
                item.getItemLevel() > playerLevel ? KO : null});
        facts.add(new String[]{"Lot", t.getOutput().getQuantity() > 1 ? t.getOutput().getQuantity() + " objets" : "à l'unité", null});
        facts.add(new String[]{"Stock", stock > 0 ? String.valueOf(stock) : "épuisé", stock > 0 ? TAB_ON : KO});
        facts.add(new String[]{"Dans votre sac", String.valueOf(count(player, id)), null});
        facts(ui, facts);
        ui.set("#ShopActHead.Text", "Achat");
        quantity(ui, can, unit, troc);
        String lots = t.getOutput().getQuantity() > 1 ? (qty > 1 ? " lots" : " lot") : "";
        ui.set("#ShopBuy1.Text", "Acheter " + qty + lots);
        ui.set("#ShopHint.Text", stock <= 0 ? "Épuisé : revenez après le réassort"
                : can <= 0 && wallet < unit ? "Il vous manque " + CoinWallet.format(unit - wallet)
                : can <= 0 ? "Il vous manque des ingrédients"
                : "Vous pouvez en acheter " + can + (t.getOutput().getQuantity() > 1 ? " lot(s)" : ""));
        ui.set("#ShopHint.Style.TextColor", stock > 0 && can > 0 ? OK : KO);
        showIngredients(ui, t, player, qty);
    }

    private void fillSell(UICommandBuilder ui, View v, Player player) {
        ui.set("#ShopTotalCap.Text", "Vous recevrez");
        ui.set("#ShopQuote.Text", "« " + quote(v, SELL, 0) + " »");
        showIngredients(ui, null, player, 1);
        Map<String, Integer> have = sellables(v, player);
        List<String> list = sellOrder(v, player);
        pageControls(ui, list.size(), "objet");
        if (sellSelected == null || !list.contains(sellSelected)) sellSelected = list.isEmpty() ? null : list.get(Math.min(page * CELLS, list.size() - 1));
        int carried = 0;
        long worth = 0;
        for (String id : list) {
            int q = have.getOrDefault(id, 0);
            if (q > 0) { carried++; worth += sellPrice(v, id) * q; }
        }
        for (int k = 0; k < CELLS; k++) {
            int n = page * CELLS + k;
            boolean shown = n < list.size();
            ui.set("#ShopCell" + k + ".Visible", shown);
            if (!shown) continue;
            String id = list.get(n);
            int q = have.getOrDefault(id, 0);
            card(ui, k, id, itemName(id), Item.getAssetMap().getAsset(id), split(sellPrice(v, id)), "#00e7b6", "",
                    q > 0 ? "vous en avez " + q : "aucun dans votre sac", q > 0 ? "#d3d6db" : "#5a6676", id.equals(sellSelected));
        }
        ui.set("#ShopEmpty.Visible", list.isEmpty());
        ui.set("#ShopEmpty.Text", "Ce marchand ne rachète rien de ce que vous portez.");
        stat(ui, 0, String.valueOf(list.size()), "Objets rachetés");
        stat(ui, 1, String.valueOf(carried), "Dans votre sac");
        stat(ui, 2, worth > 0 ? CoinWallet.format(worth) : "—", "Si vous vendez tout");
        stat(ui, 3, "¼", "de la valeur, hors liste");
        if (sellSelected == null) {
            clearDetail(ui, "Rien à vendre");
            quantity(ui, 0, 0, false);
            ui.set("#ShopBuy1.Text", "Vendre");
            return;
        }
        long unit = sellPrice(v, sellSelected);
        int q = have.getOrDefault(sellSelected, 0);
        detail(ui, sellSelected, 1);
        showCoins(ui, "ShopPriceC", PRICE, split(unit), "#00e7b6");
        ui.set("#ShopPriceText.Text", "");
        ui.set("#ShopPriceCap.Text", "Rachat, l'unité");
        Item item = Item.getAssetMap().getAsset(sellSelected);
        String ql = qualityName(item);
        List<String[]> facts = new ArrayList<>();
        facts.add(new String[]{"Catégorie", CATEGORIES[category(sellSelected)], null});
        facts.add(new String[]{"Qualité", ql.isEmpty() ? "—" : ql, qualityColor(item)});
        facts.add(new String[]{"Vous en portez", q > 0 ? String.valueOf(q) : "aucun", q > 0 ? TAB_ON : KO});
        facts.add(new String[]{"Rachat, l'unité", CoinWallet.format(unit), "#00e7b6"});
        facts.add(new String[]{"Si vous vendez tout", q > 0 ? CoinWallet.format(unit * q) : "—", q > 0 ? "#00e7b6" : null});
        facts.add(new String[]{"Barème", buyList(v).containsKey(sellSelected) ? "sa liste" : "¼ de la valeur", null});
        facts(ui, facts);
        ui.set("#ShopActHead.Text", "Vente");
        quantity(ui, q, unit, false);
        ui.set("#ShopBuy1.Text", "Vendre " + qty);
        ui.set("#ShopHint.Text", q > 0 ? "Tout vendre : " + CoinWallet.format(unit * q) : "Vous n'en portez pas");
        ui.set("#ShopHint.Style.TextColor", q > 0 ? OK : KO);
    }

    /** Faits de la fiche (2026-11-19) : {cle, valeur, couleur ou null}, 6 au plus, par rangees de deux. */
    private static void facts(UICommandBuilder ui, List<String[]> facts) {
        for (int i = 0; i < FACTS; i++) {
            boolean shown = i < facts.size();
            ui.set("#ShopFact" + i + ".Visible", shown);
            if (!shown) continue;
            ui.set("#ShopFactK" + i + ".Text", facts.get(i)[0]);
            ui.set("#ShopFactV" + i + ".Text", facts.get(i)[1]);
            ui.set("#ShopFactV" + i + ".Style.TextColor", facts.get(i)[2] != null ? facts.get(i)[2] : "#e8d9b0");
        }
        for (int r = 0; r < FACTS / 2; r++) ui.set("#ShopFactRow" + r + ".Visible", facts.size() > r * 2);
    }

    private void detail(UICommandBuilder ui, String id, int lot) {
        Item item = Item.getAssetMap().getAsset(id);
        ui.set("#ShopName.Text", itemName(id));
        ui.set("#ShopName.Style.TextColor", qualityColor(item));
        String q = qualityName(item);
        ui.set("#ShopKind.Text", CATEGORIES[category(id)] + (q.isEmpty() ? "" : "  ·  " + q)
                + (item.getItemLevel() > 0 ? "  ·  niveau " + item.getItemLevel() : "") + (lot > 1 ? "  ·  lot de " + lot : ""));
        ui.set("#ShopBig.ItemId", id);
        try { ui.set("#ShopDesc.Text", item.getDescriptionTranslationMessage()); } catch (Exception e) { ui.set("#ShopDesc.Text", ""); }
        ui.set("#ShopBig.Visible", true);
        ui.set("#ShopBigMark.Visible", false);
    }

    private static void clearDetail(UICommandBuilder ui, String text) {
        ui.set("#ShopName.Text", text);
        ui.set("#ShopName.Style.TextColor", "#8a96a8");
        ui.set("#ShopKind.Text", "");
        ui.set("#ShopDesc.Text", "");
        showCoins(ui, "ShopPriceC", PRICE, List.of(), null);
        ui.set("#ShopPriceText.Text", "");
        facts(ui, List.of());
        ui.set("#ShopHint.Text", "");
        ui.set("#ShopBig.Visible", false);
        ui.set("#ShopBigMark.Visible", true);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ShopEventData data) {
        try {
            handle(ref, store, data);
        } catch (Throwable e) {                                                       // toujours repondre, et garder la trace
            EldaniorLogger.error("[Boutique] " + shopId + " action " + data.action + " " + data.index, e);
            try {
                UICommandBuilder ui = new UICommandBuilder();
                ui.set("#ShopMsg.Text", "Une erreur est survenue (voir le journal du serveur).");
                sendUpdate(ui);
            } catch (Throwable ignored) { }
        }
    }

    private void handle(Ref<EntityStore> ref, Store<EntityStore> store, ShopEventData data) {
        message = "";
        int index = 0;
        try { index = Integer.parseInt(data.index); } catch (Exception ignored) { }
        View v = view(store);
        if (v != null) {
            List<Integer> tabs = tabs(v);
            boolean selling = currentCategory(tabs) == SELL;
            switch (data.action == null ? "" : data.action) {
                case "close" -> { close(); return; }
                case "tab" -> { tab = Math.max(0, Math.min(index, tabs.size() - 1)); page = 0; selected = -1; sellSelected = null; qty = 1; }
                case "prev" -> page--;
                case "next" -> page++;
                case "cell" -> {
                    int n = page * CELLS + index;
                    if (selling) {
                        List<String> ids = sellOrder(v, store.getComponent(ref, Player.getComponentType()));
                        if (n >= 0 && n < ids.size() && !ids.get(n).equals(sellSelected)) { sellSelected = ids.get(n); qty = 1; }
                    } else {
                        List<Integer> items = items(v, currentCategory(tabs));
                        if (n >= 0 && n < items.size() && items.get(n) != selected) { selected = items.get(n); qty = 1; }
                    }
                }
                case "qminus" -> qty = Math.max(1, qty - 1);
                case "qplus" -> qty++;
                case "q10" -> qty += 10;
                case "qmax" -> qty = Integer.MAX_VALUE;   // ramene au maximum possible par fill
                case "buy" -> {
                    if (selling) sell(v, ref, store, qty); else buy(v, ref, store, qty);
                    qty = 1;
                }
                default -> { }
            }
        }
        UICommandBuilder ui = new UICommandBuilder();
        fill(ui, ref, store);
        sendUpdate(ui);
    }

    /** Achat de qty lots (-1 : tout le stock que la bourse permet). */
    private void buy(View v, Ref<EntityStore> ref, Store<EntityStore> store, int qty) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || selected < 0 || selected >= v.trades().length) return;
        if (only != null && v.trades()[selected] != null && !only.test(v.trades()[selected].getOutput().getItemId())) {
            message = "Cet article ne vous est pas destiné.";
            return;
        }
        Purchase r = purchase(v, selected, qty, player);
        message = r.message();
        if (r.ok() && r.paid() > 0 && !reserve) {
            var zone = tradeTax(ref, store, r.paid());
            if (zone != null) message += " Taxe du commerce : " + CoinWallet.format((long) Math.floor(r.paid()
                    * com.eldanior.system.territory.ParcelEconomyManager.TRADE_TAX)) + " pour " + zone.getName() + ".";
        }
    }

    /**
     * Taxe du commerce (2026-11-11) : 10 % du prix paye chez un PNJ marchand vont a la tresorerie du territoire ou se tient
     * l'acheteur (il est devant le marchand). Pas pour la reserve de l'intendant (la maison achete chez elle).
     */
    private static com.eldanior.system.territory.ParcelData tradeTax(Ref<EntityStore> ref, Store<EntityStore> store, long paid) {
        try {
            var t = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
            String world = ((EntityStore) store.getExternalData()).getWorld().getName();
            if (t == null) return null;
            var pos = t.getPosition();
            return com.eldanior.system.territory.ParcelEconomyManager.payTradeTax(world, pos.x, pos.y, pos.z, paid);
        } catch (Exception e) {
            EldaniorLogger.error("[Boutique] taxe du commerce", e);
            return null;
        }
    }

    /** Resultat d'un achat : ok, message a afficher, montant paye en cuivres (0 si rien n'a ete paye). */
    public record Purchase(boolean ok, String message, long paid) {
        public Purchase(boolean ok, String message) { this(ok, message, 0); }
    }

    /** Achat de qty lots de l'article index (-1 : tout le stock), paye en pieces du sac (et objets du troc). */
    private static Purchase purchase(View v, int index, int qty, Player player) {
        BarterTrade t = v.trades()[index];
        int stock = index < v.stock().length ? v.stock()[index] : 0;
        long unit = price(t);
        if (stock <= 0) return new Purchase(false, "Épuisé : revenez après le réassort.");
        int n = qty < 0 ? stock : Math.min(qty, stock);
        if (unit > 0) n = (int) Math.min(n, CoinWallet.value(player) / unit);
        for (BarterItemStack in : barter(t)) n = Math.min(n, count(player, in.getItemId()) / Math.max(1, in.getQuantity()));
        if (n <= 0) return new Purchase(false, barter(t).isEmpty() ? "Votre bourse ne suffit pas." : "Il vous manque des objets pour ce troc.");
        if (!v.state().executeTrade(v.asset(), index, n, v.now())) return new Purchase(false, "Cet article n'est plus disponible.");
        long change = CoinWallet.pay(player, unit * n);
        if (change < 0) return new Purchase(false, "Votre bourse ne suffit pas.");
        for (BarterItemStack in : barter(t)) take(player, in.getItemId(), in.getQuantity() * n);
        String id = t.getOutput().getItemId();
        Item item = Item.getAssetMap().getAsset(id);
        int left = t.getOutput().getQuantity() * n, max = Math.max(1, item != null ? item.getMaxStack() : 1);
        while (left > 0) {
            int q = Math.min(left, max);
            var res = player.getInventory().getCombinedBackpackStorageHotbar().addItemStack(new ItemStack(id, q));
            if (!res.succeeded()) {
                EldaniorLogger.warn("[Boutique] inventaire plein : " + left + " x " + id + " non ranges pour " + player.getPlayerRef().getUsername());
                return new Purchase(true, "Inventaire plein : une partie de l'achat n'a pas pu être rangée.", unit * n);
            }
            left -= q;
        }
        return new Purchase(true, "Acheté : " + n + (t.getOutput().getQuantity() > 1 ? " lot(s)" : "") + " pour " + CoinWallet.format(unit * n)
                + (change > 0 ? " (monnaie rendue : " + CoinWallet.format(change) + ")" : "") + ".", unit * n);
    }

    // ===== Boutique lue par une autre fenetre (reserve de l'intendant, IntendantPage) =====

    /** Article d'une boutique : indice de l'echange, objet, nom traduit, lot, stock, prix unitaire (cuivres) et lisible, qualite, niveau. */
    public record Article(int index, String itemId, String name, int lot, int stock, long unit, String price, String quality,
                          String qualityColor, int level, String category) {}

    /** Articles a vendre d'une boutique (sans les rachats), dans l'ordre de la boutique. */
    public static List<Article> articles(String shopId, PlayerRef pr, Store<EntityStore> store) {
        List<Article> out = new ArrayList<>();
        ShopPage names = new ShopPage(pr, shopId, "", null);
        View v = names.view(store);
        if (v == null) return out;
        for (int i = 0; i < v.trades().length; i++) {
            BarterTrade t = v.trades()[i];
            Item item = t != null ? Item.getAssetMap().getAsset(t.getOutput().getItemId()) : null;
            if (item == null || isBuyback(t)) continue;
            String id = t.getOutput().getItemId();
            out.add(new Article(i, id, names.itemName(id), t.getOutput().getQuantity(), i < v.stock().length ? v.stock()[i] : 0, price(t),
                    names.priceText(t), qualityName(item), qualityColor(item), Math.max(0, item.getItemLevel()), CATEGORIES[category(id)]));
        }
        return out;
    }

    /** Achat de qty lots de l'article index d'une boutique, paye en pieces du sac du joueur. */
    public static Purchase purchase(String shopId, int index, int qty, Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return new Purchase(false, "Joueur introuvable.");
        View v = new ShopPage(pr, shopId, "", null).view(store);
        if (v == null || index < 0 || index >= v.trades().length) return new Purchase(false, "Cet article n'est plus disponible.");
        return purchase(v, index, qty, player);
    }

    /**
     * Achat paye par un autre fonds que la bourse (coffres de la maison, intendant, 2026-11-18) : funds = montant disponible,
     * pay = retrait du montant final. Les objets d'un troc restent pris dans l'inventaire de l'acheteur.
     */
    public static Purchase purchaseWith(String shopId, int index, int qty, Ref<EntityStore> ref, Store<EntityStore> store,
                                        long funds, java.util.function.LongConsumer pay) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return new Purchase(false, "Joueur introuvable.");
        View v = new ShopPage(pr, shopId, "", null).view(store);
        if (v == null || index < 0 || index >= v.trades().length) return new Purchase(false, "Cet article n'est plus disponible.");
        BarterTrade t = v.trades()[index];
        int stock = index < v.stock().length ? v.stock()[index] : 0;
        long unit = price(t);
        if (stock <= 0) return new Purchase(false, "Épuisé : revenez après le réassort.");
        int n = Math.min(Math.max(1, qty), stock);
        if (unit > 0) n = (int) Math.min(n, funds / unit);
        for (BarterItemStack in : barter(t)) n = Math.min(n, count(player, in.getItemId()) / Math.max(1, in.getQuantity()));
        if (n <= 0) return new Purchase(false, barter(t).isEmpty() ? "Les fonds ne suffisent pas." : "Il vous manque des objets pour ce troc.");
        if (!v.state().executeTrade(v.asset(), index, n, v.now())) return new Purchase(false, "Cet article n'est plus disponible.");
        pay.accept(unit * n);
        for (BarterItemStack in : barter(t)) take(player, in.getItemId(), in.getQuantity() * n);
        String id = t.getOutput().getItemId();
        Item item = Item.getAssetMap().getAsset(id);
        int left = t.getOutput().getQuantity() * n, max = Math.max(1, item != null ? item.getMaxStack() : 1);
        while (left > 0) {
            int q = Math.min(left, max);
            if (!player.getInventory().getCombinedBackpackStorageHotbar().addItemStack(new ItemStack(id, q)).succeeded())
                return new Purchase(true, "Inventaire plein : une partie de l'achat n'a pas pu être rangée.", unit * n);
            left -= q;
        }
        return new Purchase(true, "Acheté : " + n + (t.getOutput().getQuantity() > 1 ? " lot(s)" : "") + " pour " + CoinWallet.format(unit * n) + ".", unit * n);
    }

    /** Vente de qty objets au marchand (-1 : tous). */
    private void sell(View v, Ref<EntityStore> ref, Store<EntityStore> store, int qty) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || sellSelected == null) return;
        long unit = sellPrice(v, sellSelected);
        if (unit <= 0) { message = "Le marchand ne rachète pas cet objet."; return; }
        ItemContainer bag = player.getInventory().getCombinedBackpackStorageHotbar();
        int want = qty < 0 ? Integer.MAX_VALUE : qty, sold = 0;
        for (short s = 0; s < bag.getCapacity() && sold < want; s++) {
            ItemStack st = bag.getItemStack(s);
            if (st == null || st.isEmpty() || !sellSelected.equals(st.getItemId())) continue;
            int take = Math.min(st.getQuantity(), want - sold);
            if (take >= st.getQuantity()) bag.removeItemStackFromSlot(s);
            else bag.setItemStackForSlot(s, new ItemStack(st.getItemId(), st.getQuantity() - take));
            sold += take;
        }
        if (sold <= 0) { message = "Vous n'en avez pas."; return; }
        CoinWallet.receive(player, unit * sold);
        message = "Vendu : " + sold + " pour " + CoinWallet.format(unit * sold) + ".";
    }

    public static class ShopEventData {
        public static final BuilderCodec<ShopEventData> CODEC = BuilderCodec.builder(ShopEventData.class, ShopEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .build();
        public String action, index;
    }
}
