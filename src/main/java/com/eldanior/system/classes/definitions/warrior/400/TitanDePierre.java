package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TitanDePierre extends ClassModel {
    public TitanDePierre() {
        super("titan_de_pierre", "Titan de Pierre", "Le Titan de Pierre est une montagne vivante que rien ne peut ebranler. Sa peau de diamant et sa vie debordante defient les lois de la nature.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.DIAMOND_BODY, PassiveSkill.BURSTING_LIFE, PassiveSkill.UNBREAKABLE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                100, 238, 17, 238, 34, 50);
    }
}