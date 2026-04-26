package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EclaireurdElite extends ClassModel {
    public EclaireurdElite() {
        super("eclaireur_d_elite", "Eclaireur d'Elite", "Un eclaireur d'elite dont l'instinct est aussi affute que sa lame.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.SURVIVAL_INSTINCT, PassiveSkill.RAZOR_SENSES), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                7, 10, 7, 7, 34, 17);
    }
}
