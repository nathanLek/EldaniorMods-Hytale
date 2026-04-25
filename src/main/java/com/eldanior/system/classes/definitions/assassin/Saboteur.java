package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Saboteur extends ClassModel {
    public Saboteur() {
        super("saboteur", "Saboteur", "Le Saboteur seme le chaos derriere les lignes ennemies. Pieges et embuscades sont ses armes favorites.",
                Rarity.COMMON, ClassType.ASSASSIN,
                List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.STONE_SKIN, PassiveSkill.TIRELESS_BREATH),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 120, false,
                8, 8, 4, 6, 14, 10);
    }
}
