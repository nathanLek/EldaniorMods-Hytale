package com.eldanior.system.guards;

import java.util.List;
import java.util.Random;

/**
 * Professions of the Merchant (GuardType.COMMERCANT): each one opens a game shop (BarterShop) in the custom window
 * (ShopPage) and gives the NPC its outfit. Ranked professions (Rang: apprentice, journeyman, master) use the shops
 * Eldanior_Metier_<Metier>_<Rang> (scripts/guards/gen_metiers.py) ; the banker opens the bank (BankPage) ; the steward keeps
 * Guild_Npc for now. Value of Post.shop: "METIER:<NAME>"; any civil NPC can also
 * receive a profession as a trade (window of the post block).
 */
public final class NpcTrades {

    private NpcTrades() {}

    public enum Metier {
        BANQUIER("Banquier", "Bank_Npc", "#d4af37", "Banquier", "Votre or est plus en sûreté dans mes coffres que dans votre bourse."),
        INTENDANT("Intendant", "Guild_Npc", "#7fa6c9", "Cloth_Wool", "Fournitures pour aventuriers : potions, clés de donjon, parchemins."),
        MEUBLES("Marchand de meubles", null, "#b07a4a", "Cloth_Cotton", "Tables, lits, lanternes : de quoi meubler une demeure."),
        ALCHIMISTE("Alchimiste", null, "#9b59b6", "Cloth_Cindercloth", "Potions et élixirs, fraîchement distillés."),
        FORGERON("Forgeron", null, "#c0643a", "Leather_Heavy", "Lames, haches et boucliers, tout juste sortis de la forge."),
        TAVERNIER("Tavernier", null, "#c9a24a", "Cloth_Linen", "Pain, fromage et plats chauds pour la route."),
        FERMIER("Fermier", null, "#7fae4a", "Cloth_Linen", "Graines et récoltes de la saison."),
        MINEUR("Mineur", null, "#8a8f99", "Leather_Medium", "Pioches, pelles et minerais."),
        BUCHERON("Bûcheron", null, "#6f8f3a", "Leather_Light", "Haches, écorce et sève."),
        ELEVEUR("Éleveur", null, "#c99060", "Cloth_Cotton", "Bêtes, cages et outils d'élevage."),
        POISSONNIER("Poissonnier", null, "#3f8fbf", "Cloth_Linen", "Poissons frais et fruits de mer."),
        MENUISIER("Menuisier", null, "#a8743a", "Leather_Soft", "Établis et ateliers."),
        /** 2026-11-13 : Commercant Immobilier, bureau des terres (LandOfficeScreen) ; jamais tire au sort (RANDOM). */
        IMMOBILIER("Commerçant immobilier", null, "#5fa7a0", "Cloth_Cotton", "Maisons, parcelles et terres : achat, vente, location."),
        /** 2026-11-13 : oeufs de familiers (scripts/familiers/gen_familiers.py) ; boutique unique, jamais tire au sort. */
        DRESSEUR("Dresseur de bêtes", "Eldanior_Dresseur_Betes", "#7fb4d8", "Leather_Medium", "Œufs de bêtes communes et inhabituelles, nourriture pour familiers ; les bêtes rares se méritent (sa quête)."),
        /**
         * 2026-11-16 : Maroquinier, seul vendeur des ameliorations du sac a dos (I et II ; la III reste celle du jeu, en donjon)
         * et de la Poche dimensionnelle (10 imperiales). Boutique unique (scripts/poche/gen_maroquinier.py), jamais tire au sort.
         */
        MAROQUINIER("Maroquinier", "Eldanior_Maroquinier", "#8e5fbf", "Leather_Soft", "Sacs, poches et améliorations de sac à dos."),
        /** 2026-11-19 : Bailli, petit intendant d'une ville de Comte (registre de la ville), paye chaque semaine par son tresor. */
        BAILLI("Bailli", null, "#7fa6c9", "Cloth_Wool", "Je tiens le registre de la ville au nom du Comte."),
        /** 2026-11-19 : Gardien du Portail Continental : encaisse le voyage et ouvre la porte pour le voyageur. */
        PASSEUR("Gardien du portail", null, "#7fd4ff", "Cloth_Cindercloth", "Payez le passage, je vous ouvre la porte.");

