package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.skills.skills.passives.Divin.Ange.*;
import com.eldanior.system.skills.skills.passives.Divin.Peche.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Competences passives (refonte du 2026-11-24) : chaque passif est une SERIE a rangs, du rang 1 (Commun) au rang 6
 * (Legendaire). Une page de rang superieur fait monter le passif. Les 7 Anges et 7 Peches sont des passifs uniques (Divin).
 *
 * FICHIER GENERE par scripts/passifs/gen_passifs.py depuis scripts/passifs/conception.py : modifier la conception, pas ce
 * fichier. Les valeurs de chaque rang sont dans les tableaux passes a PassiveEffects (index 0 = rang 1).
 * Les stats sont designees par leur nom : StatConfig ne doit pas etre charge au demarrage (ses IDs de stats seraient faux).
 */
public enum PassiveSkill {

    INSTINCTIVE_STRIKE(Theme.ATTAQUE, "Frappe Instinctive", 1, 6,
            new String[]{null, "Frappe Instinctive", "Frappe de Prédateur", "Frappe de Fureur", "Frappe Sismique", "Frappe Fantôme", "Frappe de l'Annihilateur"},
            new String[]{null, "10 % de chance d'infliger +10 % de dégâts.", "15 % de chance d'infliger +15 % de dégâts.", "20 % de chance d'infliger +25 % de dégâts.", "20 % de chance d'infliger +35 % de dégâts.", "25 % de chance d'infliger +50 % de dégâts.", "35 % de chance d'infliger +75 % de dégâts ; 40 % de chance de doubler l'impact."},
            PassiveEffects.chanceDegats(new float[]{10f, 15f, 20f, 20f, 25f, 35f}, new float[]{10f, 15f, 25f, 35f, 50f, 75f}, 0.4f)),
    DEEP_SLASH(Theme.ATTAQUE, "Entaille Profonde", 1, 6,
            new String[]{null, "Entaille Profonde", "Lame Aiguisée", "Lame Pourpre", "Lame du Néant", "Lame de l'Abîme", "Tranchant de la Genèse"},
            new String[]{null, "+1 dégâts à chaque coup.", "+2 dégâts à chaque coup.", "+4 dégâts à chaque coup.", "+6 dégâts à chaque coup.", "+9 dégâts à chaque coup.", "+12 dégâts à chaque coup."},
            PassiveEffects.degatsFixes(new float[]{1f, 2f, 4f, 6f, 9f, 12f})),
    PRESSURE_POINT(Theme.ATTAQUE, "Point de Pression", 1, 5,
            new String[]{null, "Point de Pression", "Pression Vitale", "Pression Écrasante", "Pression Annihilante", "Pression Brise-Âme", null},
            new String[]{null, "+15 % de dégâts contre une cible à plus de 90 % de vie.", "+25 % de dégâts contre une cible à plus de 90 % de vie.", "+35 % de dégâts contre une cible à plus de 90 % de vie.", "+45 % de dégâts contre une cible à plus de 90 % de vie.", "+55 % de dégâts contre une cible à plus de 90 % de vie.", null},
            PassiveEffects.ciblePleineVie(new float[]{15f, 25f, 35f, 45f, 55f, 55f})),
    HAUNTING_THRUST(Theme.ATTAQUE, "Traque", 1, 4,
            new String[]{null, "Estocade Obsédante", "Traque Implacable", "Traque Sanguinaire", "Traque Mortelle", null, null},
            new String[]{null, "Chaque coup de suite sur la même cible : +3 % de dégâts (jusqu'à 5 fois).", "Chaque coup de suite sur la même cible : +5 % de dégâts (jusqu'à 5 fois).", "Chaque coup de suite sur la même cible : +7 % de dégâts (jusqu'à 5 fois).", "Chaque coup de suite sur la même cible : +9 % de dégâts (jusqu'à 5 fois).", null, null},
            PassiveEffects.coupsEnchaines(new float[]{3f, 5f, 7f, 9f, 9f, 9f})),
    OPPORTUNIST_STRIKE(Theme.ATTAQUE, "Frappe Opportuniste", 1, 1,
            new String[]{null, "Frappe Opportuniste", null, null, null, null, null},
            new String[]{null, "+12 % de dégâts pendant 2 s après avoir été touché.", null, null, null, null, null},
            PassiveEffects.riposte(new float[]{12f, 12f, 12f, 12f, 12f, 12f})),
    AXE_MASTERY(Theme.ATTAQUE, "Maîtrise de la Hache", 2, 6,
            new String[]{null, null, "Maîtrise de la Hache", "Maîtrise de la Grande Hache", "Maîtrise de la Hache de Guerre", "Maîtrise Légendaire de la Hache", "Maîtrise Divine de la Hache"},
            new String[]{null, null, "+10 % de dégâts avec une hache.", "+15 % de dégâts avec une hache.", "+20 % de dégâts avec une hache.", "+25 % de dégâts avec une hache.", "+30 % de dégâts avec une hache."},
            PassiveEffects.arme(new String[]{"axe"}, new String[]{"pickaxe"}, new float[]{10f, 10f, 15f, 20f, 25f, 30f})),
    BOW_MASTERY(Theme.ATTAQUE, "Maîtrise de l'Arc", 2, 5,
            new String[]{null, null, "Maîtrise de l'Arc", "Maîtrise du Tir", "Maîtrise du Sniper", "Maîtrise Légendaire du Tir", null},
            new String[]{null, null, "+10 % de dégâts avec un arc.", "+15 % de dégâts avec un arc.", "+20 % de dégâts avec un arc.", "+25 % de dégâts avec un arc.", null},
            PassiveEffects.arme(new String[]{"bow"}, new String[]{}, new float[]{10f, 10f, 15f, 20f, 25f, 25f})),
    SPEAR_MASTERY(Theme.ATTAQUE, "Maîtrise de la Lance", 2, 4,
            new String[]{null, null, "Maîtrise de la Lance", "Maîtrise de la Hallebarde", "Maîtrise de la Lance Dragon", null, null},
            new String[]{null, null, "+10 % de dégâts avec une lance.", "+15 % de dégâts avec une lance.", "+20 % de dégâts avec une lance.", null, null},
            PassiveEffects.arme(new String[]{"spear", "halberd", "lance"}, new String[]{}, new float[]{10f, 10f, 15f, 20f, 20f, 20f})),
    DAGGER_MASTERY(Theme.ATTAQUE, "Maîtrise de la Dague", 2, 6,
            new String[]{null, null, "Maîtrise de la Dague", "Maîtrise de la Lame d'Ombre", "Maîtrise de la Lame d'Assassin", "Maîtrise Légendaire de la Dague", "Maîtrise Divine de la Dague"},
            new String[]{null, null, "+10 % de dégâts avec une dague.", "+15 % de dégâts avec une dague.", "+20 % de dégâts avec une dague.", "+25 % de dégâts avec une dague.", "+30 % de dégâts avec une dague."},
            PassiveEffects.arme(new String[]{"dagger", "kodachi"}, new String[]{}, new float[]{10f, 10f, 15f, 20f, 25f, 30f})),
    SWORD_MASTERY(Theme.ATTAQUE, "Maîtrise de l'Épée", 2, 4,
            new String[]{null, null, "Initiation à l'Épée", null, "Maîtrise de l'Épée", null, null},
            new String[]{null, null, "+8 % de dégâts avec une épée.", null, "+15 % de dégâts avec une épée.", null, null},
            PassiveEffects.arme(new String[]{"sword", "katana", "sabre", "saber"}, new String[]{}, new float[]{8f, 8f, 11f, 15f, 20f, 25f})),
    ARCANE_STRIKE(Theme.MAGIE, "Frappe Arcanique", 1, 6,
            new String[]{null, "Frappe Arcanique", "Lame Ensorcelée", "Dévastation Arcanique", "Annihilation Arcanique", "Oubli Arcanique", "Genèse Arcanique"},
            new String[]{null, "30 % de chance d'infliger 8 dégâts arcaniques en plus.", "40 % de chance d'infliger 12 dégâts arcaniques en plus.", "45 % de chance d'infliger 16 dégâts arcaniques en plus.", "50 % de chance d'infliger 22 dégâts arcaniques en plus.", "60 % de chance d'infliger 30 dégâts arcaniques en plus.", "70 % de chance d'infliger 40 dégâts arcaniques en plus."},
            PassiveEffects.chanceDegatsFixes(new float[]{30f, 40f, 45f, 50f, 60f, 70f}, new float[]{8f, 12f, 16f, 22f, 30f, 40f})),
    OVERFLOWING_POWER(Theme.MAGIE, "Puissance Débordante", 1, 5,
            new String[]{null, "Puissance Débordante", "Magie Déchaînée", "Magie Pure", "Suprématie Arcanique", "Suprématie Absolue", null},
            new String[]{null, "+15 % de dégâts quand ton mana dépasse 85 %.", "+25 % de dégâts quand ton mana dépasse 80 %.", "+35 % de dégâts quand ton mana dépasse 60 %.", "+45 % de dégâts quand ton mana dépasse 50 %.", "+55 % de dégâts quand ton mana dépasse 40 %.", null},
            PassiveEffects.manaHaut(new float[]{85f, 80f, 60f, 50f, 40f, 40f}, new float[]{15f, 25f, 35f, 45f, 55f, 55f})),
    AWAKENED_MIND(Theme.MAGIE, "Esprit Éveillé", 1, 6,
            new String[]{null, "Esprit Éveillé", "Esprit Élargi", "Esprit Brillant", "Esprit de Génie", "Esprit Cosmique", "Esprit Infini"},
            new String[]{null, "+15 Intelligence.", "+25 Intelligence.", "+35 Intelligence.", "+50 Intelligence.", "+70 Intelligence.", "+90 Intelligence."},
            PassiveEffects.stats(PassiveEffects.Declencheur.AUCUN, PassiveEffects.bonus("INTELLIGENCE", PassiveEffects.Mode.FLAT, new float[]{15f, 25f, 35f, 50f, 70f, 90f}))),
    MANA_FONT(Theme.MAGIE, "Source de Mana", 1, 5,
            new String[]{null, "Source de Mana", "Flux de Mana", "Rivière de Mana", "Océan de Mana", "Mana Infini", null},
            new String[]{null, "+20 % de régénération de mana.", "Régénération de mana ×2.", "Régénération de mana ×3.", "Régénération de mana ×4.", "Régénération de mana ×6.", null},
            PassiveEffects.stats(PassiveEffects.Declencheur.AUCUN, PassiveEffects.bonus("INTELLIGENCE", PassiveEffects.Mode.REGEN, new float[]{1.2f, 2f, 3f, 4f, 6f, 6f}))),
    SPIRITUAL_SIPHON(Theme.MAGIE, "Siphon Spirituel", 1, 6,
            new String[]{null, "Siphon Spirituel", "Voleur d'Âmes", "Drain Spirituel", "Vampirisme Arcanique", "Vampirisme Cosmique", "Vampirisme Infini"},
            new String[]{null, "30 % de chance de rendre 5 de mana à chaque coup donné.", "40 % de chance de rendre 8 de mana à chaque coup donné.", "45 % de chance de rendre 12 de mana à chaque coup donné.", "50 % de chance de rendre 16 de mana à chaque coup donné.", "60 % de chance de rendre 22 de mana à chaque coup donné.", "70 % de chance de rendre 30 de mana à chaque coup donné."},
            PassiveEffects.manaAuCoup(new float[]{30f, 40f, 45f, 50f, 60f, 70f}, new float[]{5f, 8f, 12f, 16f, 22f, 30f})),
    MANA_BARRIER(Theme.MAGIE, "Barrière de Mana", 1, 4,
            new String[]{null, "Barrière de Mana", "Bouclier Arcanique", "Forteresse de Mana", "Citadelle de Mana", null, null},
            new String[]{null, "Mana au-dessus de 50 % : -10 % de dégâts reçus.", "Mana au-dessus de 40 % : -15 % de dégâts reçus.", "Mana au-dessus de 30 % : -20 % de dégâts reçus.", "Mana au-dessus de 20 % : -25 % de dégâts reçus.", null, null},
            PassiveEffects.manaBouclier(new float[]{50f, 40f, 30f, 20f, 20f, 20f}, new float[]{10f, 15f, 20f, 25f, 25f, 25f})),
    MYSTIC_VEIL(Theme.MAGIE, "Voile Mystique", 1, 2,
            new String[]{null, "Voile Mystique", "Cape Astrale", null, null, null, null},
            new String[]{null, "10 % de chance de réduire un coup reçu de 25 %.", "30 % de chance de réduire un coup reçu de 35 %.", null, null, null, null},
            PassiveEffects.chanceReduction(new float[]{10f, 30f, 30f, 30f, 30f, 30f}, new float[]{25f, 35f, 35f, 35f, 35f, 35f})),
    VOL(Theme.MAGIE, "Vol", 6, 6,
            new String[]{null, null, null, null, null, null, "Vol"},
            new String[]{null, null, null, null, null, null, "Permet de voler en consommant 20 points de mana par seconde."},
            PassiveEffects.AUCUN),
    IRON_RESOLVE(Theme.DEFENSE, "Résolution de Fer", 1, 6,
            new String[]{null, "Résolution de Fer", "Résolution d'Acier", "Résolution de Titan", "Résolution Divine", "Résolution Immortelle", "Résolution Éternelle"},
            new String[]{null, "-3 dégâts sur chaque coup reçu.", "-5 dégâts sur chaque coup reçu.", "-8 dégâts sur chaque coup reçu.", "-12 dégâts sur chaque coup reçu.", "-16 dégâts sur chaque coup reçu.", "-20 dégâts sur chaque coup reçu."},
            PassiveEffects.reductionFixe(new float[]{3f, 5f, 8f, 12f, 16f, 20f})),
    MINOR_PARRY(Theme.DEFENSE, "Parade", 1, 6,
            new String[]{null, "Parade Mineure", "Parade Experte", "Parade de Maître", "Parade Parfaite", "Parade Divine", "Parade Céleste"},
            new String[]{null, "20 % de chance de réduire un coup reçu de 15 %.", "30 % de chance de réduire un coup reçu de 20 %.", "40 % de chance de réduire un coup reçu de 25 %.", "45 % de chance de réduire un coup reçu de 30 %.", "50 % de chance de réduire un coup reçu de 35 %.", "60 % de chance de réduire un coup reçu de 40 %."},
            PassiveEffects.chanceReduction(new float[]{20f, 30f, 40f, 45f, 50f, 60f}, new float[]{15f, 20f, 25f, 30f, 35f, 40f})),
    ADAPTIVE_SHIELD(Theme.DEFENSE, "Rempart", 1, 6,
            new String[]{null, "Bouclier Adaptatif", "Rempart Réactif", "Rempart d'Adamantine", "Rempart Divin", "Rempart Cosmique", "Rempart de la Genèse"},
            new String[]{null, "10 % de chance de réduire un coup reçu de 15 %.", "20 % de chance de réduire un coup reçu de 30 %.", "30 % de chance de réduire un coup reçu de 40 %.", "40 % de chance de réduire un coup reçu de 50 %.", "45 % de chance de réduire un coup reçu de 60 %.", "50 % de chance de réduire un coup reçu de 70 %."},
            PassiveEffects.chanceReduction(new float[]{10f, 20f, 30f, 40f, 45f, 50f}, new float[]{15f, 30f, 40f, 50f, 60f, 70f})),
    HUNTER_GUARD(Theme.DEFENSE, "Garde du Chasseur", 1, 4,
            new String[]{null, "Garde de Chasseur", "Garde Bestiale", "Garde du Tueur", "Garde du Tueur de Dragon", null, null},
            new String[]{null, "-10 % de dégâts reçus des monstres.", "-15 % de dégâts reçus des monstres.", "-20 % de dégâts reçus des monstres.", "-25 % de dégâts reçus des monstres.", null, null},
            PassiveEffects.contreMonstres(new float[]{10f, 15f, 20f, 25f, 25f, 25f})),
    STURDY_BODY(Theme.DEFENSE, "Corps Robuste", 1, 5,
            new String[]{null, "Corps Robuste", "Corps de Fer", "Corps d'Acier", "Corps de Diamant", "Corps Cosmique", null},
            new String[]{null, "+3 % de dégâts reçus en moins.", "+5 % de dégâts reçus en moins.", "+8 % de dégâts reçus en moins.", "+11 % de dégâts reçus en moins.", "+15 % de dégâts reçus en moins.", null},
            PassiveEffects.stats(PassiveEffects.Declencheur.TOUCHE, PassiveEffects.bonus("ENDURANCE_DEFENSE", PassiveEffects.Mode.FLAT, new float[]{3f, 5f, 8f, 11f, 15f, 15f}))),
    STONE_SKIN(Theme.DEFENSE, "Peau de Pierre", 1, 6,
            new String[]{null, "Peau de Pierre", "Peau de Bronze", "Peau de Fer", "Peau d'Acier", "Peau d'Obsidienne", "Peau de Diamant"},
            new String[]{null, "-5 % de dégâts reçus.", "-8 % de dégâts reçus.", "-10 % de dégâts reçus.", "-15 % de dégâts reçus.", "-25 % de dégâts reçus.", "-35 % de dégâts reçus."},
            PassiveEffects.reduction(new float[]{5f, 8f, 10f, 15f, 25f, 35f})),
    TENACITY(Theme.DEFENSE, "Ténacité", 1, 6,
            new String[]{null, "Ténacité", "Inflexible", "Incassable", "Invincible", "Immortel Absolu", "Immortalité Céleste"},
            new String[]{null, "Sous 30 % de vie : -8 % de dégâts reçus.", "Sous 35 % de vie : -12 % de dégâts reçus.", "Sous 40 % de vie : -16 % de dégâts reçus.", "Sous 45 % de vie : -20 % de dégâts reçus.", "Sous 50 % de vie : -25 % de dégâts reçus.", "Sous 55 % de vie : -30 % de dégâts reçus."},
            PassiveEffects.vieBasse(new float[]{30f, 35f, 40f, 45f, 50f, 55f}, new float[]{8f, 12f, 16f, 20f, 25f, 30f})),
    SOLID_STANCE(Theme.DEFENSE, "Posture Solide", 1, 6,
            new String[]{null, "Posture Solide", "Montagne Inébranlable", "Forteresse Vivante", "Forteresse Éternelle", "Forteresse Divine", "Forteresse Céleste"},
            new String[]{null, "Endurance au-dessus de 80 % : -10 % de dégâts reçus.", "Endurance au-dessus de 70 % : -15 % de dégâts reçus.", "Endurance au-dessus de 60 % : -20 % de dégâts reçus.", "Endurance au-dessus de 50 % : -25 % de dégâts reçus.", "Endurance au-dessus de 40 % : -30 % de dégâts reçus.", "Endurance au-dessus de 30 % : -35 % de dégâts reçus."},
            PassiveEffects.enduranceHaute(new float[]{80f, 70f, 60f, 50f, 40f, 30f}, new float[]{10f, 15f, 20f, 25f, 30f, 35f})),
    STEEL_NERVES(Theme.DEFENSE, "Nerfs d'Acier", 1, 2,
            new String[]{null, "Nerfs d'Acier", "Volonté de Fer", null, null, null, null},
            new String[]{null, "Les coups de plus de 15 dégâts sont réduits de 10 %.", "Les coups de plus de 12 dégâts sont réduits de 15 %.", null, null, null, null},
            PassiveEffects.grosCoups(new float[]{15f, 12f, 12f, 12f, 12f, 12f}, new float[]{10f, 15f, 15f, 15f, 15f, 15f})),
    MIRACLE_DODGE(Theme.DEFENSE, "Esquive", 1, 6,
            new String[]{null, "Esquive Miraculeuse", "Esquive Fantôme", "Esquive de l'Ombre", "Esquive Dimensionnelle", "Esquive de la Réalité", "Esquive Temporelle"},
            new String[]{null, "+2 % de chance d'esquive automatique (plafond 30 %).", "+3 % de chance d'esquive automatique (plafond 30 %).", "+5 % de chance d'esquive automatique (plafond 30 %).", "+7 % de chance d'esquive automatique (plafond 30 %).", "+9 % de chance d'esquive automatique (plafond 30 %).", "+12 % de chance d'esquive automatique (plafond 30 %)."},
            PassiveEffects.esquive(new float[]{2f, 3f, 5f, 7f, 9f, 12f})),
    ROBUST_CONSTITUTION(Theme.VIE, "Constitution", 1, 6,
            new String[]{null, "Constitution Robuste", "Constitution d'Acier", "Constitution de Titan", "Constitution Divine", "Constitution Cosmique", "Constitution Céleste"},
            new String[]{null, "+5 Vitalité.", "+10 Vitalité.", "+15 Vitalité.", "+20 Vitalité.", "+25 Vitalité.", "+30 Vitalité."},
            PassiveEffects.stats(PassiveEffects.Declencheur.TOUCHE, PassiveEffects.bonus("VITALITY", PassiveEffects.Mode.FLAT, new float[]{5f, 10f, 15f, 20f, 25f, 30f}))),
    LIFE_FORCE(Theme.VIE, "Force Vitale", 1, 5,
            new String[]{null, "Force Vitale", "Vie Débordante", "Vie Explosive", "Vie Éternelle", "Vie Infinie", null},
            new String[]{null, "+5 % de vie max.", "+10 % de vie max.", "+15 % de vie max.", "+20 % de vie max.", "+25 % de vie max.", null},
            PassiveEffects.stats(PassiveEffects.Declencheur.TOUCHE, PassiveEffects.bonus("VITALITY", PassiveEffects.Mode.MULT, new float[]{5f, 10f, 15f, 20f, 25f, 25f}))),
    VITAL_BLOOD(Theme.VIE, "Sang Vital", 1, 4,
            new String[]{null, "Sang Vital", "Sang Enrichi", "Sang Ancestral", "Sang de Dragon", null, null},
            new String[]{null, "+3 Vitalité, +10 % de régénération de vie.", "+6 Vitalité, +20 % de régénération de vie.", "+9 Vitalité, +30 % de régénération de vie.", "+12 Vitalité, +40 % de régénération de vie.", null, null},
            PassiveEffects.stats(PassiveEffects.Declencheur.TOUCHE, PassiveEffects.bonus("VITALITY", PassiveEffects.Mode.FLAT, new float[]{3f, 6f, 9f, 12f, 12f, 12f}), PassiveEffects.bonus("VITALITY", PassiveEffects.Mode.REGEN, new float[]{1.1f, 1.2f, 1.3f, 1.4f, 1.4f, 1.4f}))),
    HEART_OF_OAK(Theme.VIE, "Cœur de Chêne", 1, 6,
            new String[]{null, "Coeur de Chêne", "Coeur de Fer", "Coeur d'Acier", "Coeur de Diamant", "Coeur d'Éternité", "Coeur de la Genèse"},
            new String[]{null, "+4 Vitalité, +1 % de dégâts reçus en moins.", "+8 Vitalité, +2 % de dégâts reçus en moins.", "+12 Vitalité, +3 % de dégâts reçus en moins.", "+16 Vitalité, +4 % de dégâts reçus en moins.", "+20 Vitalité, +5 % de dégâts reçus en moins.", "+25 Vitalité, +6 % de dégâts reçus en moins."},
            PassiveEffects.stats(PassiveEffects.Declencheur.TOUCHE, PassiveEffects.bonus("VITALITY", PassiveEffects.Mode.FLAT, new float[]{4f, 8f, 12f, 16f, 20f, 25f}), PassiveEffects.bonus("ENDURANCE_DEFENSE", PassiveEffects.Mode.FLAT, new float[]{1f, 2f, 3f, 4f, 5f, 6f}))),
    CELLULAR_REGENERATION(Theme.VIE, "Régénération Cellulaire", 1, 6,
            new String[]{null, "Régénération Cellulaire", "Sang de Troll", "Sang d'Hydre", "Sang de Phénix", "Sang Éternel", "Sang de la Genèse"},
            new String[]{null, "20 % de chance de rendre 5 % de ta vie max quand tu es touché.", "30 % de chance de rendre 8 % de ta vie max quand tu es touché.", "40 % de chance de rendre 12 % de ta vie max quand tu es touché.", "45 % de chance de rendre 16 % de ta vie max quand tu es touché.", "50 % de chance de rendre 20 % de ta vie max quand tu es touché.", "60 % de chance de rendre 25 % de ta vie max quand tu es touché."},
            PassiveEffects.soinTouche(new float[]{20f, 30f, 40f, 45f, 50f, 60f}, new float[]{5f, 8f, 12f, 16f, 20f, 25f})),
    PERSEVERANCE(Theme.VIE, "Persévérance", 1, 2,
            new String[]{null, "Persévérance", "Immortel", null, null, null, null},
            new String[]{null, "Sous 25 % de vie : chaque coup reçu rend 2 % de ta vie max.", "Sous 30 % de vie : chaque coup reçu rend 4 % de ta vie max.", null, null, null, null},
            PassiveEffects.soinVieBasse(new float[]{25f, 30f, 30f, 30f, 30f, 30f}, new float[]{2f, 4f, 4f, 4f, 4f, 4f})),
    NATURAL_RECOVERY(Theme.VIE, "Rétablissement Naturel", 1, 2,
            new String[]{null, "Rétablissement Naturel", "Rétablissement Vital", null, null, null, null},
            new String[]{null, "+20 % de régénération de vie.", "Régénération de vie ×2.", null, null, null, null},
            PassiveEffects.stats(PassiveEffects.Declencheur.AUCUN, PassiveEffects.bonus("VITALITY", PassiveEffects.Mode.REGEN, new float[]{1.2f, 2f, 2f, 2f, 2f, 2f}))),
    TIRELESS_BREATH(Theme.ENDURANCE, "Souffle Inépuisable", 1, 6,
            new String[]{null, "Souffle Inépuisable", "Poumons d'Acier", "Poumons de Dragon", "Poumons de Titan", "Poumons Cosmiques", "Poumons Célestes"},
            new String[]{null, "+10 Endurance max.", "+20 Endurance max.", "+30 Endurance max.", "+40 Endurance max.", "+55 Endurance max.", "+70 Endurance max."},
            PassiveEffects.stats(PassiveEffects.Declencheur.AUCUN, PassiveEffects.bonus("ENDURANCE", PassiveEffects.Mode.FLAT, new float[]{10f, 20f, 30f, 40f, 55f, 70f}))),
    COMBAT_VIGOR(Theme.ENDURANCE, "Vigueur Combative", 1, 5,
            new String[]{null, "Vigueur Combative", "Frénésie de Combat", "Frénésie Guerrière", "Frénésie Sanguinaire", "Frénésie Enragée", null},
            new String[]{null, "30 % de chance de rendre 5 d'endurance à chaque coup donné.", "45 % de chance de rendre 8 d'endurance à chaque coup donné.", "60 % de chance de rendre 12 d'endurance à chaque coup donné.", "70 % de chance de rendre 16 d'endurance à chaque coup donné.", "70 % de chance de rendre 20 d'endurance à chaque coup donné.", null},
            PassiveEffects.enduranceAuCoup(new float[]{30f, 45f, 60f, 70f, 70f, 70f}, new float[]{5f, 8f, 12f, 16f, 20f, 20f})),
    SECOND_WIND(Theme.ENDURANCE, "Second Souffle", 1, 2,
            new String[]{null, "Second Souffle", "Esprit du Survivant", null, null, null, null},
            new String[]{null, "Endurance sous 20 % : 20 % de chance de rendre 25 d'endurance quand tu es touché.", "Endurance sous 25 % : 30 % de chance de rendre 40 d'endurance quand tu es touché.", null, null, null, null},
            PassiveEffects.secondSouffle(new float[]{20f, 25f, 25f, 25f, 25f, 25f}, new float[]{20f, 30f, 30f, 30f, 30f, 30f}, new float[]{25f, 40f, 40f, 40f, 40f, 40f})),
    WIND_STEP(Theme.AGILITE, "Pas de Vent", 1, 6,
            new String[]{null, "Pas de Vent", "Pas de Bourrasque", "Pas de Tempête", "Pas du Néant", "Pas Dimensionnel", "Pas Céleste"},
            new String[]{null, "+3 % de vitesse de déplacement.", "+5 % de vitesse de déplacement.", "+8 % de vitesse de déplacement.", "+12 % de vitesse de déplacement.", "+16 % de vitesse de déplacement.", "+20 % de vitesse de déplacement."},
            PassiveEffects.stats(PassiveEffects.Declencheur.AUCUN, PassiveEffects.bonus("AGILITY_SPEED", PassiveEffects.Mode.MULT, new float[]{3f, 5f, 8f, 12f, 16f, 20f}))),
    ELDANIOR_SUPPLENESS(Theme.AGILITE, "Souplesse", 1, 4,
            new String[]{null, "Souplesse d'Eldanior", "Souplesse Féline", "Souplesse Acrobatique", "Défi à la Gravité", null, null},
            new String[]{null, "+10 % de hauteur de saut.", "+18 % de hauteur de saut.", "+25 % de hauteur de saut.", "+35 % de hauteur de saut.", null, null},
            PassiveEffects.stats(PassiveEffects.Declencheur.AUCUN, PassiveEffects.bonus("AGILITY_JUMP", PassiveEffects.Mode.MULT, new float[]{10f, 18f, 25f, 35f, 35f, 35f}))),
    SURVIVAL_INSTINCT(Theme.AGILITE, "Instinct de Survie", 1, 5,
            new String[]{null, "Instinct de Survie", "Sens du Danger", "Intuition de Combat", "Prophétie de Guerre", "Vision du Destin", null},
            new String[]{null, "Signale les présences proches (portée ×1,5), joueurs invisibles compris.", "Signale les présences proches (portée ×1,8), joueurs invisibles compris.", "Signale les présences proches (portée ×2,2), joueurs invisibles compris.", "Signale les présences proches (portée ×2,8), joueurs invisibles compris.", "Signale les présences proches (portée ×3,5), joueurs invisibles compris.", null},
            PassiveEffects.radar(new float[]{1.5f, 1.8f, 2.2f, 2.8f, 3.5f, 3.5f})),
    NIGHT_VISION(Theme.AGILITE, "Vision Nocturne", 1, 1,
            new String[]{null, "Vision Nocturne", null, null, null, null, null},
            new String[]{null, "Tu vois clair la nuit.", null, null, null, null, null},
            PassiveEffects.AUCUN),
    LUCKY_STRIKE(Theme.CHANCE, "Frappe Chanceuse", 1, 6,
            new String[]{null, "Frappe Chanceuse", "Chance Critique", "Frappe du Destin", "Frappe Divine", "Frappe Cosmique", "Frappe de la Genèse"},
            new String[]{null, "+3 % de chance de critique.", "+5 % de chance de critique.", "+8 % de chance de critique.", "+12 % de chance de critique.", "+16 % de chance de critique.", "+20 % de chance de critique."},
            PassiveEffects.stats(PassiveEffects.Declencheur.CRITIQUE, PassiveEffects.bonus("LUCK_CRITICAL", PassiveEffects.Mode.FLAT, new float[]{3f, 5f, 8f, 12f, 16f, 20f}))),
    TREASURE_HUNTER(Theme.CHANCE, "Chasseur de Trésors", 1, 5,
            new String[]{null, "Chasseur de Trésors", "Chasseur de Reliques", "Chasseur d'Artefacts", "Chasseur de Légendes", "Chasseur de Mythes", null},
            new String[]{null, "+5 % de chance de butin.", "+10 % de chance de butin.", "+15 % de chance de butin.", "+20 % de chance de butin.", "+25 % de chance de butin.", null},
            PassiveEffects.stats(PassiveEffects.Declencheur.AUCUN, PassiveEffects.bonus("LUCK_LOOT", PassiveEffects.Mode.FLAT, new float[]{5f, 10f, 15f, 20f, 25f, 25f}))),
    GOOD_OMEN(Theme.CHANCE, "Bon Présage", 1, 4,
            new String[]{null, "Bon Présage", "Présage du Destin", "Présage Prophétique", "Présage Cosmique", null, null},
            new String[]{null, "+15 % de chance d'événement rare.", "+25 % de chance d'événement rare.", "+35 % de chance d'événement rare.", "+50 % de chance d'événement rare.", null, null},
            PassiveEffects.stats(PassiveEffects.Declencheur.AUCUN, PassiveEffects.bonus("LUCK_EVENT", PassiveEffects.Mode.FLAT, new float[]{15f, 25f, 35f, 50f, 50f, 50f}))),
    PECHE_ORGUEIL(Theme.PECHE, "Orgueil", 7, 7, null, new String[]{null, null, null, null, null, null, null, "+5 % de dégâts et -3 % de dégâts reçus par pièce d'armure portée (armure complète : +20 % et -12 %)."},
            new Orgueil()),
    PECHE_COLERE(Theme.PECHE, "Colère", 7, 7, null, new String[]{null, null, null, null, null, null, null, "Au-dessus de 70 % de vie : +40 % de dégâts. Chaque coup reçu d'un joueur ajoute une charge de rage (+5 % de dégâts, 6 charges, 10 s)."},
            new Colere()),
    PECHE_AVARICE(Theme.PECHE, "Avarice", 7, 7, null, new String[]{null, null, null, null, null, null, null, "+15 % de chance de butin. En tuant un joueur : 25 % de chance de lui voler un objet de sa barre d'action (recharge : 1 jour)."},
            new Avarice(), 86400f),
    PECHE_ENVIE(Theme.PECHE, "Envie", 7, 7, null, new String[]{null, null, null, null, null, null, null, "+1 % de dégâts par % de vie perdue (jusqu'à +80 %). Contre un joueur de plus haut niveau : +25 % de dégâts en plus."},
            new Envie()),
    PECHE_LUXURE(Theme.PECHE, "Luxure", 7, 7, null, new String[]{null, null, null, null, null, null, null, "+10 % de vitesse de déplacement. En tuant un joueur : tu lui voles 10 % de son or (recharge : 12 h)."},
            new Luxure(), 43200f),
    PECHE_GOURMANDISE(Theme.PECHE, "Gourmandise", 7, 7, null, new String[]{null, null, null, null, null, null, null, "Chaque joueur tué te rend 20 % de ta vie max. 25 % de chance de copier l'un de ses passifs à son rang (recharge : 1 jour)."},
            new Gourmandise()),
    PECHE_PARESSE(Theme.PECHE, "Paresse", 7, 7, null, new String[]{null, null, null, null, null, null, null, "-15 % de dégâts reçus des joueurs, et les joueurs qui te frappent sont ralentis."},
            new Paresse()),
    ANGE_HUMILITE(Theme.ANGE, "Humilité (Michaël)", 7, 7, null, new String[]{null, null, null, null, null, null, null, "+15 % de dégâts et -10 % de dégâts reçus par pièce d'armure en moins (sans armure : +60 % et -40 %)."},
            new Humilite()),
    ANGE_PATIENCE(Theme.ANGE, "Patience (Raphaël)", 7, 7, null, new String[]{null, null, null, null, null, null, null, "+5 % de dégâts et -5 % de dégâts reçus par membre de ton groupe à moins de 15 blocs (4 au plus)."},
            new Patience()),
    ANGE_TEMPERANCE(Theme.ANGE, "Tempérance (Uriel)", 7, 7, null, new String[]{null, null, null, null, null, null, null, "La main vide : +200 % de dégâts et dégâts reçus divisés par deux."},
            new Temperance()),
    ANGE_CHARITE(Theme.ANGE, "Charité (Gabriel)", 7, 7, null, new String[]{null, null, null, null, null, null, null, "Dégâts reçus divisés par 2,5. Quand tu es touché, les membres blessés de ton groupe à moins de 15 blocs récupèrent 3 % de leur vie max."},
            new Charite()),
    ANGE_BIENVEILLANCE(Theme.ANGE, "Bienveillance (Chamuel)", 7, 7, null, new String[]{null, null, null, null, null, null, null, "+10 % d'XP. Ton groupe reçoit 30 % de l'XP de tes victimes au lieu de 10 %."},
            PassiveEffects.AUCUN),
    ANGE_CHASTETE(Theme.ANGE, "Chasteté (Azraël)", 7, 7, null, new String[]{null, null, null, null, null, null, null, "Contre les monstres : +150 % de dégâts et -25 % de dégâts reçus."},
            new Chastete()),
    ANGE_DILIGENCE(Theme.ANGE, "Diligence (Métatron)", 7, 7, null, new String[]{null, null, null, null, null, null, null, "+20 % d'XP, maîtrise des passifs cinq fois plus rapide, +100 % de vitesse de déplacement."},
            new Diligence());

