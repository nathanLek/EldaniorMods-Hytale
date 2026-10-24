package com.eldanior.system.guards;

import java.util.List;

import static com.eldanior.system.gui.tabs.ProfilTab.getAdventureRank;

/**
 * Type d'un aventurier : arme en main, tenue et nom. Deduit de la graine de son poste (toujours le meme pour ce poste).
 * La facon de combattre vient encore du role (guerrier) : roles propres a chaque type a venir.
 */
public enum AdventurerType {
    GUERRIER("Guerrier", "Eldanior_Npc_Aventurier", "Eldanior_Npc_Aventurier_Compagnon",
            new String[]{"Weapon_Sword_Iron", "Weapon_Sword_Iron", "Weapon_Sword_Steel", "Weapon_Sword_Steel", "Weapon_Sword_Mithril", "Weapon_Sword_Onyxium"},
            new String[]{"Leather_Medium", "Leather_Medium", "Iron", "Steel", "Mithril", "Onyxium"}),
    MAGE("Mage", "Eldanior_Npc_Aventurier_Leger", "Eldanior_Npc_Aventurier_Compagnon_Mage",
            new String[]{"Weapon_Staff_Copper", "Weapon_Staff_Iron", "Weapon_Staff_Iron", "Weapon_Staff_Cobalt", "Weapon_Staff_Mithril", "Weapon_Staff_Onyxium"},
            new String[]{"Cloth_Cotton", "Cloth_Linen", "Cloth_Wool", "Cloth_Silk", "Cloth_Cindercloth", "Cloth_Cindercloth"}),
    ARCHER("Archer", "Eldanior_Npc_Aventurier_Leger", "Eldanior_Npc_Aventurier_Compagnon_Archer",
            new String[]{"Weapon_Shortbow_Copper", "Weapon_Shortbow_Iron", "Weapon_Shortbow_Iron", "Weapon_Shortbow_Combat", "Weapon_Shortbow_Combat", "Weapon_Shortbow_Vampire"},
            new String[]{"Leather_Light", "Leather_Light", "Leather_Medium", "Leather_Medium", "Leather_Heavy", "Leather_Heavy"}),
    ASSASSIN("Assassin", "Eldanior_Npc_Aventurier_Leger", "Eldanior_Npc_Aventurier_Compagnon_Assassin",
            new String[]{"Weapon_Daggers_Crude", "Weapon_Daggers_Copper", "Weapon_Daggers_Iron", "Weapon_Daggers_Iron", "Weapon_Daggers_Void", "Weapon_Daggers_Void"},
            new String[]{"Leather_Soft", "Leather_Soft", "Leather_Raven", "Leather_Raven", "Leather_Raven", "Leather_Raven"});

    private final String label, recruitRole, companionRole;
    private final String[] weapons, armorSets;                 // par rang : E, D, C, B, A, S

    AdventurerType(String label, String recruitRole, String companionRole, String[] weapons, String[] armorSets) {
        this.label = label; this.recruitRole = recruitRole; this.companionRole = companionRole;
        this.weapons = weapons; this.armorSets = armorSets;
    }

    public String getLabel() { return label; }

    /** Indice du rang (0 : E ... 5 : S). */
    private static int rankIndex(int level) {
        return Math.max(0, "EDCBAS".indexOf(getAdventureRank(level)));
    }

    /** Arme de son rang. */
    public String getWeapon(int level) { return weapons[rankIndex(level)]; }

    /** Tenue de son rang : torse, mains, jambes (pas de casque : on voit son visage). */
    public List<String> getArmor(int level) {
        String set = "Armor_" + armorSets[rankIndex(level)];
        return List.of(set + "_Chest", set + "_Hands", set + "_Legs");
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