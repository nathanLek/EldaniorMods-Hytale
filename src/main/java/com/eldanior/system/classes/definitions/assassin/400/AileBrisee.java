package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AileBrisee extends ClassModel {
    public AileBrisee() {
        super("aile_brisee", "Aile Brisee", "Ses ailes sont brisees mais sa fureur n'a pas de limites.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.ACROBATIC_POISE, PassiveSkill.DIVINE_REFLEXES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                64, 54, 20, 36, 218, 108);
    }
}
