package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AvatardelAbondance extends ClassModel {
    public AvatardelAbondance() {
        super("avatar_de_l_abondance", "Avatar de l'Abondance", "L'incarnation de la prosperite eternelle. Sa presence enrichit le monde.",
                Rarity.LEGENDARY, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.ANY), List.of(), 400, false,
                340, 340, 408, 340, 510, 2720);
    }
}
