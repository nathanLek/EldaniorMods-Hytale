package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreOmbre extends ClassModel {
    public MaitreOmbre() {
        super("maitre_ombre", "Maitre de l'Ombre", "Le Maitre de l'Ombre controle les tenebres elles-memes. Il devient invisible a volonte et frappe depuis le neant.",
                Rarity.EPIC, ClassType.ASSASSIN,
                List.of(PassiveSkill.SHADOW_DODGE, PassiveSkill.STORM_STEP, PassiveSkill.DEADLY_PRECISION),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of("souverain_des_ombres", "ombre_eternelle", "void_walker"), 400, false,
                50, 30, 10, 30, 100, 80);
    }
}
