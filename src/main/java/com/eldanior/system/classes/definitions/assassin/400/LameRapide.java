package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameRapide extends ClassModel {
    public LameRapide() {
        super("lame_rapide", "Lame Rapide", "Sa lame frappe si vite que ses victimes ne sentent rien avant de tomber.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.THUNDER_REFLEXES, PassiveSkill.DUELIST_SWIFTNESS, PassiveSkill.SHARP_BLADE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                16, 8, 4, 4, 32, 18);
    }
}
