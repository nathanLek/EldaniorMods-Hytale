package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.skills.skills.passives.Common.Agilité.*;
import com.eldanior.system.skills.skills.passives.Common.Attack.*;
import com.eldanior.system.skills.skills.passives.Common.Defense.*;
import com.eldanior.system.skills.skills.passives.Common.Detection.*;
import com.eldanior.system.skills.skills.passives.Common.Endurance.*;
import com.eldanior.system.skills.skills.passives.Common.Magique.*;
import com.eldanior.system.skills.skills.passives.Common.Chance.*;
import com.eldanior.system.skills.skills.passives.Common.Resistance.*;
import com.eldanior.system.skills.skills.passives.Common.Vie.*;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.ActiveBreathing;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.CellularRegeneration;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.ManaFont;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.NaturalRecovery;
import com.eldanior.system.skills.skills.passives.Common.Regeneration.SpiritualSiphon;
import com.eldanior.system.skills.skills.passives.Divin.Attack.*;
import com.eldanior.system.skills.skills.passives.Divin.Defense.*;
import com.eldanior.system.skills.skills.passives.Divin.Agilite.*;
import com.eldanior.system.skills.skills.passives.Divin.Detection.*;
import com.eldanior.system.skills.skills.passives.Divin.Endurance.*;
import com.eldanior.system.skills.skills.passives.Divin.Magique.*;
import com.eldanior.system.skills.skills.passives.Divin.Chance.*;
import com.eldanior.system.skills.skills.passives.Divin.Regeneration.*;
import com.eldanior.system.skills.skills.passives.Divin.Resistance.*;
import com.eldanior.system.skills.skills.passives.Divin.Vie.*;
import com.eldanior.system.skills.skills.passives.Divin.Maitrise.*;
import com.eldanior.system.skills.skills.passives.Epique.Attack.*;
import com.eldanior.system.skills.skills.passives.Epique.Defense.*;
import com.eldanior.system.skills.skills.passives.Epique.Agilité.*;
import com.eldanior.system.skills.skills.passives.Epique.Detection.*;
import com.eldanior.system.skills.skills.passives.Epique.Endurance.*;
import com.eldanior.system.skills.skills.passives.Epique.Magique.*;
import com.eldanior.system.skills.skills.passives.Epique.Chance.*;
import com.eldanior.system.skills.skills.passives.Epique.Regeneration.*;
import com.eldanior.system.skills.skills.passives.Epique.Resistance.*;
import com.eldanior.system.skills.skills.passives.Epique.Vie.*;
import com.eldanior.system.skills.skills.passives.Epique.Maitrise.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Attack.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Defense.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Agilite.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Detection.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Endurance.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Magique.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Chance.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Regeneration.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Resistance.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Vie.*;
import com.eldanior.system.skills.skills.passives.Legendaire.Maitrise.*;
import com.eldanior.system.skills.skills.passives.Rare.Attack.*;
import com.eldanior.system.skills.skills.passives.Rare.Agilité.*;
import com.eldanior.system.skills.skills.passives.Rare.Detection.*;
import com.eldanior.system.skills.skills.passives.Rare.Endurance.*;
import com.eldanior.system.skills.skills.passives.Rare.Magique.*;
import com.eldanior.system.skills.skills.passives.Rare.Chance.*;
import com.eldanior.system.skills.skills.passives.Rare.Regeneration.*;
import com.eldanior.system.skills.skills.passives.Rare.Resistance.*;
import com.eldanior.system.skills.skills.passives.Rare.Vie.*;
import com.eldanior.system.skills.skills.passives.Rare.Defense.*;
import com.eldanior.system.skills.skills.passives.Rare.Maitrise.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Attack.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Defense.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Agilité.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Detection.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Endurance.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Magique.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Chance.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Regeneration.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Resistance.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Vie.*;
import com.eldanior.system.skills.skills.passives.Uncommon.Maitrise.*;
import com.eldanior.system.skills.skills.passives.Unique.Attack.*;
import com.eldanior.system.skills.skills.passives.Unique.Defense.*;
import com.eldanior.system.skills.skills.passives.Unique.Agilité.*;
import com.eldanior.system.skills.skills.passives.Unique.Detection.*;
import com.eldanior.system.skills.skills.passives.Unique.Endurance.*;
import com.eldanior.system.skills.skills.passives.Unique.Magique.*;
import com.eldanior.system.skills.skills.passives.Unique.Chance.*;
import com.eldanior.system.skills.skills.passives.Unique.Regeneration.*;
import com.eldanior.system.skills.skills.passives.Unique.Resistance.*;
import com.eldanior.system.skills.skills.passives.Unique.Vie.*;
import com.eldanior.system.skills.skills.passives.Unique.Maitrise.*;

public enum PassiveSkill {

    // --- GUERRIER ---
    SWORD_MASTERY("SWORD_MASTERY", "Maîtrise de l'Épée", "Augmente les dégâts infligés avec une épée de 15%.", new SwordMastery()),
    ATHLETICISM("ATHLETICISM", "Athlétisme", "Réduit la consommation d'endurance lors du sprint.", new Athleticism()),
    MANAWELL("MANAWELL", "Puit de Mana", "Augmente du mana à vie de 10%.", new ManaWell()),
    DETECTIONOFVITALPOINTS("DETECTIONOFVITALPOINTS", "Detection De Point Vital", "+15% fixes ajoutés à ta stat critique", 12, new DetectionOfVitalPoints()),

    // --- DÉTECTION (pas de coût mana) ---
    UNIVERSAL_DETECTION("UNIVERSAL_DETECTION", "Detection Universel", "+400% Conscience des menaces.", new UniversalDetection()),
    EAGLE_EYE("EAGLE_EYE", "Œil de Rapace", "+15% Distance de détection.", new EagleEye()),
    SURVIVAL_INSTINCT("SURVIVAL_INSTINCT", "Instinct de Survie", "+10% Conscience des menaces.", new SurvivalInstinct()),
    NIGHT_VISION("NIGHT_VISION", "Vision Nocturne", "+20% Visibilité en basse lumière.", new NightVision()),
    SIXTH_SENSE("SIXTH_SENSE", "Sixième Sens", "+5% Détection de l'invisibilité.", new SixthSense()),
    TRACKER("TRACKER", "Pisteur", "+25% Visibilité des traces de mobs.", new Tracker()),

    // --- ATTACK (coût mana selon la puissance) ---
    KODA_JUDGMENT("KODA_JUDGMENT", "Jugement de Koda", "12% de chance d'exécution au premier coup (Endurance >= 90%). Coût: 25 mana.", 25, new KodaJudgment()),
    MAUVAIS_PRESAGE("MAUVAIS_PRESAGE", "Mauvais Présage", "10% de chance d'infliger 20% des PV actuels de la cible en dégâts bonus. Coût: 18 mana.", 18, new MauvaisPresage()),

