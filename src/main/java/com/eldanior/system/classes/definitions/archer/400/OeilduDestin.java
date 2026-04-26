package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OeilduDestin extends ClassModel {
    public OeilduDestin() {
        super("oeil_du_destin", "Oeil du Destin", "Son regard voit le destin de chacun. Nul n'echappe a sa vision.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.FATAL_PRECISION, PassiveSkill.VOID_BLADE, PassiveSkill.DEMIGOD_SWIFTNESS), List.of(WeaponMastery.BOW), List.of(), 400, false,
                98, 64, 36, 50, 200, 250);
    }
}
