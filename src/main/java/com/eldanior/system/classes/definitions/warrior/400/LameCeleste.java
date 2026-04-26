package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameCeleste extends ClassModel {
    public LameCeleste() {
        super("lame_celeste", "Lame Celeste", "La Lame Celeste manie son epee avec une fluidite surnaturelle. Ses coups semblent guides par les etoiles, frappant toujours au moment parfait.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.DUELIST_SWIFTNESS, PassiveSkill.SHARP_BLADE, PassiveSkill.STORM_STEP), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                40, 26, 7, 16, 26, 10);
    }
}