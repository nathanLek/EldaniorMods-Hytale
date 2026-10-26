package com.eldanior.system.guards;

import java.util.List;

/**
 * PNJ de poste (poses par /es garde poser, reapparition a leur poste). Assets generes par scripts/guards/gen_guards.py
 * (gardes) et scripts/npcs/gen_npcs.py (mage royal, aventurier, chevalier errant, villageois) ; roles des aventuriers
 * archer / assassin / mage et du chasseur de prime ecrits a la main. Niveaux : NpcData.
 */
public enum GuardType {
    VILLE("Eldanior_Guard_Ville", "Garde de ville", "ville", Kind.GUARD, false),
    ROYAL("Eldanior_Guard_Royal", "Garde royal", "royal", Kind.GUARD, false),
    CHEVALIER("Eldanior_Guard_Chevalier", "Chevalier", "chevalier", Kind.GUARD, false),
    /** Fait des rondes autour de son poste et appelle les gardes proches (PvP, monstres). */
    PATROUILLEUR("Eldanior_Guard_Patrouilleur", "Patrouilleur", "patrouilleur", Kind.GUARD, false),
    /** Archimage de la garde royale : boule de feu et sorts (RoyalMageMagic). */
    MAGE_ROYAL("Eldanior_Guard_Mage", "Mage royal", "mage", Kind.GUARD, false),
    /** Erre dans un rayon de 200 blocs autour de son poste ; combat les monstres. */
    CHEVALIER_ERRANT("Eldanior_Npc_Chevalier_Errant", "Chevalier errant", "errant", Kind.ROAMER, true,
            "Armor_Steel_Chest", "Armor_Steel_Hands", "Armor_Steel_Legs"),
    /** Se promene dans sa ville, va voir les autres PNJ et les blocs. */
    VILLAGEOIS("Eldanior_Npc_Villageois", "Villageois", "villageois", Kind.VILLAGER, true),
    /** Recrutable contre de l'or : suit le joueur, revient a son poste a sa mort ou a la fin du contrat. */
    AVENTURIER("Eldanior_Npc_Aventurier", "Aventurier", "aventurier", Kind.ADVENTURER, true),
    /** Erre comme le chevalier errant et traque les joueurs PK (BountyHunter). Role selon son type : guerrier, archer, assassin. */
    CHASSEUR_PRIME("Eldanior_Npc_Chasseur_Prime", "Chasseur de prime", "chasseur", Kind.HUNTER, true),
    /**
     * Hors-la-loi (Criminals) : rode autour de son repaire, attaque les joueurs ; gardes, chasseurs et aventuriers le
     * combattent. Sa prime (selon son niveau) va a celui qui l'abat. Role selon son type : guerrier, archer, assassin.
     */
    CRIMINEL("Eldanior_Criminel", "Criminel", "criminel", Kind.CRIMINAL, true),
    /** PNJ de service, fixe : fonder une guilde, l'ameliorer (touche d'interaction : GuildMasterSystem -> GuildMasterScreen). */
    MAITRE_GUILDE("Eldanior_Npc_Maitre_Guilde", "Maître de guilde", "maitre_guilde", Kind.SERVICE, true);

    public enum Kind { GUARD, ROAMER, VILLAGER, ADVENTURER, HUNTER, CRIMINAL, SERVICE }

    public static final String ROLE_PREFIX = "Eldanior_Guard_";
    /** Role de l'aventurier une fois recrute (suit son joueur). */
    public static final String COMPANION_ROLE = "Eldanior_Npc_Aventurier_Compagnon";
    /** Armure de l'aventurier recrute (le role recrutable l'a deja dans son fichier). */
    static final List<String> COMPANION_ARMOR = List.of("Armor_Leather_Medium_Chest", "Armor_Leather_Medium_Hands", "Armor_Leather_Medium_Legs");

    private final String roleId, label, arg;
    private final Kind kind;
    private final boolean randomSkin;
    private final List<String> armor;

    GuardType(String roleId, String label, String arg, Kind kind, boolean randomSkin, String... armor) {
        this.roleId = roleId; this.label = label; this.arg = arg; this.kind = kind; this.randomSkin = randomSkin;
        this.armor = List.of(armor);
    }

    public String getRoleId() { return roleId; }
    public String getLabel() { return label; }
    public String getArg() { return arg; }
    public Kind getKind() { return kind; }
    /** Skin de joueur aleatoire (different pour chaque PNJ pose, stable pour un meme poste). */
    public boolean hasRandomSkin() { return randomSkin; }
    /** Armure posee par le code (les variantes de gabarit n'acceptent pas de champ Armor). */
    public List<String> getArmor() { return armor; }
    /** Garde : alerte au PvP, appel a l'aide entre gardes. */
    public boolean isGuard() { return kind == Kind.GUARD; }

    /** Rang d'apres l'argument de commande (ville, royal, ..., villageois), null si inconnu. */
    public static GuardType fromArg(String s) {
        if (s == null) return null;
        for (GuardType t : values()) if (t.arg.equalsIgnoreCase(s) || t.name().equalsIgnoreCase(s)) return t;
        return null;
    }

    /** Type d'apres l'identifiant de role du PNJ (tous les roles d'aventurier / de chasseur comptent), null sinon. */
    public static GuardType fromRole(String roleId) {
        if (roleId == null || !roleId.startsWith("Eldanior_")) return null;
        if (roleId.startsWith("Eldanior_Npc_Aventurier")) return AVENTURIER;
        if (roleId.startsWith(CHASSEUR_PRIME.roleId)) return CHASSEUR_PRIME;
        if (roleId.startsWith(CRIMINEL.roleId)) return CRIMINEL;
        for (GuardType t : values()) if (t.roleId.equals(roleId)) return t;
        return null;
    }

    /** PNJ de poste allie (tout sauf le criminel) : les sorts et les gardes l'epargnent. */
    public static boolean isAlly(String roleId) {
        GuardType t = fromRole(roleId);
        return t != null && t != CRIMINEL;
    }

    /** Criminel ou complice : ennemi des gardes, chasseurs et aventuriers (son role ignore les joueurs : Criminals l'agresse). */
    public static boolean isOutlaw(String roleId) {
        return fromRole(roleId) == CRIMINEL;
    }

    /** Role d'un aventurier recrute (compagnon, tout type). */
    public static boolean isCompanionRole(String roleId) {
        return roleId != null && roleId.startsWith(COMPANION_ROLE);
    }

    /** Role d'un aventurier libre, recrutable (tout type). */
    public static boolean isRecruitRole(String roleId) {
        return roleId != null && roleId.startsWith("Eldanior_Npc_Aventurier") && !isCompanionRole(roleId);
    }

    /** Liste des arguments, pour les messages d'aide. */
    public static String args() {
        StringBuilder sb = new StringBuilder();
        for (GuardType t : values()) sb.append(sb.length() > 0 ? "|" : "").append(t.arg);
        return sb.toString();
    }
}
