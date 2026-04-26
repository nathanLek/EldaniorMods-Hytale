package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SpectreOriginel extends ClassModel {
    public SpectreOriginel() {
        super("spectre_originel", "Spectre Originel", "Le premier spectre, ne avant les tenebres elles-memes.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.FATAL_PRECISION, PassiveSkill.DIMENSIONAL_STEP), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                136, 85, 26, 68, 272, 204);
    }
}