    INSTINCTIVE_STRIKE("INSTINCTIVE_STRIKE", "Frappe Instinctive", "5% de chances d'infliger 10% de dégâts bonus.", new InstinctiveStrike()),
    PREDATORY_STRIKE("PREDATORY_STRIKE", "Frappe de Prédateur", "10% de chances d'infliger 15% de dégâts bonus et de voler de la vie. Coût: 5 mana.", 5, new PredatoryStrike()),
    FURY_STRIKE("FURY_STRIKE", "Frappe de Fureur", "12% de chances d'infliger +25% dégâts (30% sur ennemis faibles). Coût: 8 mana.", 8, new FuryStrike()),
    SEISMIC_STRIKE("SEISMIC_STRIKE", "Frappe Sismique", "15% de chances d'infliger +35% dégâts et de créer une onde de choc. Coût: 12 mana.", 12, new SeismicStrike()),
    PHANTOM_STRIKE("PHANTOM_STRIKE", "Frappe Fantôme", "18% de chances d'infliger +50% de dégâts dévastateurs. Coût: 18 mana.", 18, new PhantomStrike()),
    ANNIHILATOR_STRIKE("ANNIHILATOR_STRIKE", "Frappe de l'Annihilateur", "22% de chances d'infliger +75% de dégâts. Chance de Double Impact. Coût: 25 mana.", 25, new AnnihilatorStrike()),
    JUDGMENT_OF_GENESIS("JUDGMENT_OF_GENESIS", "Décret de la Genèse", "30% de chances de +150% dégâts. Annihile instantanément les faibles. Coût: 40 mana.", 40, new JudgmentOfGenesis()),

    // --- ENDURANCE (pas de coût mana) ---
    TIRELESS_BREATH("TIRELESS_BREATH", "Souffle Inépuisable", "+10 Endurance maximale.", new TirelessBreath()),
    THICK_SKIN("THICK_SKIN", "Peau Épaisse", "+5 à la Défense d'Endurance.", new ThickSkin()),
    SOLID_STANCE("SOLID_STANCE", "Posture Solide", "Réduit les dégâts de 10% si l'Endurance est >= 80%.", new SolidStance()),
    COMBAT_VIGOR("COMBAT_VIGOR", "Vigueur Combative", "20% de chance de restaurer 5 d'Endurance en attaquant.", new CombatVigor()),
    SECOND_WIND("SECOND_WIND", "Second Souffle", "15% de chance de restaurer 25 d'Endurance en subissant un coup (si Endurance < 20%).", new SecondWind()),

    // --- MAGIQUE (coût mana pour les offensifs) ---
    AWAKENED_MIND("AWAKENED_MIND", "Esprit Éveillé", "+15 Intelligence.", new AwakenedMind()),
    ARCANE_STRIKE("ARCANE_STRIKE", "Frappe Arcanique", "20% de chance d'infliger 8 dégâts arcaniques supplémentaires. Coût: 10 mana.", 10, new ArcaneStrike()),
    MANA_BARRIER("MANA_BARRIER", "Barrière de Mana", "Réduit les dégâts subis de 10% si votre Mana est >= 50%.", new ManaBarrier()),
    OVERFLOWING_POWER("OVERFLOWING_POWER", "Puissance Débordante", "Vos attaques infligent +15% de dégâts si votre Mana est à 100%.", new OverflowingPower()),
    MYSTIC_VEIL("MYSTIC_VEIL", "Voile Mystique", "15% de chance de réduire les dégâts subis de 25%.", new MysticVeil()),
    VOL("VOL", "Vol", "Capacité de vol (1 mana/s).", new Fly()),

    // --- REGENERATION (pas de coût mana) ---
    CELLULAR_REGENERATION("CELLULAR_REGENERATION", "Régénération Cellulaire", "15% de chance de restaurer 5% de vos PV Max en subissant une attaque.", new CellularRegeneration()),
    ACTIVE_BREATHING("ACTIVE_BREATHING", "Respiration Active", "20% de chance de restaurer 8 points d'Endurance par attaque.", new ActiveBreathing()),
    SPIRITUAL_SIPHON("SPIRITUAL_SIPHON", "Siphon Spirituel", "20% de chance de restaurer 5 points de Mana par attaque.", new SpiritualSiphon()),
    MANA_FONT("MANA_FONT", "Source de Mana", "Multiplie la régénération naturelle de Mana par 1.5.", new ManaFont()),
    NATURAL_RECOVERY("NATURAL_RECOVERY", "Rétablissement Naturel", "Restaure 1% des PV max si pas touché depuis 5 secondes.", new NaturalRecovery()),
    MANA_HEART("MANA_HEART", "Coeur de Mana", "Multiplie la régénération naturelle de Mana par 10.5.", new ManaHeart()),

    // --- AGILITÉ (pas de coût mana) ---
    WIND_STEP("WIND_STEP", "Pas de Vent", "+3% Vitesse de déplacement.", new WindStep()),
    LIGHT_REFLEXES("LIGHT_REFLEXES", "Réflexes Éclairs", "+4% Vitesse d'attaque.", new LightReflexes()),
    ELDANIOR_SUPPLENESS("ELDANIOR_SUPPLENESS", "Souplesse d'Eldanior", "+10% Hauteur de saut.", new EldaniorSuppleness()),
    KEEN_SENSES("KEEN_SENSES", "Sens Aiguisés", "+2% Chance de Critique.", new KeenSenses()),

    TONOSQUIVE("TONOSQUIVE", "Tono'squive", "Augmente vos chances d'esquive de 30%.", new Tonosquive()),

    // --- CHANCE ---
    LUCKY_STRIKE("LUCKY_STRIKE", "Frappe Chanceuse", "+3% de chance de critique.", new LuckyStrike()),
    TREASURE_HUNTER("TREASURE_HUNTER", "Chasseur de Trésors", "+5% de chance de loot.", new TreasureHunter()),
    GOOD_OMEN("GOOD_OMEN", "Bon Présage", "+15% de chance d'event rare.", new GoodOmen()),
    MIRACLE_DODGE("MIRACLE_DODGE", "Esquive Miraculeuse", "3% de chance d'esquiver n'importe quelle attaque.", new MiracleDodge()),
    FORTUNE_COINS("FORTUNE_COINS", "Fortune Dorée", "+10% de coins droppés par les mobs.", new FortuneCoins()),

    ARTISANAT("ARTISANAT", "Artisanat de base", "Permet d'utiliser les établis et les forges.", new Artisanat()),

