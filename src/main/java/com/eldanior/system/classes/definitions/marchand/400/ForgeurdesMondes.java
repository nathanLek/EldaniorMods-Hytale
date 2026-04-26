package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ForgeurdesMondes extends ClassModel {
    public ForgeurdesMondes() {
        super("forgeur_des_mondes", "Forgeur des Mondes", "Le forgeur dont le marteau a facon les mondes eux-memes.",
                Rarity.LEGENDARY, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.CREATOR_MIND, PassiveSkill.GENESIS_STRIKE), List.of(WeaponMastery.ANY), List.of(), 400, false,
                850, 272, 1020, 680, 170, 680);
    }
}
