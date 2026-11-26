package com.eldanior.system.economy;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.shop.CoinWallet;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Editeur des prix en jeu (/es prix, admin) : Economie/Prix.ui (scripts/economie/gen_prix_ui.py), bareme PriceBook.
 * Au centre : familles, recherche, liste des objets avec leur prix ; a droite : l'objet choisi, sa recette (prix de chaque
 * ingredient, cliquable), les recettes qui l'utilisent, la saisie du nouveau prix. Un prix enregistre ici va dans
 * prix_jeu.json (dossier du mod) et recalcule aussitot tout ce qui en depend.
 */
public class PriceEditorPage extends InteractiveCustomUIPage<PriceEditorPage.Data> {

    private static final int TABS = 21, ROWS = 12, ING = 5, ROW_COINS = 3, PRICE = 6;
    private static final String OK = "#3fbf6a", KO = "#cc3361", GOLD = "#e8a93b", MUTED = "#878e9c", SLOT = "#1b2836";
    /** Onglets fixes avant les familles du bareme. */
    private static final int TAB_ALL = 0, TAB_GAME = 1, FIRST_FAMILY = 2;

    private int tab = TAB_ALL, page;
    private String search, selected, message = "";
    private boolean messageOk = true;
    /** Lignes affichees (ingredients cliquables) : identifiant ouvert par chaque ligne. */
    private final String[] ingTargets = new String[ING];
    private final Map<String, String> names = new HashMap<>();
    /** Derniere liste calculee (onglet, recherche) : evite de retrier 5 000 noms a chaque clic. */
    private String cachedKey;
    private List<String> cachedList = List.of();

