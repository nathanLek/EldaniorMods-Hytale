package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

/**
 * Bienveillance (Chamuel) — Quand le joueur ramasse de l'argent, 10% est redistribué aux alliés du groupe.
 * Cet effet est géré par DivineAuraSystem (système tick) qui intercepte les gains d'argent.
 * Cette classe sert de marqueur dans le PassiveSkill enum.
 * Église RELIGIEUX+ only.
 */
public class Bienveillance implements IPassiveCombatSkill {
    // Effet géré par DivineAuraSystem — pas de combat hook nécessaire
}
