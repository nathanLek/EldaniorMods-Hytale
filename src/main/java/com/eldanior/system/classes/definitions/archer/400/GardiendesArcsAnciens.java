package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GardiendesArcsAnciens extends ClassModel {
    public GardiendesArcsAnciens() {
        super("gardien_des_arcs_anciens", "Gardien des Arcs Anciens", "Le gardien de tous les arcs legendaires. Son savoir est immense.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.MANA_OCEAN, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                72, 82, 132, 72, 140, 166);
    }
}