    public PriceEditorPage(@Nonnull PlayerRef playerRef, String search) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.search = search == null || search.isBlank() ? null : search.trim();
    }

    // ===== Affichage =====

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Economie/Prix.ui");
        for (int i = 0; i < TABS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PxFam" + i, EventData.of("Action", "tab").append("Index", String.valueOf(i)), false);
        for (int i = 0; i < ROWS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PxRow" + i, EventData.of("Action", "row").append("Index", String.valueOf(i)), false);
        for (int i = 0; i < ING; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PxIng" + i, EventData.of("Action", "ing").append("Index", String.valueOf(i)), false);
        for (String[] b : new String[][]{{"#PxPrev", "prev"}, {"#PxNext", "next"}, {"#PxClear", "clear"}, {"#PxReset", "reset"},
                {"#PxDiv2", "div2"}, {"#PxM10", "m10"}, {"#PxP10", "p10"}, {"#PxMul2", "mul2"}})
            events.addEventBinding(CustomUIEventBindingType.Activating, b[0], EventData.of("Action", b[1]), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PxSearchBtn", EventData.of("Action", "search").append("@Search", "#PxSearch.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PxSave", EventData.of("Action", "save").append("@Value", "#PxValue.Value"), false);
        if (search != null) ui.set("#PxSearch.Value", search);
        fill(ui, true);
    }

    private List<String> tabLabels() {
        List<String> out = new ArrayList<>(List.of("Tout", "Modifiés en jeu"));
        out.addAll(PriceBook.families());
        return out;
    }

    /** Objets de la liste : recherche (nom ou identifiant, sans accents) sur tout le bareme, sinon l'onglet choisi ; tries par nom. */
    private List<String> list() {
        String key = tab + "|" + search + "|" + (tab == TAB_GAME ? PriceBook.gameIds() : "");
        if (key.equals(cachedKey)) return cachedList;
        List<String> ids;
        if (search != null) {
            String q = fold(search);
            ids = new ArrayList<>();
            for (String id : PriceBook.allIds()) if (fold(id).contains(q) || fold(name(id)).contains(q)) ids.add(id);
        } else if (tab == TAB_GAME) {
            ids = PriceBook.gameIds();
        } else if (tab >= FIRST_FAMILY) {
            ids = PriceBook.ids(tab - FIRST_FAMILY);
        } else {
            ids = PriceBook.allIds();
        }
        Map<String, String> sortKey = new HashMap<>();
        for (String id : ids) sortKey.put(id, fold(name(id)));
        ids.sort(Comparator.comparing((String id) -> sortKey.get(id)).thenComparing(id -> id));
        cachedKey = key;
        cachedList = ids;
        return ids;
    }

    private void fill(UICommandBuilder ui, boolean updateValue) {
        List<String> labels = tabLabels();
        for (int i = 0; i < TABS; i++) {
            boolean shown = i < labels.size();
            ui.set("#PxFam" + i + ".Visible", shown);
            if (!shown) continue;
            String label = labels.get(i);
            if (i == TAB_GAME) label += " (" + PriceBook.gameIds().size() + ")";
            ui.set("#PxFamText" + i + ".Text", label);
            ui.set("#PxFamText" + i + ".Style.TextColor", search == null && i == tab ? "#ffffff" : "#96a9be");
            ui.set("#PxFamLine" + i + ".Visible", search == null && i == tab);
        }
        ui.set("#PxSub.Text", search != null ? "Recherche : " + search : tab < labels.size() ? labels.get(tab) : "");
        List<String> ids = list();
        int pages = Math.max(1, (ids.size() + ROWS - 1) / ROWS);
        page = Math.max(0, Math.min(page, pages - 1));
        ui.set("#PxCount.Text", ids.size() + " objet(s)");
        ui.set("#PxPage.Text", "Page " + (page + 1) + " / " + pages);
        ui.set("#PxPrev.Visible", pages > 1);
        ui.set("#PxNext.Visible", pages > 1);
        if (selected == null && !ids.isEmpty()) selected = ids.get(page * ROWS < ids.size() ? page * ROWS : 0);
        for (int k = 0; k < ROWS; k++) {
            int n = page * ROWS + k;
            boolean shown = n < ids.size();
            ui.set("#PxRow" + k + ".Visible", shown);
            if (!shown) continue;
            String id = ids.get(n);
            PriceBook.Price p = PriceBook.price(id);
            ui.set("#PxRowIcon" + k + ".ItemId", id);
            ui.set("#PxRowName" + k + ".Text", name(id));
            ui.set("#PxRowName" + k + ".Style.TextColor", qualityColor(id));
            ui.set("#PxRowId" + k + ".Text", id);
            ui.set("#PxRowQ" + k + ".Background", id.equals(selected) ? GOLD : SLOT);
            List<Map.Entry<String, Long>> pc = p.value() != null ? split(p.value()) : List.of();
            showCoins(ui, "PxRow" + k + "C", ROW_COINS, pc.size() > ROW_COINS ? pc.subList(0, ROW_COINS) : pc, null);
            ui.set("#PxRowText" + k + ".Text", p.value() == null ? "—" : p.value() == 0 ? "gratuit" : pc.size() > ROW_COINS ? "+" : "");
            ui.set("#PxRowSrc" + k + ".Text", p.game() ? "jeu" : p.source().label);
            ui.set("#PxRowSrc" + k + ".Style.TextColor", sourceColor(p));
        }
        int modified = PriceBook.gameIds().size();
        ui.set("#PxStats.Text", PriceBook.allIds().size() + " objets  ·  " + modified + " prix modifié(s) en jeu (fichier economie/prix_jeu.json du mod)"
                + "  ·  docs/economie.md : relancer scripts/economie/gen_economie.py");
        fillSide(ui, updateValue);
    }

    private void fillSide(UICommandBuilder ui, boolean updateValue) {
        ui.set("#PxMsg.Text", message);
        ui.set("#PxMsg.Style.TextColor", messageOk ? "#00e7b6" : KO);
        if (selected == null || PriceBook.entry(selected) == null) {
            ui.set("#PxName.Text", PriceBook.loaded() ? "Choisissez un objet" : "Barème introuvable (eldanior/economie.json)");
            ui.set("#PxId.Text", "");
            ui.set("#PxKind.Text", "");
            ui.set("#PxBig.Visible", false);
            showCoins(ui, "PxPriceC", PRICE, List.of(), null);
            ui.set("#PxPriceText.Text", "");
            ui.set("#PxPriceCap.Text", "");
            ui.set("#PxRecipe.Visible", false);
            ui.set("#PxUsed.Text", "");
            return;
        }
        PriceBook.Entry e = PriceBook.entry(selected);
        PriceBook.Price p = PriceBook.price(selected);
        ui.set("#PxBig.Visible", true);
        ui.set("#PxBig.ItemId", selected);
        ui.set("#PxName.Text", name(selected));
        ui.set("#PxName.Style.TextColor", qualityColor(selected));
        ui.set("#PxId.Text", selected + (e.eldanior() ? "  ·  Eldanior" : "  ·  vanilla"));
        List<String> fams = PriceBook.families();
        String fam = e.family() >= 0 && e.family() < fams.size() ? fams.get(e.family()) : "";
        ui.set("#PxKind.Text", fam + (qualityName(selected).isEmpty() ? "" : "  ·  " + qualityName(selected))
                + (e.excluded() != null ? "  ·  hors économie (" + e.excluded() + ")" : ""));
        List<Map.Entry<String, Long>> pc = p.value() != null ? split(p.value()) : List.of();
        showCoins(ui, "PxPriceC", PRICE, pc, p.game() ? OK : GOLD);
        ui.set("#PxPriceText.Text", p.value() == null ? "Pas de prix" : p.value() == 0 ? "Gratuit" : "");
        ui.set("#PxPriceCap.Text", (p.value() != null ? num(p.value()) + " cuivres  ·  " : "") + sourceText(p, e));
        if (updateValue) ui.set("#PxValue.Value", (int) Math.min(Integer.MAX_VALUE, p.value() != null ? p.value() : 0));
        fillRecipe(ui, e);
        List<String> users = PriceBook.usedBy(selected);
        if (users.isEmpty()) {
            ui.set("#PxUsed.Text", "N'entre dans aucune recette.");
        } else {
            StringBuilder sb = new StringBuilder("Entre dans " + users.size() + " recette(s) : ");
            for (int i = 0; i < Math.min(3, users.size()); i++) sb.append(i > 0 ? ", " : "").append(name(users.get(i)));
            if (users.size() > 3) sb.append("…");
            sb.append(" (leur prix suit ce prix).");
            ui.set("#PxUsed.Text", sb.toString());
        }
    }

    private void fillRecipe(UICommandBuilder ui, PriceBook.Entry e) {
        boolean has = e.hasRecipe();
        ui.set("#PxRecipe.Visible", has);
        for (int i = 0; i < ING; i++) ingTargets[i] = null;
        if (!has) return;
        double total = 0;
        boolean missing = false;
        List<PriceBook.Ing> recipe = e.recipe();
        for (int i = 0; i < recipe.size(); i++) {
            PriceBook.Ing in = recipe.get(i);
            String target;
            Long unit;
            String label;
            if (in.type()) {
                Map.Entry<String, Long> best = PriceBook.cheapest(in.id());
                target = best != null ? best.getKey() : null;
                unit = best != null ? best.getValue() : null;
                label = target != null ? "Au choix, ex. " + name(PriceBook.base(target)) : "Type " + in.id();
            } else {
                target = PriceBook.base(in.id());
                unit = PriceBook.price(in.id()).value();
                label = name(target) + (in.id().startsWith("*") ? " (" + in.id().substring(in.id().indexOf("_State_") + 7).replace('_', ' ') + ")" : "");
            }
            if (unit == null) missing = true;
            else total += (double) unit * in.qty();
            if (i >= ING) continue;
            ingTargets[i] = target != null ? PriceBook.base(target) : null;
            ui.set("#PxIngIcon" + i + ".ItemId", ingTargets[i] != null ? ingTargets[i] : selected);
            ui.set("#PxIngName" + i + ".Text", label);
            ui.set("#PxIngInfo" + i + ".Text", "x" + in.qty() + (unit != null ? " à " + num(unit) + " = " + num(unit * in.qty()) : " · sans prix"));
            ui.set("#PxIngInfo" + i + ".Style.TextColor", unit != null ? GOLD : KO);
        }
        for (int i = 0; i < ING; i++) ui.set("#PxIng" + i + ".Visible", i < recipe.size());
        StringBuilder note = new StringBuilder();
        if (recipe.size() > ING) note.append("+").append(recipe.size() - ING).append(" ingrédient(s) non affiché(s). ");
        if (missing) {
            note.append("Un ingrédient n'a pas de prix : le prix fixe de l'objet est utilisé.");
        } else {
            note.append("Ingrédients ").append(num((long) total));
            if (e.byproducts() != null && !e.byproducts().isEmpty()) note.append(" moins ce qui est rendu");
            note.append(" x marge");
            if (e.outQty() > 1) note.append(" / ").append(e.outQty()).append(" produits");
            note.append(", arrondi.");
        }
        ui.set("#PxRecipeNote.Text", note.toString());
    }

    // ===== Evenements =====

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            handle(data);
        } catch (Throwable e) {
            EldaniorLogger.error("[Prix] action " + data.action + " " + data.index, e);
            message = "Une erreur est survenue (voir le journal du serveur).";
            messageOk = false;
            UICommandBuilder ui = new UICommandBuilder();
            ui.set("#PxMsg.Text", message);
            sendUpdate(ui);
        }
    }

    private void handle(Data data) {
        message = "";
        messageOk = true;
        int index = 0;
        try { index = Integer.parseInt(data.index); } catch (Exception ignored) { }
        boolean updateValue = true;
        switch (data.action == null ? "" : data.action) {
            case "tab" -> { tab = Math.max(0, Math.min(index, tabLabels().size() - 1)); search = null; page = 0; selected = null; }
            case "row" -> {
                List<String> ids = list();
                int n = page * ROWS + index;
                if (n >= 0 && n < ids.size()) selected = ids.get(n);
            }
            case "ing" -> { if (index >= 0 && index < ING && ingTargets[index] != null) selected = ingTargets[index]; }
            case "prev" -> { page--; selected = null; }
            case "next" -> { page++; selected = null; }
            case "search" -> {
                search = data.search == null || data.search.isBlank() ? null : data.search.trim();
                page = 0;
                selected = null;
            }
            case "clear" -> { search = null; page = 0; selected = null; }
            case "save" -> {
                if (data.value == null) { message = "Entrez un prix."; messageOk = false; updateValue = false; }
                else apply(data.value.longValue());
            }
            case "div2", "m10", "p10", "mul2" -> {
                Long cur = selected != null ? PriceBook.price(selected).value() : null;
                if (cur == null) { message = "Cet objet n'a pas encore de prix : entrez-en un."; messageOk = false; }
                else apply(switch (data.action) {
                    case "div2" -> cur / 2;
                    case "m10" -> Math.round(cur * 0.9);
                    case "p10" -> Math.max(cur + 1, Math.round(cur * 1.1));
                    default -> cur * 2;
                });
            }
            case "reset" -> {
                if (selected == null) break;
                if (PriceBook.reset(selected)) message = "Prix de base rétabli : " + priceLabel(selected) + ".";
                else { message = "Ce prix n'a pas été modifié en jeu."; messageOk = false; }
            }
            default -> { }
        }
        UICommandBuilder ui = new UICommandBuilder();
        fill(ui, updateValue);
        sendUpdate(ui);
    }

    private void apply(long value) {
        if (selected == null) return;
        if (value < 0) { message = "Le prix ne peut pas être négatif."; messageOk = false; return; }
        PriceBook.Entry e = PriceBook.entry(selected);
        if (!PriceBook.set(selected, value)) { message = "La monnaie a une valeur fixe."; messageOk = false; return; }
        int users = PriceBook.usedBy(selected).size();
        message = "Enregistré : " + priceLabel(selected) + (e != null && e.hasRecipe() ? " (prix imposé, la recette est ignorée)" : "")
                + (users > 0 ? ". " + users + " recette(s) recalculée(s)." : ".");
        EldaniorLogger.info("[Prix] " + playerRef.getUsername() + " : " + selected + " = " + value);
    }

    // ===== Outils =====

    private String priceLabel(String id) {
        Long v = PriceBook.price(id).value();
        return v == null ? "sans prix" : CoinWallet.format(v);
    }

    private static String sourceText(PriceBook.Price p, PriceBook.Entry e) {
        if (p.game()) return p.source() == PriceBook.Source.FORCE ? "imposé en jeu" : "fixé en jeu";
        return switch (p.source()) {
            case CALCULE -> "calculé par la recette";
            case FIXE -> e.hasRecipe() ? "fixé (recette circulaire)" : "fixé (prix.json)";
            case FORCE -> "imposé (prix.json)";
            case MONNAIE -> "valeur de la pièce";
            case HORS -> "hors économie : un prix l'y fait entrer";
            default -> "à fixer";
        };
    }

    private static String sourceColor(PriceBook.Price p) {
        if (p.game()) return OK;
        return switch (p.source()) {
            case CALCULE -> "#5fa8d3";
            case FORCE -> GOLD;
            case MONNAIE -> "#f1c40f";
            case HORS -> "#5a6676";
            case A_FIXER, INCONNU -> KO;
            default -> "#96a9be";
        };
    }

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

    private static String num(long v) {
        return String.format(Locale.FRANCE, "%,d", v).replace(' ', ' ').replace(' ', ' ');
    }

    /** Minuscules sans accents, pour chercher et trier. */
    private static String fold(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /** Nom traduit dans la langue du joueur (repli : anglais, puis identifiant), mis en cache. */
    private String name(String id) {
        return names.computeIfAbsent(id, k -> {
            Item it = Item.getAssetMap().getAsset(k);
            if (it == null) return k;
            String key = it.getTranslationKey();
            try {
                com.hypixel.hytale.server.core.modules.i18n.I18nModule i18n = com.hypixel.hytale.server.core.modules.i18n.I18nModule.get();
                String lang = playerRef.getLanguage();
                String n = lang != null ? i18n.getMessage(lang, key) : null;
                if (n == null || n.isEmpty() || n.equals(key)) n = i18n.getMessage("en-US", key);
                if (n != null && !n.isEmpty() && !n.equals(key)) return n;
            } catch (Exception ignored) { }
            return k.replace('_', ' ');
        });
    }

    private static String qualityId(String id) {
        Item item = Item.getAssetMap().getAsset(id);
        if (item == null) return "";
        try {
            ItemQuality q = ItemQuality.getAssetMap().getAsset(item.getQualityIndex());
            return q != null ? q.getId() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String qualityColor(String id) {
        return switch (qualityId(id)) {
            case "Uncommon" -> "#5fc95f";
            case "Rare" -> "#3498db";
            case "Epic" -> "#9b59b6";
            case "Unique" -> "#e74c3c";
            case "Legendary" -> "#f1c40f";
            case "Divin" -> "#00ffff";
            default -> "#ffffff";
        };
    }

    private static String qualityName(String id) {
        return switch (qualityId(id)) {
            case "Junk" -> "Rebut";
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

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .append(new KeyedCodec<>("@Search", Codec.STRING), (d, v) -> d.search = v, d -> d.search).add()
                .append(new KeyedCodec<>("@Value", Codec.INTEGER), (d, v) -> d.value = v, d -> d.value).add()
                .build();
        public String action, index, search;
        public Integer value;
    }
}
