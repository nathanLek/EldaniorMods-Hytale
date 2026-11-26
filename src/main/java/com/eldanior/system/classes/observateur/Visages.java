package com.eldanior.system.classes.observateur;

import com.eldanior.system.config.EldaniorLogger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Visages de l'Observateur (2026-11-17), generes par scripts/observateur/gen_visages.py :
 *  - un objet portrait cache par modele d'entite (Eldanior_Visage_<ModelId>), que l'element ItemPreviewComponent d'un .ui
 *    dessine en 3D (carte du HUD d'Arkies Domini, fenetre Sans Visage) ;
 *  - le catalogue classe /eldanior/visages.json (pnj, animal, monstre) de la fenetre Sans Visage.
 */
public final class Visages {

    private Visages() {}

    public static final String PREFIX = "Eldanior_Visage_";
    public static final String[] CATEGORIES = {"pnj", "animal", "monstre"};

    public record Entry(String modelId, String label) {}

    private static Map<String, List<Entry>> catalog;
    /** ItemScale des apercus (gen_visages.py : modele trop grand pour son cadre), par id de modele ; absent = 1. */
    private static final Map<String, Float> SCALES = new java.util.HashMap<>();

    /** Portrait 3D du modele (objet du mod), ou null s'il n'existe pas. */
    public static String portrait(String modelAssetId) {
        if (modelAssetId == null || modelAssetId.isEmpty()) return null;
        String id = PREFIX + modelAssetId;
        try {
            return Item.getAssetMap().getAsset(id) != null ? id : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Taille a donner a l'apercu 3D d'un portrait (ItemPreviewComponent.ItemScale) : les gros modeles sont reduits. */
    public static float scaleOfPortrait(String portraitItemId) {
        if (portraitItemId == null || !portraitItemId.startsWith(PREFIX)) return 1f;
        load();
        return SCALES.getOrDefault(portraitItemId.substring(PREFIX.length()), 1f);
    }

    /** Modeles d'une categorie (pnj, animal, monstre), filtres par texte (null = tous). */
    public static List<Entry> list(String category, String filter) {
        List<Entry> all = load().getOrDefault(category, List.of());
        if (filter == null || filter.isBlank()) return all;
        String f = filter.trim().toLowerCase(Locale.ROOT);
        List<Entry> out = new ArrayList<>();
        for (Entry e : all) if (e.label().toLowerCase(Locale.ROOT).contains(f)) out.add(e);
        return out;
    }

    public static String categoryLabel(String c) {
        return switch (c) {
            case "pnj" -> "PNJ";
            case "animal" -> "Animaux";
            case "monstre" -> "Monstres";
            case "joueur" -> "Joueurs";
            default -> c;
        };
    }

    private static synchronized Map<String, List<Entry>> load() {
        if (catalog != null) return catalog;
        Map<String, List<Entry>> map = new LinkedHashMap<>();
        try (InputStream in = Visages.class.getResourceAsStream("/eldanior/visages.json")) {
            if (in != null) {
                JsonObject root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                for (String c : CATEGORIES) {
                    List<Entry> list = new ArrayList<>();
                    if (root.has(c)) for (JsonElement el : root.getAsJsonArray(c)) {
                        var pair = el.getAsJsonArray();
                        list.add(new Entry(pair.get(0).getAsString(), pair.get(1).getAsString()));
                    }
                    map.put(c, List.copyOf(list));
                }
                if (root.has("scales"))
                    for (var en : root.getAsJsonObject("scales").entrySet()) SCALES.put(en.getKey(), en.getValue().getAsFloat());
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Visages] lecture du catalogue", e);
        }
        catalog = map;
        return catalog;
    }
}
