package com.eldanior.system.titles.nobility.family;

/**
 * Hierarchie commune des ordres (2026-11-18, idee de l'utilisateur adaptee) : sept rangs, du plus haut (I) au plus bas (VII).
 * Le titre prend « Imperial » dans les ordres du Clan Luo et « Royal » dans ceux de la maison Eldanior (voir {@link #title}).
 *  - Grand Maitre : le fondateur (ex-Capitaine), un seul ;
 *  - Vice-Maitre : un seul (ex-Lieutenant) ; Haut Commandant : 2 ; Commandant : 4 ; Maitre, Disciple : sans limite ;
 *  - Aspirant : entree par serment.
 * Un officier ne change que le rang de ceux sous lui, jamais au-dela du rang juste sous le sien (voir KnightOrder.canSet).
 * Les rangs d'officier (Commandant et plus) sont reserves aux Chevaliers de la maison ou plus.
 */
public enum KnightOrderRole {
    GRAND_MAITRE("Grand Maître", "I", "Direction suprême et autorité sur l'ensemble de l'ordre.", 1, true),
    VICE_MAITRE("Vice-Maître", "II", "Assiste le Grand Maître et assure la continuité du commandement.", 1, true),
    HAUT_COMMANDANT("Haut Commandant", "III", "Dirige les divisions et supervise les opérations majeures.", 2, false),
    COMMANDANT("Commandant", "IV", "Encadre les membres et dirige les missions ou unités.", 4, false),
    MAITRE("Maître", "V", "Membre confirmé, d'une grande maîtrise et d'une fidélité exemplaire.", 0, true),
    DISCIPLE("Disciple", "VI", "Membre officiellement intégré à l'ordre.", 0, true),
    ASPIRANT("Aspirant", "VII", "Nouvelle recrue en période d'apprentissage et d'évaluation.", 0, true);

    // Anciens noms (sauvegardes d'avant le 2026-11-18)
    public static final KnightOrderRole CAPITAINE = GRAND_MAITRE, LIEUTENANT = VICE_MAITRE, MEMBRE = DISCIPLE;

    private final String base, numeral, duty;
    private final int max;
    private final boolean suffixed;

    KnightOrderRole(String base, String numeral, String duty, int max, boolean suffixed) {
        this.base = base;
        this.numeral = numeral;
        this.duty = duty;
        this.max = max;
        this.suffixed = suffixed;
    }

    /** Nombre de places de ce rang dans un ordre (0 : sans limite). */
    public int max() { return max; }

    public String numeral() { return numeral; }

    public String duty() { return duty; }

    /** Commandant ou plus : rang d'officier, reserve aux Chevaliers de la maison (ou plus). */
    public boolean officer() { return ordinal() <= COMMANDANT.ordinal(); }

    /** Rang au-dessus (null pour le Grand Maitre). */
    public KnightOrderRole up() { return ordinal() > 0 ? values()[ordinal() - 1] : null; }

    /** Rang au-dessous (null pour l'Aspirant). */
    public KnightOrderRole down() { return ordinal() < values().length - 1 ? values()[ordinal() + 1] : null; }

    /** Titre selon la maison : « Grand Maître Impérial » (Luo), « Disciple Royal » (Eldanior), « Maître » (autres). */
    public String title(String familyId) {
        if (!suffixed || familyId == null) return base;
        String f = FamilyManager.migrateId(familyId);
        if ("luo".equalsIgnoreCase(f)) return base + " Impérial";
        if ("eldanior".equalsIgnoreCase(f)) return base + " Royal";
        return base;
    }

    public String getDisplayName() { return base; }

    /** Couleur du rang : or (direction), bleu (commandement), gris-bleu (membres). */
    public String getColorCode() {
        return ordinal() <= VICE_MAITRE.ordinal() ? "#FFD700" : officer() ? "#5b9bd5" : "#aabbcc";
    }

    public static KnightOrderRole fromString(String name) {
        if (name == null) return null;
        return switch (name.toUpperCase()) {
            case "CAPITAINE" -> GRAND_MAITRE;
            case "LIEUTENANT" -> VICE_MAITRE;
            case "MEMBRE" -> DISCIPLE;
            default -> {
                try { yield valueOf(name.toUpperCase()); } catch (IllegalArgumentException e) { yield null; }
            }
        };
    }
}
