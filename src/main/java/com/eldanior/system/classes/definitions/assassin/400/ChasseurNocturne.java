package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChasseurNocturne extends ClassModel {
    public ChasseurNocturne() {
        super("chasseur_nocturne", "Chasseur Nocturne", "La nuit est son territoire. Invisible dans les tenebres, il frappe sans bruit.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.DARK_VISION, PassiveSkill.RAZOR_SENSES, PassiveSkill.SHADOW_DODGE), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                10, 8, 6, 6, 30, 22);
    }
}