    // --- COMMON ATTACK (gratuit, coût 0) ---
    DEEP_SLASH("DEEP_SLASH", "Entaille Profonde", "Ajoute +1 points de dégâts physiques à chaque attaque.", new DeepSlash()),
    DUELIST_SWIFTNESS("DUELIST_SWIFTNESS", "Vivacité du Duelliste", "Augmente la vitesse d'attaque de 5%.", new DuelistSwiftness()),
    PRESSURE_POINT("PRESSURE_POINT", "Point de Pression", "Le premier coup sur une cible en pleine santé inflige 15% de dégâts bonus.", new PressurePoint()),
    HAUNTING_THRUST("HAUNTING_THRUST", "Estocade Obsédante", "Chaque coup consécutif sur la même cible augmente vos dégâts de 3% (Max 15%).", new HauntingThrust()),
    OPPORTUNIST_STRIKE("OPPORTUNIST_STRIKE", "Frappe Opportuniste", "Inflige +12% de dégâts si vous avez été touché durant les 2 dernières secondes.", new OpportunistStrike()),

    // --- DEFENSE (pas de coût mana) ---
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
    STURDY_BODY("STURDY_BODY","Corps Robuste", "Augmente l'Endurance de 15 points.", new SturdyBody()),

    // --- RÉSISTANCE (pas de coût mana) ---
    TENACITY("TENACITY", "Ténacité", "Sous 30% de vie, réduit les dégâts reçus de 8%.", new Tenacity()),
    ADAPTIVE_SHIELD("ADAPTIVE_SHIELD", "Bouclier Adaptatif", "10% de chance de réduire un coup de 20%.", new AdaptiveShield()),
    PAIN_TOLERANCE("PAIN_TOLERANCE", "Tolérance à la Douleur", "Réduit les dégâts de 2 points fixes sur chaque coup.", new PainTolerance()),
    HARDENING("HARDENING", "Endurcissement", "Réduit tous les dégâts reçus de 4%.", new Hardening()),
    STEEL_NERVES("STEEL_NERVES", "Nerfs d'Acier", "Les coups au-dessus de 15 dégâts sont réduits de 10%.", new SteelNerves()),

    // --- VIE (pas de coût mana) ---
    ROBUST_CONSTITUTION("ROBUST_CONSTITUTION", "Constitution Robuste", "+5 à la Vitalité.", new RobustConstitution()),
    LIFE_FORCE("LIFE_FORCE", "Force Vitale", "+5% de vie maximale.", new LifeForce()),
    VITAL_BLOOD("VITAL_BLOOD", "Sang Vital", "+3 Vitalité et +10% régénération de vie.", new VitalBlood()),
    HEART_OF_OAK("HEART_OF_OAK", "Coeur de Chêne", "+4 Vitalité et +2 Défense d'Endurance.", new HeartOfOak()),
    PERSEVERANCE("PERSEVERANCE", "Persévérance", "Sous 25% de vie, chaque coup reçu restaure 2% de vie max.", new Perseverance()),

    // ===================== UNCOMMON =====================

    // --- UNCOMMON ATTACK ---
    SHARP_BLADE("SHARP_BLADE", "Lame Aiguisée", "+2 dégâts physiques par attaque.", new SharpBlade()),
    COMBATANT_SWIFTNESS("COMBATANT_SWIFTNESS", "Vivacité du Combattant", "+15% vitesse d'attaque.", new CombatantSwiftness()),
    VITAL_PRESSURE("VITAL_PRESSURE", "Pression Vitale", "Premier coup sur cible pleine vie : +25% dégâts.", new VitalPressure()),
    RELENTLESS_HUNT("RELENTLESS_HUNT", "Traque Implacable", "+5% par coup consécutif, max 25%.", new RelentlessHunt()),

    // --- UNCOMMON DEFENSE ---
    STEEL_RESOLVE("STEEL_RESOLVE", "Résolution d'Acier", "Réduit tous les dégâts subis de 5.", new SteelResolve()),
    EXPERT_PARRY("EXPERT_PARRY", "Parade Experte", "20% de chance de bloquer 20% des dégâts.", new ExpertParry()),
    BEAST_GUARD("BEAST_GUARD", "Garde Bestiale", "Réduit les dégâts des monstres de 15%.", new BeastGuard()),
    IRON_BODY("IRON_BODY", "Corps de Fer", "+25 Défense d'Endurance.", new IronBody()),

    // --- UNCOMMON AGILITÉ ---
    GALE_STEP("GALE_STEP", "Pas de Bourrasque", "+5% vitesse de déplacement.", new GaleStep()),
    THUNDER_REFLEXES("THUNDER_REFLEXES", "Réflexes Foudroyants", "+7% vitesse d'attaque.", new ThunderReflexes()),
    CATLIKE_POISE("CATLIKE_POISE", "Souplesse Féline", "+18% hauteur de saut.", new CatlikePoise()),
    RAZOR_SENSES("RAZOR_SENSES", "Sens Acérés", "+4% chance critique.", new RazorSenses()),
    MARATHON_RUNNER("MARATHON_RUNNER", "Coureur de Marathon", "+8% vitesse + +10 Endurance max.", new MarathonRunner()),

    // --- UNCOMMON DETECTION ---
    HAWK_EYE("HAWK_EYE", "Oeil de Faucon", "+25% distance de détection.", new HawkEye()),
    DANGER_SENSE("DANGER_SENSE", "Sens du Danger", "+80% conscience des menaces.", new DangerSense()),
    DARK_VISION("DARK_VISION", "Vision des Ténèbres", "+40% visibilité basse lumière.", new DarkVision()),
    PSYCHIC_AWARENESS("PSYCHIC_AWARENESS", "Perception Psychique", "+10% détection invisibilité.", new PsychicAwareness()),
    MASTER_TRACKER("MASTER_TRACKER", "Pisteur Expert", "+50% visibilité des traces.", new MasterTracker()),

    // --- UNCOMMON ENDURANCE ---
    LUNGS_OF_STEEL("LUNGS_OF_STEEL", "Poumons d'Acier", "+20 Endurance maximale.", new LungsOfSteel()),
    ARMORED_SKIN("ARMORED_SKIN", "Peau Blindée", "+10 Défense d'Endurance.", new ArmoredSkin()),
    UNMOVABLE_MOUNTAIN("UNMOVABLE_MOUNTAIN", "Montagne Inébranlable", "-15% dégâts si Endurance >= 70%.", new UnmovableMountain()),
    BATTLE_FRENZY("BATTLE_FRENZY", "Frénésie de Combat", "30% chance +8 Endurance en attaquant.", new BattleFrenzy()),
    SURVIVOR_SPIRIT("SURVIVOR_SPIRIT", "Esprit du Survivant", "20% chance +40 Endurance si < 25%.", new SurvivorSpirit()),

