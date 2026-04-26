package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Necromancien extends ClassModel {
    public Necromancien() {
        super("necromancien", "Necromancien", "Le Necromancien puise dans les energies sombres de la mort. Ses maledictions affaiblissent l'ame de ses victimes.",
                Rarity.COMMON, ClassType.MAGE,
                List.of(PassiveSkill.SPIRITUAL_SIPHON, PassiveSkill.AWAKENED_MIND, PassiveSkill.LUCKY_STRIKE),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("necromancien_noir", "seigneur_des_morts", "invocateur_d_ames"), 400, false,
                4, 6, 24, 4, 4, 8);
    }
}
