package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TraqueurAbsolu extends ClassModel {
    public TraqueurAbsolu() {
        super("traqueur_absolu", "Traqueur Absolu", "Sa traque est implacable. Une fois la piste relevee, c'est deja fini.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.MARATHON_RUNNER, PassiveSkill.STORM_STEP), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW, WeaponMastery.SWORD), List.of(), 400, false,
                40, 36, 8, 28, 74, 74);
    }
}
