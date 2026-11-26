package com.eldanior.system.titles.nobility.family;

/**
 * Hierarchie d'une famille noble, du bas vers le haut (2026-11-18, decision de l'utilisateur) :
 * Recrue < Chevalier < Chevalier Royal < Noble (Comte, Baron) < Seigneur < Doyen < Patriarche.
 * Seuls les Chevaliers ou plus demandent la creation d'un ordre de chevalerie (le demandeur en devient le Capitaine).
 * Le code enregistre dans PlayerLevelData.status ne change pas pour les anciens rangs : VICE (ex-Vice-Patriarche) s'affiche Seigneur.
 *
 * Rangs automatiques (2026-11-18) : Doyen = Duc (aussi le Patriarche d'une maison ducale dans le Marquisat, voir IntendantPage),
 * Noble = Comte ou Baron, Chevalier Royal = Chevalier ; le Patriarche ne choisit que Recrue, Chevalier et Seigneur (effective()).
 * Anciens droits (2026-11-05) : le Patriarche nomme et retire tous les titres ; Doyens et Seigneur invitent, et nomment
 * ou retirent les Nobles. Une famille compte au plus {@link #MAX_ELDERS} Doyens (registre dans FamilyManager, connectes ou non).
 */
public enum FamilyRole {
    MEMBER("MEMBER", "Recrue"),
    CHEVALIER("CHEVALIER", "Chevalier"),
    CHEVALIER_ROYAL("CHEVALIER_ROYAL", "Chevalier Royal"),
    NOBLE("NOBLE", "Noble"),
    VICE("VICE", "Seigneur"),
    ELDER("ELDER", "Doyen"),
    PATRIARCH("PATRIARCH", "Patriarche");

    public static final int MAX_ELDERS = 4;

    private final String code;
    private final String label;

    FamilyRole(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() { return code; }
    public String label() { return label; }

    /** Rang lu dans PlayerLevelData.status ; null si le joueur n'a pas de famille. Un code inconnu vaut membre sans titre. */
    public static FamilyRole of(String status) {
        if (status == null || status.isEmpty()) return null;
        for (FamilyRole r : values()) if (r.code.equals(status)) return r;
        return MEMBER;
    }

    /** Libelle d'un status, "Aucun" sans famille. */
    public static String labelOf(String status) {
        FamilyRole r = of(status);
        return r == null ? "Aucun" : r.label;
    }

    // ===== Rangs automatiques (2026-11-18, regle de l'utilisateur) =====
    // Doyen = Duc, Noble = Comte ou Baron, Chevalier Royal = Chevalier (titre de noblesse) : jamais attribues a la main.
    // Le Patriarche choisit seulement Recrue, Chevalier et Seigneur.

    /** Rang de maison donne par un titre de noblesse ; null : aucun (le rang choisi par le Patriarche s'applique). */
    public static FamilyRole fromNobility(com.eldanior.system.titles.nobility.NobilityRank r) {
        if (r == null) return null;
        return switch (r) {
            case DUC, MARQUIS, ROI -> ELDER;
            case COMTE, BARON -> NOBLE;
            case CHEVALIER -> CHEVALIER_ROYAL;
            default -> null;
        };
    }

    /** Rang qui ne se donne qu'avec un titre de noblesse (Doyen, Noble, Chevalier Royal). */
    public boolean automatic() { return this == ELDER || this == NOBLE || this == CHEVALIER_ROYAL; }

    /**
     * Rang effectif : Patriarche ; sinon le Doyen (Duc) l'emporte sur tout ; un Seigneur nomme le reste au-dessus d'un Noble ;
     * sinon le rang du titre de noblesse ; sinon le rang choisi (Recrue, Chevalier). Un rang automatique sans le titre qui va
     * avec redevient Chevalier.
     */
    public static FamilyRole effective(FamilyRole stored, com.eldanior.system.titles.nobility.NobilityRank nobility) {
        if (stored == PATRIARCH) return PATRIARCH;
        FamilyRole auto = fromNobility(nobility);
        if (auto == ELDER) return ELDER;
        if (stored == VICE) return VICE;
        if (auto != null) return auto;
        if (stored == null) return MEMBER;
        return stored.automatic() ? CHEVALIER : stored;
    }

    /** Rang au-dessus, par choix du Patriarche : Recrue → Chevalier → Seigneur ; un Noble ou un Chevalier Royal → Seigneur. */
    public FamilyRole promoted() {
        return switch (this) {
            case MEMBER -> CHEVALIER;
            case CHEVALIER, CHEVALIER_ROYAL, NOBLE -> VICE;
            default -> null;
        };
    }

    /** Rang au-dessous, par choix : Seigneur → Chevalier (le titre de noblesse reprend le dessus), Chevalier → Recrue. */
    public FamilyRole demoted() {
        return switch (this) {
            case VICE -> CHEVALIER;
            case CHEVALIER -> MEMBER;
            default -> null;
        };
    }

    /** Chevalier ou plus : peut demander la creation d'un ordre de chevalerie (2026-11-18). */
    public boolean knightly() { return ordinal() >= CHEVALIER.ordinal(); }

    /** Patriarche, Doyen ou Seigneur : payent la reserve avec les coffres (2026-11-18 ; allocation par semaine sauf Patriarche). */
    public boolean stewardOfTreasury() { return this == PATRIARCH || this == ELDER || this == VICE; }

    public boolean canInvite() {
        return this == PATRIARCH || this == ELDER || this == VICE;
    }

    /**
     * Ce rang peut-il faire passer quelqu'un de from a to ? Seuls les rangs choisis changent a la main : le Patriarche nomme
     * Recrue, Chevalier et Seigneur ; les Doyens et le Seigneur font passer de Recrue a Chevalier et retour.
     */
    public boolean canChange(FamilyRole from, FamilyRole to) {
        if (from == null || to == null || from == PATRIARCH || to == PATRIARCH || to.automatic() || from == ELDER) return false;
        if (this == PATRIARCH) return true;
        return canInvite() && from.ordinal() <= CHEVALIER.ordinal() && to.ordinal() <= CHEVALIER.ordinal();
    }
}
