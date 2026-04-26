package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EpeedesOmbres extends ClassModel {
    public EpeedesOmbres() {
        super("epee_des_ombres", "Epee des Ombres", "Une lame qui n'existe que dans les tenebres. Invisible et mortelle.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.SHADOW_DODGE, PassiveSkill.RAZOR_SENSES, PassiveSkill.CRIMSON_BLADE), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                58, 16, 10, 16, 92, 68);
    }
}
