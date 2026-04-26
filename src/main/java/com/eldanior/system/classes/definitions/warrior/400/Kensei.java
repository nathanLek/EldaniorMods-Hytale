package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Kensei extends ClassModel {
    public Kensei() {
        super("kensei", "Kensei", "Le Kensei est le saint de l'epee, un maitre absolu de la voie du sabre. Sa lame ne frappe qu'une seule fois, car une seule fois suffit toujours.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.SWORD_MASTERY), List.of(WeaponMastery.SWORD, WeaponMastery.DAGGER), List.of(), 400, false,
                76, 42, 17, 34, 100, 68);
    }
}