    /** Theme d'un passif (tri et filtre de l'interface). */
    public enum Theme {
        ATTAQUE("Attaque"), MAGIE("Magie"), DEFENSE("Défense"), VIE("Vie"), ENDURANCE("Endurance"), AGILITE("Agilité"),
        CHANCE("Chance"), PECHE("Péché capital"), ANGE("Ange");
        public final String label;
        Theme(String label) { this.label = label; }
    }

    /** Un passif a un rang donne (passifs de classe, passifs d'un joueur). */
    public record Ranked(PassiveSkill skill, int rank) {}

    /** Rang maximal d'un joueur pour un passif de serie (Legendaire). */
    public static final int MAX_RANK = 6;
    /** Rang affiche des Anges et Peches. */
    public static final int DIVINE_RANK = 7;

    private static final String[] RANK_LABELS = {"", "Commun", "Inhabituel", "Rare", "Épique", "Unique", "Légendaire", "Divin"};
    private static final String[] RANK_KEYS = {"", "COMMUN", "UNCOMMON", "RARE", "EPIQUE", "UNIQUE", "LEGENDAIRE", "DIVIN"};

    private final Theme theme;
    private final String displayName;
    private final int minRank, maxRank;
    private final String[] rankNames;
    private final String[] descriptions;
    private final IPassiveCombatSkill logic;
    private final float cooldownSeconds;

