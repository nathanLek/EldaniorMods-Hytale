package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Protecteur extends ClassModel {

    public Protecteur() {
        super(
                "protecteur",
                "Protecteur",
                "Le Protecteur est un rempart vivant qui se dresse entre ses allies et le danger. Son bouclier est son arme la plus redoutable.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(PassiveSkill.STONE_SKIN, PassiveSkill.ROBUST_CONSTITUTION, PassiveSkill.NATURAL_RECOVERY),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                120,
                false,
                12, 24, 2, 18, 6, 4
        );
    }
}
