package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class FleauDeGuerre extends ClassModel {
    public FleauDeGuerre() {
        super("fleau_de_guerre", "Fleau de Guerre", "Le Fleau de Guerre seme la terreur partout ou il passe. Sa lame affutee et sa chasse impitoyable ne laissent aucun survivant.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.BATTLE_FRENZY, PassiveSkill.BLOOD_HUNT), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                148, 66, 7, 66, 52, 26);
    }
}