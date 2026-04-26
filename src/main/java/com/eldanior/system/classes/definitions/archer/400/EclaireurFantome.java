package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EclaireurFantome extends ClassModel {
    public EclaireurFantome() {
        super("eclaireur_fantome_archer", "Eclaireur Fantome", "Un eclaireur si rapide qu'il semble se teleporter entre les ombres.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.GALE_STEP, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.PHANTOM_DODGE), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                7, 7, 7, 7, 34, 24);
    }
}