        /** Metiers tires au sort pour un commercant sans metier (les 12 d'origine : ajouter un metier ne doit pas changer les tirages). */
        static final int RANDOM = 12;

        final String label, shop, theme, outfit, pitch;

        Metier(String label, String shop, String theme, String outfit, String pitch) {
            this.label = label; this.shop = shop; this.theme = theme; this.outfit = outfit; this.pitch = pitch;
        }

        public String getLabel() { return label; }

        /** Ce que le metier rachete (onglet Rachat), en plus de la liste de sa BarterShop. */
        ShopPage.Buys buys() {
            return switch (this) {
                case BANQUIER, INTENDANT, IMMOBILIER, DRESSEUR, MAROQUINIER, BAILLI, PASSEUR -> new ShopPage.Buys("", id -> false);
                case MEUBLES -> buys("meubles et décorations", "Furniture_", "Deco_");
                case ALCHIMISTE -> buys("potions, plantes médicinales, cristaux, butins de monstres",
                        "Potion_", "Eld_Potion_", "Elixir_", "Plant_Crop_Health", "Plant_Crop_Mana", "Plant_Crop_Stamina", "Plant_Flower",
                        "Ingredient_Life", "Ingredient_Crystal", "Butin_");
                case FORGERON -> buys("armes, boucliers, armures, outils, minerais et lingots",
                        "Weapon_", "Eldanior_Weapon_", "Armor_", "Tool_", "Ore_", "Ingredient_Bar_");
                case TAVERNIER -> buys("nourriture, fruits et légumes", "Food_", "Plant_Fruit_", "Plant_Crop_");
                case FERMIER -> buys("récoltes, graines, pousses et œufs", "Plant_Crop_", "Plant_Seeds_", "Plant_Sapling", "Plant_Fruit_", "Food_Egg");
                case MINEUR -> buys("minerais, lingots, cristaux, pioches et pelles",
                        "Ore_", "Ingredient_Bar_", "Ingredient_Crystal", "Tool_Pickaxe", "Tool_Shovel", "Rock_");
                case BUCHERON -> buys("bois, écorce, sève, pousses et haches", "Wood_", "Ingredient_Tree_", "Ingredient_Stick",
                        "Ingredient_Fibre", "Plant_Sapling", "Tool_Hatchet");
                case ELEVEUR -> buys("peaux, cuirs, plumes, laine et viandes", "Ingredient_Hide", "Ingredient_Leather", "Ingredient_Feathers",
                        "Ingredient_Fabric", "Food_Wildmeat", "Food_Beef", "Food_Pork", "Food_Chicken", "Food_Egg");
                case POISSONNIER -> buys("poissons et fruits de mer", "Fish_", "Food_Fish");
                case MENUISIER -> buys("bois, établis et meubles", "Wood_", "Bench_", "Furniture_", "Ingredient_Stick");
            };
        }

        /** Metier a trois rangs (boutiques Eldanior_Metier_<Metier>_<Rang>, scripts/guards/gen_metiers.py) ; banquier et intendant : non. */
        public boolean ranked() { return this != BANQUIER && this != INTENDANT && this != IMMOBILIER && this != DRESSEUR && this != MAROQUINIER
                && this != BAILLI && this != PASSEUR; }

        /** BarterShop du metier pour ce rang. */
        String shopFor(Rang r) {
            if (!ranked()) return shop;
            String n = name().toLowerCase();
            return "Eldanior_Metier_" + Character.toUpperCase(n.charAt(0)) + n.substring(1) + "_" + (r != null ? r : Rang.APPRENTI).asset;
        }

        private static ShopPage.Buys buys(String label, String... prefixes) {
            return new ShopPage.Buys(label, id -> {
                for (String p : prefixes) if (id.startsWith(p)) return true;
                return false;
            });
        }

        List<String> armor() {
            String b = "Armor_" + outfit + "_";
            return List.of(b + "Head", b + "Chest", b + "Hands", b + "Legs");
        }
    }

