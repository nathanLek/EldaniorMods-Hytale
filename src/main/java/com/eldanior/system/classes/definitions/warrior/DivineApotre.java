package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DivineApotre extends ClassModel {
    public DivineApotre() {
        super("DivineApotre", "Apotre Divin", "Dieu t'accorde sa benediction. L'Apotre Divin porte la lumiere divine sur le champ de bataille.",
                Rarity.DIVINE, ClassType.WARRIOR, List.of(PassiveSkill.CREATOR_EDGE, PassiveSkill.CREATOR_CONSTITUTION, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                440, 360, 280, 360, 360, 480);
    }
}