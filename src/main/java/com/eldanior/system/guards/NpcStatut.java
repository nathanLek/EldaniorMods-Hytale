package com.eldanior.system.guards;

/**
 * Statut commun des PNJ de poste (demande du 2026-11-16) : trois paliers, Apprenti, Expert, Maitre, pour tous les PNJ, choisis
 * dans la fenetre du poste (NpcPostPage, liste « Statut »). Chaque type garde son propre systeme de rang ; le statut s'y
 * traduit :
 *  - aventurier : Aventurier (ordinaire) / Exceptionnel / Heros (AdventurerTier) ;
 *  - marchand Luo : Apprenti / Negociant / Grand Marchand (LuoMerchants.Rank) ;
 *  - commercant : Apprenti / Compagnon / Maitre (NpcTrades.Rang) ;
 *  - gardes, chevaliers, assassins, disciples (rang martial) : niveau martial Apprenti / Expert / Maitre Martial ;
 *  - Sensei (deja Grand Maitre au minimum) : Grand Maitre / Saint / Maitre Supreme Martial ;
 *  - chasseur de prime et criminel : niveau d'aventure (rang E-D, C-B, A-S) ;
 *  - exclusifs sans rang (maitre de guilde, forgeron legendaire) : statut garde dans Post.statut et affiche dans le nom.
 * Le champ Niveau de la fenetre, s'il est rempli, l'emporte sur le niveau tire du statut.
 */
public enum NpcStatut {
    APPRENTI("apprenti", "Apprenti", 70, 450, 120),
    EXPERT("expert", "Expert", 230, 600, 330),
    MAITRE("maitre", "Maître", 340, 800, 560);

    public final String arg, label;
    /** Niveau martial (Apprenti 40, Expert 180, Maitre 280), niveau du Sensei (400 a 998), niveau d'aventure. */
    private final int martial, sensei, adventure;

    NpcStatut(String arg, String label, int martial, int sensei, int adventure) {
        this.arg = arg; this.label = label; this.martial = martial; this.sensei = sensei; this.adventure = adventure;
    }

    /** Statut d'apres la valeur du menu (anciennes valeurs de rang acceptees), null si « au hasard » ou inconnu. */
    public static NpcStatut of(String v) {
        if (v == null) return null;
        String k = v.toLowerCase(java.util.Locale.ROOT);
        if (k.startsWith("rang_")) k = k.substring(5);
        return switch (k) {
            case "apprenti", "ordinaire" -> APPRENTI;
            case "expert", "exceptionnel", "negociant", "compagnon" -> EXPERT;
            case "maitre", "maître", "heros", "héros", "grand" -> MAITRE;
            default -> null;
        };
    }

    public AdventurerTier adventurer() {
        return switch (this) { case APPRENTI -> AdventurerTier.ORDINAIRE; case EXPERT -> AdventurerTier.EXCEPTIONNEL; case MAITRE -> AdventurerTier.HEROS; };
    }

    public LuoMerchants.Rank luo() {
        return switch (this) { case APPRENTI -> LuoMerchants.Rank.APPRENTI; case EXPERT -> LuoMerchants.Rank.NEGOCIANT; case MAITRE -> LuoMerchants.Rank.MAITRE; };
    }

    public NpcTrades.Rang trade() {
        return switch (this) { case APPRENTI -> NpcTrades.Rang.APPRENTI; case EXPERT -> NpcTrades.Rang.COMPAGNON; case MAITRE -> NpcTrades.Rang.MAITRE; };
    }

    /** Niveau a donner au poste pour ce statut (0 : le type n'a pas de niveau, le statut est garde dans Post.statut). */
    public int levelFor(GuardType t) {
        if (t == GuardType.SENSEI) return sensei;
        if (MartialRanks.applies(t)) return martial;
        if (t == GuardType.CHASSEUR_PRIME || t == GuardType.CRIMINEL) return adventure;
        return 0;
    }

    /** Statut affiche dans le menu pour un poste existant (null : au hasard / inconnu). */
    static NpcStatut current(GuardManager.Post p) {
        if (p == null) return null;
        if (p.statut != null) return of(p.statut);
        return switch (p.type) {
            case AVENTURIER -> switch (AdventurerTier.of(p)) { case ORDINAIRE -> APPRENTI; case EXCEPTIONNEL -> EXPERT; case HEROS -> MAITRE; };
            case MARCHAND -> of(LuoMerchants.rankOf(p).arg);
            case COMMERCANT -> NpcTrades.metier(p) != null && NpcTrades.metier(p).ranked() ? of(NpcTrades.rang(p).arg) : null;
            default -> null;
        };
    }

    /** « · Expert » apres le role des PNJ dont le statut n'a pas d'autre effet (exclusifs sans rang), sinon "". */
    static String suffix(GuardManager.Post p) {
        NpcStatut s = p != null && p.statut != null ? of(p.statut) : null;
        return s != null && shownInName(p.type) ? " · " + s.label : "";
    }

    /**
     * Le statut s'affiche dans le nom seulement pour les PNJ sans autre rang (2026-11-20 : il est maintenant garde pour tous, afin
     * que la fenetre du poste le retrouve ; un garde Expert garde « Expert Martial » sans « · Expert » en plus).
     */
    static boolean shownInName(GuardType t) {
        return APPRENTI.levelFor(t) == 0 && t != GuardType.AVENTURIER && t != GuardType.MARCHAND && t != GuardType.COMMERCANT;
    }
}
