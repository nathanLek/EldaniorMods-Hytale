package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ColosseEternel extends ClassModel {
    public ColosseEternel() {
        super("colosse_eternel", "Colosse Eternel", "Le Colosse Eternel existe depuis les premiers ages du monde. Son corps immortel et sa forteresse interieure sont des monuments indestructibles.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.GOD_CONSTITUTION, PassiveSkill.INFINITE_LIFE, PassiveSkill.LIVING_FORTRESS), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                336, 504, 0, 240, 66, 34);
    }
}