    // --- UNCOMMON MAGIQUE ---
    EXPANDED_MIND("EXPANDED_MIND", "Esprit Élargi", "+25 Intelligence.", new ExpandedMind()),
    SPELLBLADE("SPELLBLADE", "Lame Ensorcelée", "25% chance +12 dégâts arcaniques. Coût: 14 mana.", 14, new Spellblade()),
    ARCANE_SHIELD("ARCANE_SHIELD", "Bouclier Arcanique", "-15% dégâts si Mana >= 40%.", new ArcaneShield()),
    UNLEASHED_MAGIC("UNLEASHED_MAGIC", "Magie Déchaînée", "+25% dégâts si Mana >= 80%.", new UnleashedMagic()),
    ASTRAL_CLOAK("ASTRAL_CLOAK", "Cape Astrale", "20% chance de -35% dégâts.", new AstralCloak()),

    // --- UNCOMMON CHANCE ---
    CRITICAL_LUCK("CRITICAL_LUCK", "Chance Critique", "+5% chance critique.", new CriticalLuck()),
    RELIC_HUNTER("RELIC_HUNTER", "Chasseur de Reliques", "+10% chance de loot.", new RelicHunter()),
    FATED_OMEN("FATED_OMEN", "Présage du Destin", "+25% chance d'event rare.", new FatedOmen()),
    PHANTOM_DODGE("PHANTOM_DODGE", "Esquive Fantôme", "5% chance d'esquive totale.", new PhantomDodge()),
    GOLDEN_TOUCH("GOLDEN_TOUCH", "Toucher d'Or", "+20% coins des mobs.", new GoldenTouch()),

    // --- UNCOMMON REGENERATION ---
    TROLL_BLOOD("TROLL_BLOOD", "Sang de Troll", "20% chance +8% vie max quand touché.", new TrollBlood()),
    IRON_LUNGS("IRON_LUNGS", "Poumons de Fer", "30% chance +12 Endurance par attaque.", new IronLungs()),
    SOUL_STEALER("SOUL_STEALER", "Voleur d'Âmes", "25% chance +8 Mana par attaque.", new SoulStealer()),
    MANA_STREAM("MANA_STREAM", "Flux de Mana", "x2.0 régénération naturelle de Mana.", new ManaStream()),
    VITAL_RECOVERY("VITAL_RECOVERY", "Rétablissement Vital", "x2.0 régénération naturelle de vie.", new VitalRecovery()),

    // --- UNCOMMON RÉSISTANCE ---
    UNYIELDING("UNYIELDING", "Inflexible", "Sous 35% vie, -12% dégâts reçus.", new Unyielding()),
    REACTIVE_BULWARK("REACTIVE_BULWARK", "Rempart Réactif", "15% chance de -30% dégâts.", new ReactiveBulwark()),
    BATTLE_SCARS("BATTLE_SCARS", "Cicatrices de Guerre", "-4 dégâts fixes par coup.", new BattleScars()),
    FORTIFICATION("FORTIFICATION", "Fortification", "-7% dégâts reçus permanent.", new Fortification()),
    IRON_WILL("IRON_WILL", "Volonté de Fer", "Coups >12 dégâts réduits de 15%.", new IronWill()),

    // --- UNCOMMON VIE ---
    STEEL_CONSTITUTION("STEEL_CONSTITUTION", "Constitution d'Acier", "+10 Vitalité.", new SteelConstitution()),
    OVERFLOWING_LIFE("OVERFLOWING_LIFE", "Vie Débordante", "+10% vie maximale.", new OverflowingLife()),
    ENRICHED_BLOOD("ENRICHED_BLOOD", "Sang Enrichi", "+6 Vitalité + +20% regen vie.", new EnrichedBlood()),
    HEART_OF_IRON("HEART_OF_IRON", "Coeur de Fer", "+8 Vitalité + +4 Défense Endurance.", new HeartOfIron()),
    UNDYING("UNDYING", "Immortel", "Sous 30% vie, +4% vie max par coup reçu.", new Undying()),

    // --- UNCOMMON MAÎTRISE ---
    AXE_MASTERY("AXE_MASTERY", "Maîtrise de la Hache", "+10% dégâts avec une hache.", new AxeMastery()),
    BOW_MASTERY("BOW_MASTERY", "Maîtrise de l'Arc", "+10% dégâts avec un arc.", new BowMastery()),
    SPEAR_MASTERY("SPEAR_MASTERY", "Maîtrise de la Lance", "+10% dégâts avec une lance.", new SpearMastery()),
    DAGGER_MASTERY("DAGGER_MASTERY", "Maîtrise de la Dague", "+10% dégâts avec une dague.", new DaggerMastery()),
    MINOR_SWORD_MASTERY("MINOR_SWORD_MASTERY", "Initiation à l'Épée", "+8% dégâts avec une épée.", new MinorSwordMastery()),

    // ===================== RARE =====================

    // --- RARE ATTACK ---
    CRIMSON_BLADE("CRIMSON_BLADE", "Lame Pourpre", "+4 dégâts physiques par attaque.", new CrimsonBlade()),
    WARRIOR_SWIFTNESS("WARRIOR_SWIFTNESS", "Vivacité du Guerrier", "+20% vitesse d'attaque.", new WarriorSwiftness()),
    CRUSHING_PRESSURE("CRUSHING_PRESSURE", "Pression Écrasante", "+35% dégâts sur cible pleine vie.", new CrushingPressure()),
    BLOOD_HUNT("BLOOD_HUNT", "Traque Sanguinaire", "+7% par coup consécutif, max 35%.", new BloodHunt()),

    // --- RARE DEFENSE ---
    TITAN_RESOLVE("TITAN_RESOLVE", "Résolution de Titan", "-8 dégâts fixes par coup.", new TitanResolve()),
    MASTER_PARRY("MASTER_PARRY", "Parade de Maître", "25% chance bloquer 25% dégâts.", new MasterParry()),
    MONSTER_SLAYER_GUARD("MONSTER_SLAYER_GUARD", "Garde du Tueur", "-20% dégâts des monstres.", new MonsterSlayerGuard()),
    STEEL_BODY("STEEL_BODY", "Corps d'Acier", "+35 Défense Endurance.", new SteelBody()),

    // --- RARE AGILITÉ ---
    STORM_STEP("STORM_STEP", "Pas de Tempête", "+8% vitesse déplacement.", new StormStep()),
    LIGHTNING_REFLEXES("LIGHTNING_REFLEXES", "Réflexes de Foudre", "+10% vitesse d'attaque.", new LightningReflexes()),
    ACROBATIC_POISE("ACROBATIC_POISE", "Souplesse Acrobatique", "+25% hauteur saut.", new AcrobaticPoise()),
    DEADLY_PRECISION("DEADLY_PRECISION", "Précision Mortelle", "+6% chance critique.", new DeadlyPrecision()),

    // --- RARE DETECTION ---
    EAGLE_VISION("EAGLE_VISION", "Vision d'Aigle", "+35% distance détection.", new EagleVision()),
    COMBAT_INTUITION("COMBAT_INTUITION", "Intuition de Combat", "+120% conscience menaces.", new CombatIntuition()),
    ABYSSAL_VISION("ABYSSAL_VISION", "Vision Abyssale", "+60% visibilité basse lumière.", new AbyssalVision()),
    MIND_READER("MIND_READER", "Lecture Mentale", "+15% détection invisibilité.", new MindReader()),

