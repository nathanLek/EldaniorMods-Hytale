package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ShinobiSupreme extends ClassModel {
    public ShinobiSupreme() {
        super("shinobi_supreme", "Shinobi Supreme", "Le shinobi ultime dont les techniques sont gardees secretes depuis des generations.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.GRAVITY_DEFIANCE, PassiveSkill.CRIMSON_BLADE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                42, 24, 20, 14, 114, 48);
    }
}
