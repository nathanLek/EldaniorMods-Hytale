// 2. Le Colosse (Épique - Tank)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Colosse extends ClassModel {
    public Colosse() {
        super("colosse", "Colosse", "Une montagne de muscles impossible a faire vaciller. Le Colosse absorbe les degats comme nul autre.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.STEEL_BODY, PassiveSkill.BURSTING_LIFE, PassiveSkill.UNBREAKABLE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of("titan_de_pierre", "golem_vivant", "forteresse"), 400, false,
                60, 140, 10, 140, 20, 30);
    }
}