    // --- RARE ENDURANCE ---
    DRAGON_LUNGS("DRAGON_LUNGS", "Poumons de Dragon", "+30 Endurance max.", new DragonLungs()),
    FORTIFIED_SKIN("FORTIFIED_SKIN", "Peau Fortifiée", "+15 Défense Endurance.", new FortifiedSkin()),
    LIVING_FORTRESS("LIVING_FORTRESS", "Forteresse Vivante", "-20% dégâts si Endurance >= 60%.", new LivingFortress()),
    WAR_FRENZY("WAR_FRENZY", "Frénésie Guerrière", "40% chance +12 Endurance en attaquant.", new WarFrenzy()),

    // --- RARE MAGIQUE ---
    BRILLIANT_MIND("BRILLIANT_MIND", "Esprit Brillant", "+35 Intelligence.", new BrilliantMind()),
    ARCANE_DEVASTATION("ARCANE_DEVASTATION", "Dévastation Arcanique", "30% chance +16 dégâts arcaniques. Coût: 18 mana.", 18, new ArcaneDevastation()),
    MANA_FORTRESS("MANA_FORTRESS", "Forteresse de Mana", "-20% dégâts si Mana >= 30%.", new ManaFortress()),
    PURE_MAGIC("PURE_MAGIC", "Magie Pure", "+35% dégâts si Mana >= 60%.", new PureMagic()),

    // --- RARE CHANCE ---
    DESTINY_STRIKE("DESTINY_STRIKE", "Frappe du Destin", "+8% critique.", new DestinyStrike()),
    ARTIFACT_HUNTER("ARTIFACT_HUNTER", "Chasseur d'Artefacts", "+15% loot.", new ArtifactHunter()),
    PROPHECY_OMEN("PROPHECY_OMEN", "Présage Prophétique", "+35% event rare.", new ProphecyOmen()),
    SHADOW_DODGE("SHADOW_DODGE", "Esquive de l'Ombre", "8% esquive totale.", new ShadowDodge()),

    // --- RARE REGENERATION ---
    HYDRA_BLOOD("HYDRA_BLOOD", "Sang d'Hydre", "25% chance +12% vie max quand touché.", new HydraBlood()),
    ADAMANTINE_LUNGS("ADAMANTINE_LUNGS", "Poumons d'Adamantine", "35% chance +16 Endurance par attaque.", new AdamantineLungs()),
    SPIRIT_DRAIN("SPIRIT_DRAIN", "Drain Spirituel", "30% chance +12 Mana par attaque.", new SpiritDrain()),
    MANA_RIVER("MANA_RIVER", "Rivière de Mana", "x3.0 regen Mana naturelle.", new ManaRiver()),

    // --- RARE RÉSISTANCE ---
    UNBREAKABLE("UNBREAKABLE", "Incassable", "Sous 40% vie, -16% dégâts.", new Unbreakable()),
    ADAMANTINE_BULWARK("ADAMANTINE_BULWARK", "Rempart d'Adamantine", "20% chance -40% dégâts.", new AdamantineBulwark()),
    WAR_VETERAN("WAR_VETERAN", "Vétéran de Guerre", "-6 dégâts fixes par coup.", new WarVeteran()),
    IRON_FORTIFICATION("IRON_FORTIFICATION", "Fortification de Fer", "-10% dégâts permanent.", new IronFortification()),

    // --- RARE VIE ---
    TITAN_CONSTITUTION("TITAN_CONSTITUTION", "Constitution de Titan", "+15 Vitalité.", new TitanConstitution()),
    BURSTING_LIFE("BURSTING_LIFE", "Vie Explosive", "+15% vie max.", new BurstingLife()),
    ANCIENT_BLOOD("ANCIENT_BLOOD", "Sang Ancestral", "+9 Vitalité + regen vie +30%.", new AncientBlood()),
    HEART_OF_STEEL("HEART_OF_STEEL", "Coeur d'Acier", "+12 Vitalité + +6 Défense Endurance.", new HeartOfSteel()),

    // --- RARE MAÎTRISE ---
    GREAT_AXE_MASTERY("GREAT_AXE_MASTERY", "Maîtrise de la Grande Hache", "+15% dégâts hache.", new GreatAxeMastery()),
    MARKSMAN_MASTERY("MARKSMAN_MASTERY", "Maîtrise du Tir", "+15% dégâts arc.", new MarksmanMastery()),
    HALBERD_MASTERY("HALBERD_MASTERY", "Maîtrise de la Hallebarde", "+15% dégâts lance.", new HalberdMastery()),
    SHADOW_BLADE_MASTERY("SHADOW_BLADE_MASTERY", "Maîtrise de la Lame d'Ombre", "+15% dégâts dague.", new ShadowBladeMastery()),

    // ===================== ÉPIQUE =====================

    // --- ÉPIQUE ATTACK ---
    VOID_BLADE("VOID_BLADE", "Lame du Néant", "+6 dégâts physiques par attaque.", new VoidBlade()),
    BERSERKER_SWIFTNESS("BERSERKER_SWIFTNESS", "Vivacité du Berserker", "+25% vitesse d'attaque.", new BerserkerSwiftness()),
    ANNIHILATING_PRESSURE("ANNIHILATING_PRESSURE", "Pression Annihilante", "+45% dégâts sur cible pleine vie.", new AnnihilatingPressure()),
    DEATH_HUNT("DEATH_HUNT", "Traque Mortelle", "+9% par coup consécutif, max 45%.", new DeathHunt()),

    // --- ÉPIQUE DEFENSE ---
    GOD_RESOLVE("GOD_RESOLVE", "Résolution Divine", "-12 dégâts fixes par coup.", new GodResolve()),
    PERFECT_PARRY("PERFECT_PARRY", "Parade Parfaite", "30% chance bloquer 30% dégâts.", new PerfectParry()),
    DRAGON_SLAYER_GUARD("DRAGON_SLAYER_GUARD", "Garde du Tueur de Dragon", "-25% dégâts des monstres.", new DragonSlayerGuard()),
    DIAMOND_BODY("DIAMOND_BODY", "Corps de Diamant", "+50 Défense Endurance.", new DiamondBody()),

    // --- ÉPIQUE AGILITÉ ---
    VOID_STEP("VOID_STEP", "Pas du Néant", "+12% vitesse déplacement.", new VoidStep()),
    DIVINE_REFLEXES("DIVINE_REFLEXES", "Réflexes Divins", "+14% vitesse d'attaque.", new DivineReflexes()),
    GRAVITY_DEFIANCE("GRAVITY_DEFIANCE", "Défi à la Gravité", "+35% hauteur saut.", new GravityDefiance()),
    FATAL_PRECISION("FATAL_PRECISION", "Précision Fatale", "+9% chance critique.", new FatalPrecision()),