    PassiveSkill(Theme theme, String displayName, int minRank, int maxRank, String[] rankNames, String[] descriptions,
                 IPassiveCombatSkill logic) {
        this(theme, displayName, minRank, maxRank, rankNames, descriptions, logic, 0f);
    }

    PassiveSkill(Theme theme, String displayName, int minRank, int maxRank, String[] rankNames, String[] descriptions,
                 IPassiveCombatSkill logic, float cooldownSeconds) {
        this.theme = theme;
        this.displayName = displayName;
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.rankNames = rankNames;
        this.descriptions = descriptions;
        this.logic = logic;
        this.cooldownSeconds = cooldownSeconds;
    }

    public Ranked r(int rank) { return new Ranked(this, clamp(rank)); }

    public String getId() { return name(); }
    public Theme getTheme() { return theme; }
    public String getDisplayName() { return displayName; }
    public int getMinRank() { return minRank; }
    public int getMaxRank() { return maxRank; }
    public IPassiveCombatSkill getLogic() { return logic; }
    /** Recharge apres un declenchement (seulement certains Peches). */
    public float getCooldownSeconds() { return cooldownSeconds; }

    /** Rang ramene entre le premier et le dernier rang de la serie. */
    public int clamp(int rank) { return Math.max(minRank, Math.min(maxRank, rank)); }

