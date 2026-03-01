package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.skills.skills.passives.Common.Agilité.*;
import com.eldanior.system.skills.skills.passives.Common.Attack.*;
import com.eldanior.system.skills.skills.passives.Common.Defense.StoneSkin;
import com.eldanior.system.skills.skills.passives.Common.Detection.*;
import com.eldanior.system.skills.skills.passives.Divin.Attack.JudgmentOfGenesis;
import com.eldanior.system.skills.skills.passives.Divin.Defense.DivineAegis;
import com.eldanior.system.skills.skills.passives.Epique.Attack.DetectionOfVitalPoints;
import com.eldanior.system.skills.skills.passives.Epique.Attack.SeismicStrike;
import com.eldanior.system.skills.skills.passives.Epique.Defense.SteelSkin;
import com.eldanior.system.skills.skills.passives.Epique.Detection.UniversalDetection;
import com.eldanior.system.skills.skills.passives.Epique.Magique.ManaWell;
import com.eldanior.system.skills.skills.passives.Epique.Maitrise.SwordMastery;
import com.eldanior.system.skills.skills.passives.Legendaire.Attack.AnnihilatorStrike;
import com.eldanior.system.skills.skills.passives.Legendaire.Defense.DiamondSkin;
import com.eldanior.system.skills.skills.passives.Rare.Attack.FuryStrike;
import com.eldanior.system.skills.skills.passives.Rare.Defense.IronSkin;
import com.eldanior.system.skills.skills.passives.Uncommon.Attack.PredatoryStrike;
import com.eldanior.system.skills.skills.passives.Uncommon.Defense.BronzeSkin;
import com.eldanior.system.skills.skills.passives.Unique.Attack.PhantomStrike;
import com.eldanior.system.skills.skills.passives.Unique.Defense.ObsidianSkin;

public enum PassiveSkill {

    // --- GUERRIER ---
    // Regarde ici : on ajoute "new SwordMastery()" en dernier argument !ManaWell
    SWORD_MASTERY("SWORD_MASTERY", "Maîtrise de l'Épée", "Augmente les dégâts infligés avec une épée de 15%.", new SwordMastery()),
    ATHLETICISM("ATHLETICISM", "Athlétisme", "Réduit la consommation d'endurance lors du sprint.", new Athleticism()),
    MANAWELL("MANAWELL", "Puit de Mana", "Augmente du mana à vie de 10%.", new ManaWell()),
    DETECTIONOFVITALPOINTS("DETECTIONOFVITALPOINTS", "Detection De Point Vital", "+15% fixes ajoutés à ta stat critique", new DetectionOfVitalPoints()),

    // --- DÉTECTION  ---
    UNIVERSAL_DETECTION("UNIVERSAL_DETECTION", "Detection Universel", "+400% Conscience des menaces.", new UniversalDetection()),
    EAGLE_EYE("EAGLE_EYE", "Œil de Rapace", "+15% Distance de détection.", new EagleEye()),
    SURVIVAL_INSTINCT("SURVIVAL_INSTINCT", "Instinct de Survie", "+10% Conscience des menaces.", new SurvivalInstinct()),
    NIGHT_VISION("NIGHT_VISION", "Vision Nocturne", "+20% Visibilité en basse lumière.", new NightVision()),
    SIXTH_SENSE("SIXTH_SENSE", "Sixième Sens", "+5% Détection de l'invisibilité.", new SixthSense()),
    TRACKER("TRACKER", "Pisteur", "+25% Visibilité des traces de mobs.", new Tracker()),

    // --- ATTACK ---
    INSTINCTIVE_STRIKE("INSTINCTIVE_STRIKE", "Frappe Instinctive", "5% de chances d'infliger 10% de dégâts bonus.", new InstinctiveStrike()),
    PREDATORY_STRIKE("PREDATORY_STRIKE", "Frappe de Prédateur", "10% de chances d'infliger 15% de dégâts bonus et de voler de la vie.", new PredatoryStrike()),
    FURY_STRIKE("FURY_STRIKE", "Frappe de Fureur", "12% de chances d'infliger +25% dégâts (30% sur ennemis faibles).", new FuryStrike()),
    SEISMIC_STRIKE("SEISMIC_STRIKE", "Frappe Sismique", "15% de chances d'infliger +35% dégâts et de créer une onde de choc.", new SeismicStrike()),
    PHANTOM_STRIKE("PHANTOM_STRIKE", "Frappe Fantôme", "18% de chances d'infliger +50% de dégâts dévastateurs.", new PhantomStrike()),
    ANNIHILATOR_STRIKE("ANNIHILATOR_STRIKE", "Frappe de l'Annihilateur", "22% de chances d'infliger +75% de dégâts. Chance de Double Impact.", new AnnihilatorStrike()),
    JUDGMENT_OF_GENESIS("JUDGMENT_OF_GENESIS", "Décret de la Genèse", "30% de chances de +150% dégâts. Annihile instantanément les faibles.", new JudgmentOfGenesis()),

    WIND_STEP("WIND_STEP", "Pas de Vent", "+3% Vitesse de déplacement.", new WindStep()),
    LIGHT_REFLEXES("LIGHT_REFLEXES", "Réflexes Éclairs", "+4% Vitesse d'attaque.", new LightReflexes()),
    ELDANIOR_SUPPLENESS("ELDANIOR_SUPPLENESS", "Souplesse d'Eldanior", "+10% Hauteur de saut.", new EldaniorSuppleness()),
    KEEN_SENSES("KEEN_SENSES", "Sens Aiguisés", "+2% Chance de Critique.", new KeenSenses()),

    DEEP_SLASH("DEEP_SLASH", "Entaille Profonde", "Ajoute +1 points de dégâts physiques à chaque attaque.", new DeepSlash()),
    DUELIST_SWIFTNESS("DUELIST_SWIFTNESS", "Vivacité du Duelliste", "Augmente la vitesse d'attaque de 5%.", new DuelistSwiftness()),
    PRESSURE_POINT("PRESSURE_POINT", "Point de Pression", "Le premier coup sur une cible en pleine santé inflige 15% de dégâts bonus.", new PressurePoint()),
    HAUNTING_THRUST("HAUNTING_THRUST", "Estocade Obsédante", "Chaque coup consécutif sur la même cible augmente vos dégâts de 3% (Max 15%).", new HauntingThrust()),
    OPPORTUNIST_STRIKE("OPPORTUNIST_STRIKE", "Frappe Opportuniste", "Inflige +12% de dégâts si vous avez été touché durant les 2 dernières secondes.", new OpportunistStrike()),

    // --- DEFENSE ---
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