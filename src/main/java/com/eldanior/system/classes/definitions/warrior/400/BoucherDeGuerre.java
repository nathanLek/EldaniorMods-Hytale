package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class BoucherDeGuerre extends ClassModel {
    public BoucherDeGuerre() {
        super("boucher_de_guerre", "Boucher de Guerre", "Le Boucher de Guerre est une machine a tuer sans pitie. Sa brutalite est legendaire et ses ennemis prient pour ne jamais le croiser sur le champ de bataille.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.STONE_SKIN, PassiveSkill.SHARP_BLADE, PassiveSkill.BATTLE_FRENZY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                52, 32, 3, 14, 13, 7);
    }
}