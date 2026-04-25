package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AvatarDeLArc extends ClassModel {
    public AvatarDeLArc() {
        super("avatar_arc", "Avatar de l'Arc", "L'Avatar de l'Arc est la fleche incarnee. Il est partout et nulle part, frappant avant meme d'etre vu.",
                Rarity.LEGENDARY, ClassType.ARCHER,
                List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.COSMIC_CONSTITUTION),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER),
                List.of(), 250, false,
                100, 80, 40, 60, 200, 200);
    }
}
