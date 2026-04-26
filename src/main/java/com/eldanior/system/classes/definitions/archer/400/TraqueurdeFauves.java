package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TraqueurdeFauves extends ClassModel {
    public TraqueurdeFauves() {
        super("traqueur_de_fauves", "Traqueur de Fauves", "Specialise dans la traque des betes les plus dangereuses.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.PREDATORY_STRIKE, PassiveSkill.KEEN_SENSES), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                12, 10, 4, 8, 26, 18);
    }
}
