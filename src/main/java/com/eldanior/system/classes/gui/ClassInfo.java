package com.eldanior.system.classes.gui;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.Rarity;

import java.util.ArrayList;
import java.util.List;

/**
 * Textes et couleurs des fenetres de classe (cartes de l'accueil et de la statue, recrutement) : runes de la classe lues dans
 * leurs items, raretes, armes. Repris de l'ancien ClassSelectionScreen (supprime le 2026-11-26 avec ClassIntroScreen et
 * ClassEvolutionIntroScreen, remplaces par ClassChoicePage).
 */
public final class ClassInfo {

    private ClassInfo() {}

    /** Une rune telle que la carte l'affiche : item (image), nom, rarete et recharge, effet, couleur de rarete. */
    public record RuneInfo(String itemId, String name, String meta, String effect, String color) {}

    /** Runes de la classe (ClassModel.getRuneIds), lues dans leurs items : nom, qualite, recharge et description. */
    public static List<RuneInfo> runes(ClassModel model) { return runes(model, 70); }

    /** Runes de la classe, effet coupe a max caracteres (fenetres a cartes : accueil, statue). */
    public static List<RuneInfo> runes(ClassModel model, int max) {
        List<RuneInfo> out = new ArrayList<>();
        for (String id : model.getRuneIds()) {
            var item = com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap().getAsset(id);
            if (item == null) continue;
            var props = item.getTranslationProperties();
            String name = text(props != null ? props.getName() : null, id.replace("Rune_", ""));
            String desc = text(props != null ? props.getDescription() : null, "");
            var quality = com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality.getAssetMap().getAsset(item.getQualityIndex());
            String q = quality != null ? quality.getId() : "Common";
            String meta = qualityLabel(q);
            if (item.getAbility() instanceof com.hypixel.hytale.server.core.asset.type.item.config.CoreItemAbility ability) {
                meta += "  ·  " + Math.round(ability.getCooldownS()) + " s";
            }
            out.add(new RuneInfo(id, name, meta, shorten(runeEffect(desc), max), qualityColor(q)));
        }
        return out;
    }

    /** Texte d'un item : traduit s'il s'agit d'une cle (runes vanilla), tel quel sinon (runes du mod). */
    private static String text(String value, String fallback) {
        if (value == null || value.isEmpty()) return fallback;
        String translated = com.eldanior.system.gui.tabs.InventaireTab.translate(value);
        return translated != null ? translated : (value.startsWith("server.") ? fallback : value);
    }

    /** Effet d'une rune : sa description sans la phrase d'introduction (« Sort commun de ... (classes). ») ni le cout. */
    private static String runeEffect(String desc) {
        String d = desc.replaceFirst("^Sort [^.]*\\.\\s*", "");
        int cost = d.indexOf(" Coût");
        return (cost > 0 ? d.substring(0, cost) : d).trim();
    }

    private static String qualityLabel(String quality) {
        return switch (quality) {
            case "Common" -> "Commune";
            case "Uncommon" -> "Peu commune";
            case "Rare" -> "Rare";
            case "Epic" -> "Épique";
            case "Unique" -> "Unique";
            case "Legendary" -> "Légendaire";
            case "Divin" -> "Divine";
            default -> quality;
        };
    }

    /** Couleurs des qualites (Server/Item/Qualities, vanilla et mod). */
    private static String qualityColor(String quality) {
        return switch (quality) {
            case "Uncommon" -> "#3e9049";
            case "Rare" -> "#2770b7";
            case "Epic" -> "#8b339e";
            case "Unique" -> "#ce1624";
            case "Legendary" -> "#bb8a2c";
            case "Divin" -> "#07d8fb";
            default -> "#c9d2dd";
        };
    }

    private static String shorten(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 1).trim() + "…";
    }

    public static String rarityLabel(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "commune";
            case RARE -> "rare";
            case EPIC -> "épique";
            case UNIQUE -> "unique";
            case LEGENDARY -> "légendaire";
            case DIVINE -> "divine";
        };
    }

    /** Armes maniables, dans l'ordre des maitrises de la classe. */
    public static String weapons(List<com.eldanior.system.config.configs.WeaponMastery> masteries) {
        if (masteries == null || masteries.isEmpty()) return "Aucune";
        if (masteries.contains(com.eldanior.system.config.configs.WeaponMastery.ANY)) return "Toutes les armes";
        List<String> names = new ArrayList<>();
        for (var m : masteries) {
            names.add(switch (m) {
                case SWORD -> "Épée";
                case AXE -> "Hache";
                case BOW -> "Arc";
                case STAFF -> "Bâton";
                case DAGGER -> "Dague";
                case SHIELD -> "Bouclier";
                case SPEAR -> "Lance";
                case SPELLBOOK -> "Grimoire";
                case MACE -> "Masse";
                case GUN -> "Pistolet";
                case CLUB -> "Massue";
                case RIFLE -> "Fusil";
                case FLAIL -> "Fléau";
                case ANY -> "Toutes";
            });
        }
        return String.join("  ·  ", names);
    }

    public static String getRarityHexColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "#E0E0E0";
            case RARE -> "#3498DB";
            case EPIC -> "#9B59B6";
            case UNIQUE -> "#E74C3C";
            case LEGENDARY -> "#F1C40F";
            case DIVINE -> "#00FFFF";
            default -> "#FFFFFF";
        };
    }
}
