package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LegendeArchere extends ClassModel {
    public LegendeArchere() {
        super("legende_archere", "Legende Archere", "La legende vivante de l'archerie. Son nom est eternel.",
                Rarity.LEGENDARY, ClassType.ARCHER, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.DEMIGOD_SWIFTNESS), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                174, 132, 64, 106, 344, 344);
    }
}