    /** Nom du rang (ancien nom du passif de ce rang : Frappe de Fureur...) ; le nom de la serie s'il n'en a pas. */
    public String getRankName(int rank) {
        int r = clamp(rank);
        return rankNames != null && rankNames[r] != null ? rankNames[r] : displayName;
    }

    /** Effet au rang donne (le plus proche si ce rang n'existe pas). */
    public String getDescription(int rank) {
        int r = clamp(rank);
        for (int i = r; i >= 1; i--) if (descriptions[i] != null) return descriptions[i];
        for (int i = r; i < descriptions.length; i++) if (descriptions[i] != null) return descriptions[i];
        return "";
    }

    public boolean hasRank(int rank) { return rank >= 1 && rank < descriptions.length && descriptions[rank] != null; }

    public static String rankLabel(int rank) { return rank >= 0 && rank < RANK_LABELS.length ? RANK_LABELS[rank] : ""; }
    /** Cle de rarete de l'interface (COMMUN, UNCOMMON...). */
    public static String rankKey(int rank) { return rank >= 0 && rank < RANK_KEYS.length ? RANK_KEYS[rank] : ""; }

    public boolean isDivine() { return theme == Theme.PECHE || theme == Theme.ANGE; }
    public boolean isPeche() { return theme == Theme.PECHE; }
    public boolean isAnge() { return theme == Theme.ANGE; }

    private static final Map<String, PassiveSkill> LOOKUP;
    static {
        Map<String, PassiveSkill> map = new HashMap<>();
        for (PassiveSkill ps : values()) map.put(ps.name(), ps);
        LOOKUP = Collections.unmodifiableMap(map);
    }

    /** Le passif de cet identifiant, ou null (sans exception). */
    public static PassiveSkill fromName(String name) { return name == null ? null : LOOKUP.get(name.toUpperCase()); }

    public static boolean isDivineSkill(String id) { PassiveSkill p = fromName(id); return p != null && p.isDivine(); }
    public static boolean isPecheSkill(String id) { PassiveSkill p = fromName(id); return p != null && p.isPeche(); }
    public static boolean isAngeSkill(String id) { PassiveSkill p = fromName(id); return p != null && p.isAnge(); }
}
