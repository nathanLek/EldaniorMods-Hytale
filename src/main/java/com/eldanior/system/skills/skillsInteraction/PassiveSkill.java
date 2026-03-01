package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.skills.skills.passives.*;

public enum PassiveSkill {

    // --- GUERRIER ---
    // Regarde ici : on ajoute "new SwordMastery()" en dernier argument !ManaWell
    SWORD_MASTERY("SWORD_MASTERY", "Maîtrise de l'Épée", "Augmente les dégâts infligés avec une épée de 15%.", new SwordMastery()),
    ATHLETICISM("ATHLETICISM", "Athlétisme", "Réduit la consommation d'endurance lors du sprint.", new Athleticism()),
    MANAWELL("MANAWELL", "Puit de Mana", "Augmente du mana à vie de 10%.", new ManaWell()),
    DETECTIONOFVITALPOINTS("DETECTIONOFVITALPOINTS", "Detection De Point Vital", "+15% fixes ajoutés à ta stat critique", new DetectionOfVitalPoints()),

    STONE_SKIN("STONE_SKIN", "Peau de Pierre", "Réduit les dégâts physiques reçus de 5%.", new StoneSkin()),
    BRONZE_SKIN("BRONZE_SKIN", "Peau de Bronze", "Réduit les dégâts physiques reçus de 8%.", new BronzeSkin()),
    IRON_SKIN("IRON_SKIN", "Peau de Fer", "Réduit les dégâts physiques reçus de 10%.", new IronSkin()),
    STEEL_SKIN("STEEL_SKIN", "Peau d'Acier", "Réduit les dégâts physiques reçus de 15%.", new SteelSkin()),
    OBSIDIAN_SKIN("OBSIDIAN_SKIN", "Peau d'Obsidienne", "Réduit les dégâts physiques reçus de 25%.", new ObsidianSkin()),
    DIAMOND_SKIN("DIAMOND_SKIN", "Peau de Diamant", "Réduit les dégâts physiques reçus de 35%.", new DiamondSkin()),
    DIVINE_AEGIS("DIVINE_AEGIS", "Égide Divine", "Réduit les dégâts physiques reçus de 50%.", new DivineAegis());

    private final String id;
    private final String displayName;
    private final String description;

    // LA MAGIE EST ICI : Chaque Enum contient la logique de son propre combat
    private final IPassiveCombatSkill logic;

    PassiveSkill(String id, String displayName, String description, IPassiveCombatSkill logic) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.logic = logic;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public IPassiveCombatSkill getLogic() {
        return logic;
    }
}