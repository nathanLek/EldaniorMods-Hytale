package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.skills.skills.passives.Common.Agilité.*;
import com.eldanior.system.skills.skills.passives.Common.Attack.*;
import com.eldanior.system.skills.skills.passives.Common.Defense.*;
import com.eldanior.system.skills.skills.passives.Common.Detection.*;
import com.eldanior.system.skills.skills.passives.Common.Endurance.*;
import com.eldanior.system.skills.skills.passives.Common.Magique.*;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.ActiveBreathing;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.CellularRegeneration;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.ManaFont;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.SpiritualSiphon;
import com.eldanior.system.skills.skills.passives.Divin.Attack.JudgmentOfGenesis;
import com.eldanior.system.skills.skills.passives.Divin.Defense.DivineAegis;
import com.eldanior.system.skills.skills.passives.Divin.Defense.DynaAegis;
import com.eldanior.system.skills.skills.passives.Divin.Regeneration.ManaHeart;
import com.eldanior.system.skills.skills.passives.Epique.Attack.DetectionOfVitalPoints;
import com.eldanior.system.skills.skills.passives.Epique.Attack.SeismicStrike;
import com.eldanior.system.skills.skills.passives.Epique.Defense.SteelSkin;
import com.eldanior.system.skills.skills.passives.Epique.Detection.UniversalDetection;
import com.eldanior.system.skills.skills.passives.Epique.Magique.ManaWell;
import com.eldanior.system.skills.skills.passives.Epique.Maitrise.SwordMastery;
import com.eldanior.system.skills.skills.passives.Legendaire.Attack.AnnihilatorStrike;
import com.eldanior.system.skills.skills.passives.Legendaire.Attack.KodaJudgment;
import com.eldanior.system.skills.skills.passives.Legendaire.Defense.DiamondSkin;
import com.eldanior.system.skills.skills.passives.Legendaire.Magique.Fly;
import com.eldanior.system.skills.skills.passives.Rare.Attack.FuryStrike;
import com.eldanior.system.skills.skills.passives.Rare.Maitrise.Artisanat;
import com.eldanior.system.skills.skills.passives.Rare.Defense.IronSkin;
import com.eldanior.system.skills.skills.passives.Uncommon.Attack.PredatoryStrike;
import com.eldanior.system.skills.skills.passives.Uncommon.Defense.BronzeSkin;
import com.eldanior.system.skills.skills.passives.Unique.Agilité.Tonosquive;
import com.eldanior.system.skills.skills.passives.Unique.Attack.MauvaisPresage;
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
    KODA_JUDGMENT("KODA_JUDGMENT", "Jugement de Koda", "12% de chance d'exécution au premier coup (Endurance >= 90%).", new KodaJudgment()),
    MAUVAIS_PRESAGE("MAUVAIS_PRESAGE", "Mauvais Présage", "10% de chance d'infliger 20% des PV actuels de la cible en dégâts bonus.", new MauvaisPresage()),


    INSTINCTIVE_STRIKE("INSTINCTIVE_STRIKE", "Frappe Instinctive", "5% de chances d'infliger 10% de dégâts bonus.", new InstinctiveStrike()),
    PREDATORY_STRIKE("PREDATORY_STRIKE", "Frappe de Prédateur", "10% de chances d'infliger 15% de dégâts bonus et de voler de la vie.", new PredatoryStrike()),
    FURY_STRIKE("FURY_STRIKE", "Frappe de Fureur", "12% de chances d'infliger +25% dégâts (30% sur ennemis faibles).", new FuryStrike()),
    SEISMIC_STRIKE("SEISMIC_STRIKE", "Frappe Sismique", "15% de chances d'infliger +35% dégâts et de créer une onde de choc.", new SeismicStrike()),
    PHANTOM_STRIKE("PHANTOM_STRIKE", "Frappe Fantôme", "18% de chances d'infliger +50% de dégâts dévastateurs.", new PhantomStrike()),
    ANNIHILATOR_STRIKE("ANNIHILATOR_STRIKE", "Frappe de l'Annihilateur", "22% de chances d'infliger +75% de dégâts. Chance de Double Impact.", new AnnihilatorStrike()),
    JUDGMENT_OF_GENESIS("JUDGMENT_OF_GENESIS", "Décret de la Genèse", "30% de chances de +150% dégâts. Annihile instantanément les faibles.", new JudgmentOfGenesis()),

    // --- ENDURANCE ---
    TIRELESS_BREATH("TIRELESS_BREATH", "Souffle Inépuisable", "+10 Endurance maximale.", new TirelessBreath()),
    THICK_SKIN("THICK_SKIN", "Peau Épaisse", "+5 à la Défense d'Endurance.", new ThickSkin()),
    SOLID_STANCE("SOLID_STANCE", "Posture Solide", "Réduit les dégâts de 10% si l'Endurance est >= 80%.", new SolidStance()),
    COMBAT_VIGOR("COMBAT_VIGOR", "Vigueur Combative", "20% de chance de restaurer 5 d'Endurance en attaquant.", new CombatVigor()),
    SECOND_WIND("SECOND_WIND", "Second Souffle", "15% de chance de restaurer 25 d'Endurance en subissant un coup (si Endurance < 20%).", new SecondWind()),

    // --- MAGIQUE ---
    AWAKENED_MIND("AWAKENED_MIND", "Esprit Éveillé", "+15 Intelligence.", new AwakenedMind()),
    ARCANE_STRIKE("ARCANE_STRIKE", "Frappe Arcanique", "20% de chance d'infliger 8 dégâts arcaniques supplémentaires.", new ArcaneStrike()),
    MANA_BARRIER("MANA_BARRIER", "Barrière de Mana", "Réduit les dégâts subis de 10% si votre Mana est >= 50%.", new ManaBarrier()),
    OVERFLOWING_POWER("OVERFLOWING_POWER", "Puissance Débordante", "Vos attaques infligent +15% de dégâts si votre Mana est à 100%.", new OverflowingPower()),
    MYSTIC_VEIL("MYSTIC_VEIL", "Voile Mystique", "15% de chance de réduire les dégâts subis de 25%.", new MysticVeil()),
    VOL("VOL", "Vol", "Capacité de vol (1 mana/s).", new Fly()),

    // --- REGENERATION ---
    CELLULAR_REGENERATION("CELLULAR_REGENERATION", "Régénération Cellulaire", "15% de chance de restaurer 5% de vos PV Max en subissant une attaque.", new CellularRegeneration()),
    ACTIVE_BREATHING("ACTIVE_BREATHING", "Respiration Active", "20% de chance de restaurer 8 points d'Endurance par attaque.", new ActiveBreathing()),
    SPIRITUAL_SIPHON("SPIRITUAL_SIPHON", "Siphon Spirituel", "20% de chance de restaurer 5 points de Mana par attaque.", new SpiritualSiphon()),
    MANA_FONT("MANA_FONT", "Source de Mana", "Multiplie la régénération naturelle de Mana par 1.5.", new ManaFont()),
    MANA_HEART("MANA_HEART", "Coeur de Mana", "Multiplie la régénération naturelle de Mana par 10.5.", new ManaHeart()),

    WIND_STEP("WIND_STEP", "Pas de Vent", "+3% Vitesse de déplacement.", new WindStep()),
    LIGHT_REFLEXES("LIGHT_REFLEXES", "Réflexes Éclairs", "+4% Vitesse d'attaque.", new LightReflexes()),
    ELDANIOR_SUPPLENESS("ELDANIOR_SUPPLENESS", "Souplesse d'Eldanior", "+10% Hauteur de saut.", new EldaniorSuppleness()),
    KEEN_SENSES("KEEN_SENSES", "Sens Aiguisés", "+2% Chance de Critique.", new KeenSenses()),

    TONOSQUIVE("TONOSQUIVE", "Tono'squive", "Augmente vos chances d'esquive de 30%.", new Tonosquive()),

    ARTISANAT("ARTISANAT", "Artisanat de base", "Permet d'utiliser les établis et les forges.", new Artisanat()),

    DEEP_SLASH("DEEP_SLASH", "Entaille Profonde", "Ajoute +1 points de dégâts physiques à chaque attaque.", new DeepSlash()),
    DUELIST_SWIFTNESS("DUELIST_SWIFTNESS", "Vivacité du Duelliste", "Augmente la vitesse d'attaque de 5%.", new DuelistSwiftness()),
    PRESSURE_POINT("PRESSURE_POINT", "Point de Pression", "Le premier coup sur une cible en pleine santé inflige 15% de dégâts bonus.", new PressurePoint()),
    HAUNTING_THRUST("HAUNTING_THRUST", "Estocade Obsédante", "Chaque coup consécutif sur la même cible augmente vos dégâts de 3% (Max 15%).", new HauntingThrust()),
    OPPORTUNIST_STRIKE("OPPORTUNIST_STRIKE", "Frappe Opportuniste", "Inflige +12% de dégâts si vous avez été touché durant les 2 dernières secondes.", new OpportunistStrike()),

    // --- DEFENSE ---
    DYNA_AEGIS("DYNA_AEGIS", "Égide de Dyna", "Rend invincible 5s si la vie tombe à 50% (Cooldown: 2m).", new DynaAegis()),

    STONE_SKIN("STONE_SKIN", "Peau de Pierre", "Réduit les dégâts physiques reçus de 5%.", new StoneSkin()),
    BRONZE_SKIN("BRONZE_SKIN", "Peau de Bronze", "Réduit les dégâts physiques reçus de 8%.", new BronzeSkin()),
    IRON_SKIN("IRON_SKIN", "Peau de Fer", "Réduit les dégâts physiques reçus de 10%.", new IronSkin()),
    STEEL_SKIN("STEEL_SKIN", "Peau d'Acier", "Réduit les dégâts physiques reçus de 15%.", new SteelSkin()),
    OBSIDIAN_SKIN("OBSIDIAN_SKIN", "Peau d'Obsidienne", "Réduit les dégâts physiques reçus de 25%.", new ObsidianSkin()),
    DIAMOND_SKIN("DIAMOND_SKIN", "Peau de Diamant", "Réduit les dégâts physiques reçus de 35%.", new DiamondSkin()),
    DIVINE_AEGIS("DIVINE_AEGIS", "Égide Divine", "Réduit les dégâts physiques reçus de 50%.", new DivineAegis()),

    IRON_RESOLVE("IRON_RESOLVE","Résolution de Fer", "Réduit tous les dégâts subis de 3.", new IronResolve()),
    MINOR_PARRY("MINOR_PARRY","Parade Mineure", "15% de chance de bloquer 15% des dégâts.", new MinorParry()),
    HUNTER_GUARD("HUNTER_GUARD","Garde de Chasseur", "Réduit les dégâts infligés par les monstres de 10%.", new HunterGuard()),
    STURDY_BODY("STURDY_BODY","Corps Robuste", "Augmente l'Endurance de 15 points.", new SturdyBody());

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