package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArtisanLegendaire extends ClassModel {
    public ArtisanLegendaire() {
        super("artisan_legendaire", "Artisan Legendaire", "L'artisan dont les creations sont des oeuvres d'art inestimables.",
                Rarity.EPIC, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.STEEL_RESOLVE, PassiveSkill.RELIC_HUNTER), List.of(WeaponMastery.ANY), List.of(), 400, false,
                20, 7, 28, 24, 10, 42);
    }
}