    // --- ÉPIQUE DETECTION ---
    OMNISCIENT_VISION("OMNISCIENT_VISION", "Vision Omnisciente", "+50% distance détection.", new OmniscientVision()),
    WAR_PROPHECY("WAR_PROPHECY", "Prophétie de Guerre", "+180% conscience menaces.", new WarProphecy()),
    VOID_SIGHT("VOID_SIGHT", "Vue du Néant", "+80% visibilité basse lumière.", new VoidSight()),
    SOUL_READER("SOUL_READER", "Lecture d'Âme", "+20% détection invisibilité.", new SoulReader()),

    // --- ÉPIQUE ENDURANCE ---
    TITAN_LUNGS("TITAN_LUNGS", "Poumons de Titan", "+40 Endurance max.", new TitanLungs()),
    ADAMANTINE_SKIN("ADAMANTINE_SKIN", "Peau d'Adamantine", "+20 Défense Endurance.", new AdamantineSkin()),
    ETERNAL_FORTRESS("ETERNAL_FORTRESS", "Forteresse Éternelle", "-25% dégâts si Endurance >= 50%.", new EternalFortress()),
    BLOOD_FRENZY("BLOOD_FRENZY", "Frénésie Sanguinaire", "50% chance +16 Endurance en attaquant.", new BloodFrenzy()),

    // --- ÉPIQUE MAGIQUE ---
    GENIUS_MIND("GENIUS_MIND", "Esprit de Génie", "+50 Intelligence.", new GeniusMind()),
    ARCANE_ANNIHILATION("ARCANE_ANNIHILATION", "Annihilation Arcanique", "35% chance +22 dégâts arcaniques. Coût: 22 mana.", 22, new ArcaneAnnihilation()),
    MANA_CITADEL("MANA_CITADEL", "Citadelle de Mana", "-25% dégâts si Mana >= 20%.", new ManaCitadel()),
    ARCANE_SUPREMACY("ARCANE_SUPREMACY", "Suprématie Arcanique", "+45% dégâts si Mana >= 50%.", new ArcaneSupremacy()),

    // --- ÉPIQUE CHANCE ---
    DIVINE_STRIKE("DIVINE_STRIKE", "Frappe Divine", "+12% critique.", new DivineStrike()),
    LEGEND_HUNTER("LEGEND_HUNTER", "Chasseur de Légendes", "+20% loot.", new LegendHunter()),
    COSMIC_OMEN("COSMIC_OMEN", "Présage Cosmique", "+50% event rare.", new CosmicOmen()),
    DIMENSIONAL_DODGE("DIMENSIONAL_DODGE", "Esquive Dimensionnelle", "12% esquive totale.", new DimensionalDodge()),

    // --- ÉPIQUE REGENERATION ---
    PHOENIX_BLOOD("PHOENIX_BLOOD", "Sang de Phénix", "30% chance +16% vie max quand touché.", new PhoenixBlood()),
    MYTHRIL_LUNGS("MYTHRIL_LUNGS", "Poumons de Mythril", "40% chance +20 Endurance par attaque.", new MythrilLungs()),
    ARCANE_VAMPIRISM("ARCANE_VAMPIRISM", "Vampirisme Arcanique", "35% chance +16 Mana par attaque.", new ArcaneVampirism()),
    MANA_OCEAN("MANA_OCEAN", "Océan de Mana", "x4.0 regen Mana naturelle.", new ManaOcean()),

    // --- ÉPIQUE RÉSISTANCE ---
    INVINCIBLE("INVINCIBLE", "Invincible", "Sous 45% vie, -20% dégâts.", new Invincible()),
    DIVINE_BULWARK("DIVINE_BULWARK", "Rempart Divin", "25% chance -50% dégâts.", new DivineBulwark()),
    WAR_LEGEND("WAR_LEGEND", "Légende de Guerre", "-8 dégâts fixes par coup.", new WarLegend()),
    MYTHRIL_FORTIFICATION("MYTHRIL_FORTIFICATION", "Fortification de Mythril", "-13% dégâts permanent.", new MythrilFortification()),

    // --- ÉPIQUE VIE ---
    GOD_CONSTITUTION("GOD_CONSTITUTION", "Constitution Divine", "+20 Vitalité.", new GodConstitution()),
    ETERNAL_LIFE("ETERNAL_LIFE", "Vie Éternelle", "+20% vie max.", new EternalLife()),
    DRAGON_BLOOD("DRAGON_BLOOD", "Sang de Dragon", "+12 Vitalité + regen vie +40%.", new DragonBlood()),
    HEART_OF_DIAMOND("HEART_OF_DIAMOND", "Coeur de Diamant", "+16 Vitalité + +8 Défense Endurance.", new HeartOfDiamond()),

    // --- ÉPIQUE MAÎTRISE ---
    WAR_AXE_MASTERY("WAR_AXE_MASTERY", "Maîtrise de la Hache de Guerre", "+20% dégâts hache.", new WarAxeMastery()),
    SNIPER_MASTERY("SNIPER_MASTERY", "Maîtrise du Sniper", "+20% dégâts arc.", new SniperMastery()),
    DRAGON_SPEAR_MASTERY("DRAGON_SPEAR_MASTERY", "Maîtrise de la Lance Dragon", "+20% dégâts lance.", new DragonSpearMastery()),
    ASSASSIN_BLADE_MASTERY("ASSASSIN_BLADE_MASTERY", "Maîtrise de la Lame d'Assassin", "+20% dégâts dague.", new AssassinBladeMastery()),

    // ===================== UNIQUE =====================

    // --- UNIQUE ATTACK ---
    ABYSS_BLADE("ABYSS_BLADE", "Lame de l'Abîme", "+9 dégâts physiques par attaque.", new AbyssBlade()),
    DEMIGOD_SWIFTNESS("DEMIGOD_SWIFTNESS", "Vivacité du Demi-Dieu", "+30% vitesse d'attaque.", new DemigodSwiftness()),
    SOUL_CRUSHING_PRESSURE("SOUL_CRUSHING_PRESSURE", "Pression Brise-Âme", "+55% dégâts sur cible pleine vie.", new SoulCrushingPressure()),

    // --- UNIQUE DEFENSE ---
    IMMORTAL_RESOLVE("IMMORTAL_RESOLVE", "Résolution Immortelle", "-16 dégâts fixes par coup.", new ImmortalResolve()),
    DIVINE_PARRY("DIVINE_PARRY", "Parade Divine", "35% chance bloquer 35% dégâts.", new DivineParry()),
    COSMIC_BODY("COSMIC_BODY", "Corps Cosmique", "+70 Défense Endurance.", new CosmicBody()),

