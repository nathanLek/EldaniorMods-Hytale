package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameNoire extends ClassModel {
    public LameNoire() {
        super("lame_noire", "Lame Noire", "La Lame Noire est un assassin d'elite qui ne rate jamais sa cible. Son nom seul suffit a semer la terreur.",
                Rarity.RARE, ClassType.ASSASSIN,
                List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.SOUL_STEALER, PassiveSkill.RAZOR_SENSES),
                List.of(WeaponMastery.DAGGER),
                List.of(), 250, false,
                35, 10, 5, 10, 55, 40);
    }
}
