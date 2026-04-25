package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OmbreFurtive extends ClassModel {
    public OmbreFurtive() {
        super("ombre_furtive", "Ombre Furtive", "L'Ombre Furtive se fond dans les tenebres. Invisible et silencieuse, elle frappe sans jamais etre vue.",
                Rarity.RARE, ClassType.ASSASSIN,
                List.of(PassiveSkill.PHANTOM_DODGE, PassiveSkill.GALE_STEP, PassiveSkill.RELENTLESS_HUNT),
                List.of(WeaponMastery.DAGGER),
                List.of(), 250, false,
                20, 15, 5, 10, 60, 45);
    }
}
