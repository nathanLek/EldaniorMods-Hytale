package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameAbyssale extends ClassModel {
    public LameAbyssale() {
        super("lame_abyssale", "Lame Abyssale", "Sa lame est forgee dans les abysses. Chaque coup draine l'ame.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.SOUL_STEALER), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                60, 18, 8, 18, 96, 70);
    }
}
