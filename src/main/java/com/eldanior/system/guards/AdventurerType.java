package com.eldanior.system.guards;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * Type d'un aventurier : equipement, roles et nom. Deduit de la graine de son poste (toujours le meme pour ce poste).
 * L'equipement est tire dans un choix par rang (graine du poste : meme tenue a chaque apparition). Les armes d'un type
 * partagent le jeu d'animations de son role (epees du guerrier, dagues de l'assassin...) : ne pas y melanger d'autres armes.
 */
public enum AdventurerType {
    GUERRIER("Guerrier", "Eldanior_Npc_Aventurier", "Eldanior_Npc_Aventurier_Compagnon",
            ranks("Weapon_Sword_",
                    "Crude Wood Copper",
                    "Copper Steel_Rusty Cutlass Scrap Iron",
                    "Iron Bronze Bronze_Ancient Stone_Trork Bone",
                    "Steel Thorium Doomed Runic Steel_Incandescent",
                    "Cobalt Frost Nexus Adamantite Silversteel",
                    "Silversteel Onyxium Mithril Adamantite Steel_Incandescent Frost"),
            ranks("Armor_",
                    "Wood Copper Leather_Medium",
                    "Copper Bronze Trork Iron",
                    "Iron Steel Steel_Ancient",
                    "Steel Cobalt Thorium",
                    "Thorium Adamantite Mithril Cobalt Steel_Ancient",
                    "Mithril Prisma Onyxium Adamantite Thorium"),
            ranks("Weapon_Shield_",
                    "Wood Rusty",
                    "Copper Scrap",
                    "Iron Scrap_Spiked",
                    "Thorium Doomed",
                    "Cobalt Orbis_Knight Adamantite Thorium",
                    "Mithril Praetorian Onyxium Orbis_Incandescent Adamantite")),
    MAGE("Mage", "Eldanior_Npc_Aventurier_Leger", "Eldanior_Npc_Aventurier_Compagnon_Mage",
            ranks("Weapon_Staff_",
                    "Wood Wood_Rotten Wood_Kweebec Copper",
                    "Copper Iron Bone Bronze",
                    "Iron Bronze Doomed Crystal_Fire_Trork Voidcrystal",
                    "Thorium Frost Crystal_Purple Crystal_Red",
                    "Cobalt Wizard Adamantite Crystal_Ice Crystal_Purple Crystal_Red",
                    "Crystal_Flame Onyxium Mithril Crystal_Ice Wizard Voidcrystal"),
            ranks("Armor_",
                    "Cloth_Cotton Cloth_Linen",
                    "Cloth_Linen Wool Cloth_Wool",
                    "Wool Cloth_Wool Cloth_Silk",
                    "Cloth_Wool Cloth_Silk",
                    "Cloth_Silk Cloth_Cindercloth Cloth_Wool Wool",
                    "Cloth_Silk Cloth_Cindercloth Cloth_Wool"),
            null),
    ARCHER("Archer", "Eldanior_Npc_Aventurier_Leger", "Eldanior_Npc_Aventurier_Compagnon_Archer",
            ranks("Weapon_Shortbow_",
                    "Crude Copper Combat",
                    "Copper Iron_Rusty Iron",
                    "Iron Bronze Flame Frost",
                    "Thorium Doomed Cobalt",
                    "Cobalt Adamantite Vampire Frost Flame Thorium",
                    "Onyxium Vampire Mithril Adamantite Frost Flame"),
            ranks("Armor_",
                    "Leather_Soft Leather_Light",
                    "Leather_Light Leather_Medium",
                    "Leather_Light Leather_Medium Bronze",
                    "Leather_Medium Leather_Heavy",
                    "Leather_Heavy Cobalt Leather_Medium Leather_Raven",
                    "Leather_Heavy Adamantite Leather_Raven Mithril"),
            null),
    ASSASSIN("Assassin", "Eldanior_Npc_Aventurier_Leger", "Eldanior_Npc_Aventurier_Compagnon_Assassin",
            ranks("Weapon_Daggers_",
                    "Crude Copper",
                    "Copper Iron Bone",
                    "Iron Bronze Bronze_Ancient Stone_Trork",
                    "Thorium Doomed Void_Crystal",
                    "Cobalt Adamantite_Saurian Adamantite Void_Crystal Doomed",
                    "Void_Crystal Onyxium Mithril Adamantite_Saurian Adamantite"),
            ranks("Armor_",
                    "Leather_Soft Leather_Light",
                    "Leather_Soft Leather_Light",
                    "Leather_Soft Leather_Raven",
                    "Leather_Raven Leather_Medium",
                    "Leather_Raven Leather_Heavy Leather_Soft Leather_Medium",
                    "Leather_Raven Onyxium Leather_Heavy Leather_Medium"),
            null);

    private final String label, recruitRole, companionRole;
    /** Choix par rang (E, D, C, B, A, S). */
    private final String[][] weapons, armorSets, offHands;

    AdventurerType(String label, String recruitRole, String companionRole, String[][] weapons, String[][] armorSets, String[][] offHands) {
        this.label = label; this.recruitRole = recruitRole; this.companionRole = companionRole;
        this.weapons = weapons; this.armorSets = armorSets; this.offHands = offHands;
    }

