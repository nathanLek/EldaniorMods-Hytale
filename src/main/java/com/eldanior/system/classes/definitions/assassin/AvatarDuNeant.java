package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AvatarDuNeant extends ClassModel {
    public AvatarDuNeant() {
        super("avatar_neant", "Avatar du Neant", "L'Avatar du Neant est le vide absolu. Il efface l'existence meme de ceux qui croisent son chemin.",
                Rarity.DIVINE, ClassType.ASSASSIN,
                List.of(PassiveSkill.CREATOR_EDGE, PassiveSkill.CREATOR_STEP, PassiveSkill.FATE_DODGE),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                180, 120, 40, 100, 400, 350);
    }
}