    // --- UNIQUE AGILITÉ ---
    DIMENSIONAL_STEP("DIMENSIONAL_STEP", "Pas Dimensionnel", "+16% vitesse déplacement.", new DimensionalStep()),
    COSMIC_REFLEXES("COSMIC_REFLEXES", "Réflexes Cosmiques", "+18% vitesse d'attaque.", new CosmicReflexes()),
    ABSOLUTE_PRECISION("ABSOLUTE_PRECISION", "Précision Absolue", "+12% chance critique.", new AbsolutePrecision()),

    // --- UNIQUE DETECTION ---
    ALL_SEEING_EYE("ALL_SEEING_EYE", "Oeil Omniscient", "+70% distance détection.", new AllSeeingEye()),
    FATE_VISION("FATE_VISION", "Vision du Destin", "+250% conscience menaces.", new FateVision()),
    TRUE_SIGHT("TRUE_SIGHT", "Vision Véritable", "+25% détection invisibilité.", new TrueSight()),

    // --- UNIQUE ENDURANCE ---
    COSMIC_LUNGS("COSMIC_LUNGS", "Poumons Cosmiques", "+55 Endurance max.", new CosmicLungs()),
    DIVINE_FORTRESS("DIVINE_FORTRESS", "Forteresse Divine", "-30% dégâts si Endurance >= 40%.", new DivineFortress()),
    RAGE_FRENZY("RAGE_FRENZY", "Frénésie Enragée", "60% chance +20 Endurance en attaquant.", new RageFrenzy()),

    // --- UNIQUE MAGIQUE ---
    COSMIC_MIND("COSMIC_MIND", "Esprit Cosmique", "+70 Intelligence.", new CosmicMind()),
    ARCANE_OBLIVION("ARCANE_OBLIVION", "Oubli Arcanique", "40% chance +30 dégâts arcaniques. Coût: 28 mana.", 28, new ArcaneOblivion()),
    ABSOLUTE_SUPREMACY("ABSOLUTE_SUPREMACY", "Suprématie Absolue", "+55% dégâts si Mana >= 40%.", new AbsoluteSupremacy()),

    // --- UNIQUE CHANCE ---
    COSMIC_STRIKE("COSMIC_STRIKE", "Frappe Cosmique", "+16% critique.", new CosmicStrike()),
    MYTH_HUNTER("MYTH_HUNTER", "Chasseur de Mythes", "+25% loot.", new MythHunter()),
    REALITY_DODGE("REALITY_DODGE", "Esquive de la Réalité", "16% esquive totale.", new RealityDodge()),

    // --- UNIQUE REGENERATION ---
    ETERNAL_BLOOD("ETERNAL_BLOOD", "Sang Éternel", "35% chance +20% vie max quand touché.", new EternalBlood()),
    COSMIC_VAMPIRISM("COSMIC_VAMPIRISM", "Vampirisme Cosmique", "40% chance +22 Mana par attaque.", new CosmicVampirism()),
    MANA_INFINITY("MANA_INFINITY", "Mana Infini", "x6.0 regen Mana naturelle.", new ManaInfinity()),

    // --- UNIQUE RÉSISTANCE ---
    IMMORTAL_ABSOLUTE("IMMORTAL_ABSOLUTE", "Immortel Absolu", "Sous 50% vie, -25% dégâts.", new Immortal()),
    COSMIC_BULWARK("COSMIC_BULWARK", "Rempart Cosmique", "30% chance -60% dégâts.", new CosmicBulwark()),
    ETERNAL_FORTIFICATION("ETERNAL_FORTIFICATION", "Fortification Éternelle", "-16% dégâts permanent.", new EternalFortification()),

    // --- UNIQUE VIE ---
    COSMIC_CONSTITUTION("COSMIC_CONSTITUTION", "Constitution Cosmique", "+25 Vitalité.", new CosmicConstitution()),
    INFINITE_LIFE("INFINITE_LIFE", "Vie Infinie", "+25% vie max.", new InfiniteLife()),
    HEART_OF_ETERNITY("HEART_OF_ETERNITY", "Coeur d'Éternité", "+20 Vitalité + +10 Défense Endurance.", new HeartOfEternity()),

    // --- UNIQUE MAÎTRISE ---
    LEGENDARY_AXE_MASTERY("LEGENDARY_AXE_MASTERY", "Maîtrise Légendaire de la Hache", "+25% dégâts hache.", new LegendaryAxeMastery()),
    LEGENDARY_SNIPER_MASTERY("LEGENDARY_SNIPER_MASTERY", "Maîtrise Légendaire du Tir", "+25% dégâts arc.", new LegendarySniperMastery()),
    LEGENDARY_DAGGER_MASTERY("LEGENDARY_DAGGER_MASTERY", "Maîtrise Légendaire de la Dague", "+25% dégâts dague.", new LegendaryDaggerMastery()),

    // ===================== LÉGENDAIRE =====================

    // --- LÉGENDAIRE ATTACK ---
    GENESIS_EDGE("GENESIS_EDGE", "Tranchant de la Genèse", "+12 dégâts physiques par attaque.", new GenesisEdge()),
    GOD_SLAYER_SWIFTNESS("GOD_SLAYER_SWIFTNESS", "Vivacité du Tueur de Dieux", "+35% vitesse d'attaque.", new GodSlayerSwiftness()),

    // --- LÉGENDAIRE DEFENSE ---
    ETERNITY_RESOLVE("ETERNITY_RESOLVE", "Résolution Éternelle", "-20 dégâts fixes par coup.", new EternityResolve()),
    CELESTIAL_PARRY("CELESTIAL_PARRY", "Parade Céleste", "40% chance bloquer 40% dégâts.", new CelestialParry()),

    // --- LÉGENDAIRE AGILITÉ ---
    CELESTIAL_STEP("CELESTIAL_STEP", "Pas Céleste", "+20% vitesse déplacement.", new CelestialStep()),
    OMNISCIENT_PRECISION("OMNISCIENT_PRECISION", "Précision Omnisciente", "+15% chance critique.", new OmniscientPrecision()),

    // --- LÉGENDAIRE DETECTION ---
    GENESIS_VISION("GENESIS_VISION", "Vision de la Genèse", "+90% distance détection.", new GenesisVision()),
    OMNI_SIGHT("OMNI_SIGHT", "Vision Totale", "+30% détection invisibilité.", new OmniSight()),

    // --- LÉGENDAIRE ENDURANCE ---
    CELESTIAL_LUNGS("CELESTIAL_LUNGS", "Poumons Célestes", "+70 Endurance max.", new CelestialLungs()),
    CELESTIAL_FORTRESS("CELESTIAL_FORTRESS", "Forteresse Céleste", "-35% dégâts si Endurance >= 30%.", new CelestialFortress()),

    // --- LÉGENDAIRE MAGIQUE ---
    INFINITE_MIND("INFINITE_MIND", "Esprit Infini", "+90 Intelligence.", new InfiniteMind()),
    ARCANE_GENESIS("ARCANE_GENESIS", "Genèse Arcanique", "45% chance +40 dégâts arcaniques. Coût: 35 mana.", 35, new ArcaneGenesis()),

