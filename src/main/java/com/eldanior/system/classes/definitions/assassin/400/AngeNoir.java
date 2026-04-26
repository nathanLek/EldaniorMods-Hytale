package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AngeNoir extends ClassModel {
    public AngeNoir() {
        super("ange_noir", "Ange Noir", "Un ange dechu par choix. Sa justice est celle de la mort.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.DEATH_HUNT, PassiveSkill.DIVINE_REFLEXES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                130, 54, 12, 54, 148, 108);
    }
}
