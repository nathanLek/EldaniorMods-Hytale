package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OmbreSupreme extends ClassModel {
    public OmbreSupreme() {
        super("ombre_supreme", "Ombre Supreme", "L'Ombre Supreme est le cauchemar des mortels. Personne ne l'a jamais vue et vecu pour le raconter.",
                Rarity.LEGENDARY, ClassType.ASSASSIN,
                List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.DEMIGOD_SWIFTNESS, PassiveSkill.REALITY_DODGE),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                120, 60, 20, 60, 250, 180);
    }
}