    /** Choix d'un rang a l'autre : prefixe + noms separes par des espaces. */
    private static String[][] ranks(String prefix, String... perRank) {
        String[][] out = new String[perRank.length][];
        for (int i = 0; i < perRank.length; i++) {
            String[] names = perRank[i].split(" ");
            for (int j = 0; j < names.length; j++) names[j] = prefix + names[j];
            out[i] = names;
        }
        return out;
    }

    public String getLabel() { return label; }

    /** Indice du rang (0 : E ... 5 : S). */
    private static int rankIndex(int level) {
        return Math.max(0, "EDCBAS".indexOf(getAdventureRank(level)));
    }

    /** Rang de son equipement : le sien, +1 pour un exceptionnel, +2 pour un heros. */
    private static int gearRank(GuardManager.Post p) {
        AdventurerTier tier = AdventurerTier.of(p);
        int bonus = tier == AdventurerTier.HEROS ? 2 : tier == AdventurerTier.EXCEPTIONNEL ? 1 : 0;
        return Math.min(5, rankIndex(p.level) + bonus);
    }

    /** Choix de son rang d'equipement, elargi a celui du rang precedent (plus de variete). */
    private static List<String> pool(String[][] perRank, GuardManager.Post p) {
        int r = gearRank(p);
        List<String> out = new ArrayList<>(List.of(perRank[r]));
        if (r > 0) for (String s : perRank[r - 1]) if (!out.contains(s)) out.add(s);
        return out;
    }

    /** Tirage propre a ce poste (meme graine : meme equipement a chaque apparition). */
    private static Random rng(GuardManager.Post p, int salt) {
        return new Random(p.skinSeed * 31 + salt);
    }

    /** Arme en main. */
    public String getWeapon(GuardManager.Post p) {
        List<String> pool = pool(weapons, p);
        return pool.get(rng(p, 1).nextInt(pool.size()));
    }

    /** Main gauche (bouclier du guerrier) ; null : aucune. */
    public String getOffHand(GuardManager.Post p) {
        if (offHands == null) return null;
        List<String> pool = pool(offHands, p);
        return pool.get(rng(p, 2).nextInt(pool.size()));
    }

    /**
     * Tenue composee piece par piece : l'ensemble du torse est tire, chaque autre piece lui est assortie une fois sur deux
     * (sinon tiree a part). Un ordinaire n'a pas de casque, un mage jamais : il porte un chapeau (getHat).
     */
    public List<String> getArmor(GuardManager.Post p) {
        List<String> pool = pool(armorSets, p);
        Random rng = rng(p, 3);
        String chest = pool.get(rng.nextInt(pool.size()));
        boolean helmet = this != MAGE && AdventurerTier.of(p) != AdventurerTier.ORDINAIRE;
        List<String> out = new ArrayList<>();
        for (String piece : new String[]{"_Head", "_Chest", "_Hands", "_Legs"}) {
            if (piece.equals("_Head") && !helmet) continue;
            String set = piece.equals("_Chest") || rng.nextBoolean() ? chest : pool.get(rng.nextInt(pool.size()));
            out.add(set + piece);
        }
        return out;
    }

    /** Chapeaux de mage (accessoires de tete du skin) et leurs couleurs (communes a leurs nuanciers). */
    private static final String[] HATS = {"WitchHat", "WitchHat", "GoblinMage_Hat", "WideBrimHat"};
    private static final String[] HAT_COLORS = {"Purple", "Blue", "Red", "Black", "Green", "Turquoise", "Brown"};

    /**
     * Chapeau de mage, a la place du casque : exceptionnel et heros toujours, ordinaire une fois sur deux. Candidats a
     * essayer dans l'ordre (le premier que le skin accepte) ; vide : pas de chapeau.
     */
    public List<String> getHat(GuardManager.Post p) {
        if (this != MAGE || (AdventurerTier.of(p) == AdventurerTier.ORDINAIRE && rng(p, 5).nextBoolean())) return List.of();
        Random rng = rng(p, 6);
        String hat = HATS[rng.nextInt(HATS.length)], color = HAT_COLORS[rng.nextInt(HAT_COLORS.length)];
        String variant = rng.nextBoolean() ? ".Buckle" : ".No_Buckle";
        return List.of(hat + "." + color + variant, hat + "." + color, "WitchHat." + color + variant, "WitchHat." + color);
    }

    public static AdventurerType fromArg(String s) {
        if (s == null) return null;
        for (AdventurerType t : values()) if (t.name().equalsIgnoreCase(s) || t.label.equalsIgnoreCase(s)) return t;
        return null;
    }

    /** Role de l'aventurier libre (recrutable). */
    public String getRecruitRole() { return recruitRole; }
    /** Role de l'aventurier une fois recrute (sa facon de combattre). */
    public String getCompanionRole() { return companionRole; }

    /** Type d'un aventurier d'apres la graine de son poste. */
    public static AdventurerType of(long seed) {
        return values()[(int) Math.floorMod(seed, (long) values().length)];
    }
}
