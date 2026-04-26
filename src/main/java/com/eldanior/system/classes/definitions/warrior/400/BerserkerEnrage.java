package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class BerserkerEnrage extends ClassModel {
    public BerserkerEnrage() {
        super("berserker_enrage", "Berserker Enrage", "Le Berserker Enrage a perdu toute notion de controle. Sa fureur depasse les limites humaines, transformant chaque coup en une explosion de violence pure.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.DEEP_SLASH, PassiveSkill.PREDATORY_STRIKE, PassiveSkill.IRON_LUNGS), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                46, 17, 3, 13, 20, 10);
    }
}