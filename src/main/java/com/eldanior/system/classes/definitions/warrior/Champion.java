package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Champion extends ClassModel {
    public Champion() {
        super("champion", "Champion", "Un combattant d'exception, idolatre par les foules. Le Champion incarne la perfection martiale.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.TITAN_CONSTITUTION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of("elu_des_batailles", "conquerant", "invaincu"), 400, false,
                120, 80, 20, 80, 60, 40);
    }
}