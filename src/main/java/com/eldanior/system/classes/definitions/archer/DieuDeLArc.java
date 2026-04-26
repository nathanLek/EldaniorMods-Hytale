package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DieuDeLArc extends ClassModel {
    public DieuDeLArc() {
        super("dieu_arc", "Dieu de l'Arc", "Le Dieu de l'Arc ne connait pas la notion de distance. Chaque fleche traverse le monde pour atteindre sa cible.",
                Rarity.DIVINE, ClassType.ARCHER,
                List.of(PassiveSkill.CREATOR_EDGE, PassiveSkill.CREATOR_PRECISION, PassiveSkill.FATE_DODGE),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER),
                List.of("demi_dieu_archer"), 400, false,
                150, 120, 60, 80, 350, 350);
    }
}
