package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Warlord extends ClassModel {

    public Warlord() {
        super(
                "warlord",
                "Warlord",
                "Le Warlord est un chef de guerre ne. Sa presence sur le champ de bataille inspire ses allies et terrifie ses ennemis.",
                Rarity.EPIC,
                ClassType.WARRIOR,
                List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.LIGHTNING_REFLEXES),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                100, 80, 30, 80, 50, 60
        );
    }
}