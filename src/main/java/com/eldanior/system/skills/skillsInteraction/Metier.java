package com.eldanior.system.skills.skillsInteraction;

/**
 * Metiers : acces aux etablis. Sortis des competences passives le 2026-11-24 (demande de l'utilisateur) ; ils
 * s'apprennent avec un parchemin de Metier et ne comptent pas dans la limite de passifs.
 * L'identifiant (CRAFT_...) est garde : il sert aux sauvegardes, a la progression d'artisanat et aux titres.
 *
 * FICHIER GENERE par scripts/passifs/gen_passifs.py.
 */
public enum Metier {

    ARTISANAT("Artisanat", "Permet d'utiliser tous les établis (passe-partout).", "Eldanior_Parchemin_Metier_Artisanat"),
    CRAFT_ARTISAN_BASE("Artisan", "Permet d'utiliser l'établi de base.", "Eldanior_Parchemin_Metier_Artisan"),
    CRAFT_CUISINE("Cuisine", "Permet d'utiliser l'établi de cuisine.", "Eldanior_Parchemin_Metier_Cuisine"),
    CRAFT_FONDERIE("Fonderie", "Permet d'utiliser le fourneau.", "Eldanior_Parchemin_Metier_Fonderie"),
    CRAFT_ARMURERIE("Armurerie", "Permet d'utiliser l'établi d'armurier.", "Eldanior_Parchemin_Metier_Armurerie"),
    CRAFT_FORGE_ARMES("Forge d'Armes", "Permet d'utiliser l'établi d'armes.", "Eldanior_Parchemin_Metier_Forge_Armes"),
    CRAFT_FORGE_DIVINE("Forge Divine", "Permet d'utiliser la Forge Divine (artefacts).", "Eldanior_Parchemin_Metier_Forge_Divine"),
    CRAFT_TANNERIE("Tannerie", "Permet d'utiliser la tannerie.", "Eldanior_Parchemin_Metier_Tannerie"),
    CRAFT_ALCHIMIE("Alchimie", "Permet d'utiliser l'établi d'alchimie.", "Eldanior_Parchemin_Metier_Alchimie"),
    CRAFT_SCIERIE("Scierie", "Permet d'utiliser la scierie.", "Eldanior_Parchemin_Metier_Scierie"),
    CRAFT_AGRICULTURE("Agriculture", "Permet d'utiliser l'établi de ferme.", "Eldanior_Parchemin_Metier_Agriculture"),
    CRAFT_RECYCLAGE("Recyclage", "Permet d'utiliser l'établi de recyclage.", "Eldanior_Parchemin_Metier_Recyclage");

    private final String displayName;
    private final String description;
    private final String scrollItem;

    Metier(String displayName, String description, String scrollItem) {
        this.displayName = displayName;
        this.description = description;
        this.scrollItem = scrollItem;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    /** Objet du parchemin qui apprend ce metier. */
    public String getScrollItem() { return scrollItem; }

    public static Metier fromName(String id) {
        if (id == null) return null;
        for (Metier m : values()) if (m.name().equalsIgnoreCase(id)) return m;
        return null;
    }
}
