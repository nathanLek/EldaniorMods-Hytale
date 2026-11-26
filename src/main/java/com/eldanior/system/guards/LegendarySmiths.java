package com.eldanior.system.guards;

import java.util.List;

/**
 * Le forgeron legendaire (GuardType.LEGENDAIRE ; un seul PNJ a trois noms possibles depuis le 2026-11-16), legendes du monde d'Eldanior et seuls PNJ qui forgent le fer divin :
 * armes et armures divines, munitions, pistolets... (jamais les tresors de famille). Poses par un admin seulement (commande ou
 * bloc de poste), un par variante en principe. Meme niveau pour les trois ; chacun sa tenue et ses stocks.
 *
 * Sa forge est une boutique (ShopPage) : chaque objet coute ses materiaux (lingots divins, adamantite...) et beaucoup de
 * pieces, pris dans le sac. Boutiques Eldanior_Forge_Legendaire_<Variante> (scripts/guards/gen_forge_legendaire.py),
 * tenue Armor_Forgeron_Luo_* (scripts/armures/gen_forgeron_luo.py) pour le forgeron du Dragon ; cuir epais pour les autres.
 * Post.shop = "LEGENDE:<VARIANTE>".
 */
public final class LegendarySmiths {

    private LegendarySmiths() {}

    /**
     * 2026-11-16 (demande de l'utilisateur) : un seul forgeron legendaire, le forgeron du Dragon (tenue Armor_Forgeron_Luo_*,
     * forge Eldanior_Forge_Legendaire_Humain), qui peut porter trois noms. Les constantes NAIN et ELFE ne sont plus que des noms
     * (gardees pour les postes deja enregistres « LEGENDE:NAIN » / « LEGENDE:ELFE ») ; leurs tenues et forges sont supprimees.
     */
    public enum Smith {
        HUMAIN("Tetsugen Luo", "le Marteau du Dragon",
                "Le clan m'a confié le feu du Dragon d'Or. Apportez le métal divin et l'or : je forgerai ce que nul autre ne peut forger."),
        NAIN("Durgrim", "Barbe-de-Braise",
                "Du fer divin ? Posez-le sur l'enclume, et votre or à côté. Le feu du Dragon fera le reste."),
        ELFE("Elarion", "Chant-d'Argent",
                "Le métal divin se souvient des étoiles. Confiez-le-moi, et le feu du Dragon lui rendra sa lumière… contre son juste prix.");

        /** Tenue, forge et couleur communes aux trois noms. */
        static final String OUTFIT = "Luo", SHOP = "Eldanior_Forge_Legendaire_Humain", THEME = "#c9a24a";

        final String name, epithet, theme, pitch;

        Smith(String name, String epithet, String pitch) {
            this.name = name; this.epithet = epithet; this.theme = THEME; this.pitch = pitch;
        }

        public String getLabel() { return name + ", " + epithet; }
        public String arg() { return name().toLowerCase(); }
        String shop() { return SHOP; }

        /** Tenue du forgeron du Dragon, pour les trois noms. */
        List<String> armor() {
            String b = "Armor_Forgeron_" + OUTFIT + "_";
            return List.of(b + "Head", b + "Chest", b + "Hands", b + "Legs");
        }
    }

    static final String PREFIX = "LEGENDE:";

    static Smith ofArg(String s) {
        if (s == null) return null;
        String k = s.startsWith(PREFIX) ? s.substring(PREFIX.length()) : s.startsWith("legende_") ? s.substring(8) : s;
        for (Smith m : Smith.values()) if (m.name().equalsIgnoreCase(k)) return m;
        return null;
    }

    static String value(Smith s) { return PREFIX + s.name(); }

    /** Forgeron du poste (Humain par defaut). */
    static Smith of(GuardManager.Post p) {
        Smith s = ofArg(p.shop);
        return s != null ? s : Smith.HUMAIN;
    }

    /** « Durgrim Barbe-de-Braise - Forgeron légendaire » */
    static String nameplate(GuardManager.Post p) {
        Smith s = of(p);
        return (s == Smith.HUMAIN ? s.name : s.name + " " + s.epithet) + " - Forgeron légendaire" + NpcStatut.suffix(p);
    }

    /** Ne rachete rien : sa forge n'a pas d'onglet Rachat. */
    static final ShopPage.Buys BUYS_NOTHING = new ShopPage.Buys("", id -> false);
}
