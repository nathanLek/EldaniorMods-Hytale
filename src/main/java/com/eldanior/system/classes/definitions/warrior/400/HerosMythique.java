package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class HerosMythique extends ClassModel {
    public HerosMythique() {
        super("heros_mythique", "Heros Mythique", "Le Heros Mythique est chante dans les legendes de toutes les civilisations. Sa maitrise universelle du combat et sa constitution cosmique en font un etre d'exception.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                255, 255, 255, 255, 136, 136);
    }
}