    // --- LÉGENDAIRE CHANCE ---
    GENESIS_STRIKE("GENESIS_STRIKE", "Frappe de la Genèse", "+20% critique.", new GenesisStrike()),
    TIME_DODGE("TIME_DODGE", "Esquive Temporelle", "20% esquive totale.", new TimeDodge()),

    // --- LÉGENDAIRE REGENERATION ---
    GENESIS_BLOOD("GENESIS_BLOOD", "Sang de la Genèse", "40% chance +25% vie max quand touché.", new GenesisBlood()),
    INFINITE_VAMPIRISM("INFINITE_VAMPIRISM", "Vampirisme Infini", "45% chance +30 Mana par attaque.", new InfiniteVampirism()),

    // --- LÉGENDAIRE RÉSISTANCE ---
    CELESTIAL_IMMORTALITY("CELESTIAL_IMMORTALITY", "Immortalité Céleste", "Sous 55% vie, -30% dégâts.", new CelestialImmortality()),
    GENESIS_BULWARK("GENESIS_BULWARK", "Rempart de la Genèse", "35% chance -70% dégâts.", new GenesisBulwark()),

    // --- LÉGENDAIRE VIE ---
    CELESTIAL_CONSTITUTION("CELESTIAL_CONSTITUTION", "Constitution Céleste", "+30 Vitalité.", new CelestialConstitution()),
    HEART_OF_GENESIS("HEART_OF_GENESIS", "Coeur de la Genèse", "+25 Vitalité + +12 Défense Endurance.", new HeartOfGenesis()),

    // --- LÉGENDAIRE MAÎTRISE ---
    DIVIN_AXE_MASTERY("DIVIN_AXE_MASTERY", "Maîtrise Divine de la Hache", "+30% dégâts hache.", new DivinAxeMastery()),
    DIVIN_DAGGER_MASTERY("DIVIN_DAGGER_MASTERY", "Maîtrise Divine de la Dague", "+30% dégâts dague.", new DivinDaggerMastery()),

    // ===================== DIVIN =====================

    // --- DIVIN ATTACK ---
    CREATOR_EDGE("CREATOR_EDGE", "Tranchant du Créateur", "+16 dégâts physiques par attaque.", new CreatorEdge()),
    CREATOR_SWIFTNESS("CREATOR_SWIFTNESS", "Vivacité du Créateur", "+40% vitesse d'attaque.", new CreatorSwiftness()),

    // --- DIVIN DEFENSE ---
    ABSOLUTE_RESOLVE("ABSOLUTE_RESOLVE", "Résolution Absolue", "-25 dégâts fixes par coup.", new AbsoluteResolve()),
    CREATOR_PARRY("CREATOR_PARRY", "Parade du Créateur", "45% chance bloquer 45% dégâts.", new CreatorParry()),

    // --- DIVIN AGILITÉ ---
    CREATOR_STEP("CREATOR_STEP", "Pas du Créateur", "+25% vitesse déplacement.", new CreatorStep()),
    CREATOR_PRECISION("CREATOR_PRECISION", "Précision du Créateur", "+18% chance critique.", new CreatorPrecision()),

    // --- DIVIN DETECTION ---
    CREATOR_VISION("CREATOR_VISION", "Vision du Créateur", "+120% distance détection.", new CreatorVision()),
    ABSOLUTE_SIGHT("ABSOLUTE_SIGHT", "Vue Absolue", "+40% détection invisibilité.", new AbsoluteSight()),

    // --- DIVIN ENDURANCE ---
    CREATOR_LUNGS("CREATOR_LUNGS", "Poumons du Créateur", "+90 Endurance max.", new CreatorLungs()),
    CREATOR_FORTRESS("CREATOR_FORTRESS", "Forteresse du Créateur", "-40% dégâts si Endurance >= 20%.", new CreatorFortress()),

    // --- DIVIN MAGIQUE ---
    CREATOR_MIND("CREATOR_MIND", "Esprit du Créateur", "+120 Intelligence.", new CreatorMind()),
    ARCANE_CREATION("ARCANE_CREATION", "Création Arcanique", "50% chance +50 dégâts arcaniques. Coût: 40 mana.", 40, new ArcaneCreation()),

    // --- DIVIN CHANCE ---
    CREATOR_STRIKE("CREATOR_STRIKE", "Frappe du Créateur", "+25% critique.", new CreatorStrike()),
    FATE_DODGE("FATE_DODGE", "Esquive du Destin", "25% esquive totale.", new FateDodge()),

    // --- DIVIN REGENERATION ---
    CREATOR_BLOOD("CREATOR_BLOOD", "Sang du Créateur", "45% chance +30% vie max quand touché.", new CreatorBlood()),
    CREATOR_VAMPIRISM("CREATOR_VAMPIRISM", "Vampirisme du Créateur", "50% chance +40 Mana par attaque.", new CreatorVampirism()),

    // --- DIVIN RÉSISTANCE ---
    TRUE_IMMORTALITY("TRUE_IMMORTALITY", "Immortalité Véritable", "Sous 60% vie, -35% dégâts.", new TrueImmortality()),
    CREATOR_BULWARK("CREATOR_BULWARK", "Rempart du Créateur", "40% chance -80% dégâts.", new CreatorBulwark()),

    // --- DIVIN VIE ---
    CREATOR_CONSTITUTION("CREATOR_CONSTITUTION", "Constitution du Créateur", "+40 Vitalité.", new CreatorConstitution()),
    HEART_OF_CREATION("HEART_OF_CREATION", "Coeur de la Création", "+30 Vitalité + +15 Défense Endurance.", new HeartOfCreation()),

    // --- DIVIN MAÎTRISE ---
    CREATOR_AXE_MASTERY("CREATOR_AXE_MASTERY", "Maîtrise Absolue de la Hache", "+35% dégâts hache.", new CreatorAxeMastery()),
    CREATOR_DAGGER_MASTERY("CREATOR_DAGGER_MASTERY", "Maîtrise Absolue de la Dague", "+35% dégâts dague.", new CreatorDaggerMastery());

    private final String id;
    private final String displayName;
    private final String description;
    private final int manaCost;

    // LA MAGIE EST ICI : Chaque Enum contient la logique de son propre combat
    private final IPassiveCombatSkill logic;

    PassiveSkill(String id, String displayName, String description, IPassiveCombatSkill logic) {
        this(id, displayName, description, 0, logic);
    }

    PassiveSkill(String id, String displayName, String description, int manaCost, IPassiveCombatSkill logic) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.manaCost = manaCost;
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

    public int getManaCost() {
        return manaCost;
    }

    public IPassiveCombatSkill getLogic() {
        return logic;
    }
}