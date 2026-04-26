package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OeilPercant extends ClassModel {
    public OeilPercant() {
        super("oeil_percant", "Oeil Percant", "Rien n'echappe a son regard. Il detecte les faiblesses avant meme le combat.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.HAWK_EYE, PassiveSkill.WIND_STEP, PassiveSkill.KEEN_SENSES), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                8, 8, 8, 6, 32, 20);
    }
}