    /**
     * Rangs des metiers, comme les marchands Luo : Apprenti (Commun et Peu commun, reassort chaque jour), Compagnon (Rare et
     * Epique, tous les 2 jours), Maitre (Unique et Legendaire, une piece, tous les 3 jours).
     */
    public enum Rang {
        APPRENTI("apprenti", "Apprenti", "Apprenti"),
        COMPAGNON("compagnon", "Compagnon", "Compagnon"),
        MAITRE("maitre", "Maitre", "Maître");

        final String arg, asset, label;
        Rang(String arg, String asset, String label) { this.arg = arg; this.asset = asset; this.label = label; }
        public String getLabel() { return label; }
        /** Valeur du menu du poste (distincte des rangs Luo « apprenti »...). */
        public String menuValue() { return "rang_" + arg; }
    }

    static Rang rangArg(String s) {
        if (s == null) return null;
        String k = s.startsWith("rang_") ? s.substring(5) : s;
        for (Rang r : Rang.values()) if (r.arg.equalsIgnoreCase(k) || r.name().equalsIgnoreCase(k)) return r;
        return null;
    }

    static final String PREFIX = "METIER:";

    /** Profession described by Post.shop ("METIER:BANQUIER"), null otherwise. */
    static Metier of(String shop) {
        if (shop == null || !shop.startsWith(PREFIX)) return null;
        try { return Metier.valueOf(shop.substring(PREFIX.length()).split(":")[0]); } catch (IllegalArgumentException e) { return null; }
    }

    static Metier ofArg(String s) {
        if (s == null) return null;
        String k = s.startsWith(PREFIX) ? s.substring(PREFIX.length()) : s;
        for (Metier m : Metier.values()) if (m.name().equalsIgnoreCase(k)) return m;
        return null;
    }

    static String value(Metier m) { return PREFIX + m.name(); }

    static String value(Metier m, Rang r) { return r == null || !m.ranked() ? value(m) : PREFIX + m.name() + ":" + r.name(); }

    /** Rang du commercant : celui du poste (« METIER:FORGERON:MAITRE »), sinon tire de sa graine (Maitre plus rare). */
    static Rang rang(GuardManager.Post p) {
        String[] parts = p.shop != null ? p.shop.split(":") : new String[0];
        if (parts.length > 2) try { return Rang.valueOf(parts[2]); } catch (IllegalArgumentException ignored) { }
        int r = new Random(p.skinSeed ^ 0x5eedL).nextInt(20);
        return r < 10 ? Rang.APPRENTI : r < 17 ? Rang.COMPAGNON : Rang.MAITRE;
    }

    /** BarterShop du commercant (metier et rang). */
    static String shopOf(GuardManager.Post p) {
        Metier m = metier(p);
        return m != null ? m.shopFor(rang(p)) : null;
    }

    /** Parcelle (territoire) d'une position, null hors des parcelles. */
    static String parcelAt(String world, double x, double y, double z) {
        try {
            var parcel = com.eldanior.system.territory.ParcelManager.getParcelAt(world, x, y, z);
            return parcel != null ? parcel.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Un seul intendant par territoire : celui deja poste sur la parcelle de cette position, null sinon. */
    static GuardManager.Post stewardOn(String world, double x, double y, double z) {
        String parcel = parcelAt(world, x, y, z);
        if (parcel == null) return null;
        for (GuardManager.Post p : GuardManager.getPosts())
            if (p.type == GuardType.COMMERCANT && metier(p) == Metier.INTENDANT && p.getWorld().equals(world)
                    && parcel.equals(parcelAt(p.getWorld(), p.x, p.y, p.z))) return p;
        return null;
    }

    /** « Maître forgeron », « Apprenti alchimiste » ; banquier, intendant : le metier seul. */
    static String title(GuardManager.Post p) {
        Metier m = metier(p);
        if (m == null) return "Commerçant";
        return m.ranked() ? rang(p).label + " " + m.label.toLowerCase() : m.label;
    }

    /** Profession of the post (Merchant: drawn at random if none). */
    static Metier metier(GuardManager.Post p) {
        Metier m = of(p.shop);
        if (m == null && p.type == GuardType.COMMERCANT) m = Metier.values()[new Random(p.skinSeed).nextInt(Metier.RANDOM)];
        return m;
    }
}
