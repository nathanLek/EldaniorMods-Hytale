package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DuellisteSupreme extends ClassModel {
    public DuellisteSupreme() {
        super("duelliste_supreme", "Duelliste Supreme", "Invaincu en duel, sa technique est absolute.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.STEEL_NERVES, PassiveSkill.LIGHTNING_REFLEXES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                54, 22, 8, 14, 92, 72);
    }
}
