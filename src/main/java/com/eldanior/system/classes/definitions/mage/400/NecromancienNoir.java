package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class NecromancienNoir extends ClassModel {
    public NecromancienNoir() {
        super("necromancien_noir", "Necromancien Noir", "Un maitre de la mort noire dont les rituels font trembler les vivants.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.SPIRITUAL_SIPHON, PassiveSkill.SPIRIT_DRAIN, PassiveSkill.AWAKENED_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                7, 10, 40, 7, 7, 14);
    }
}
