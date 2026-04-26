package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class FaucheseDeGuerre extends ClassModel {
    public FaucheseDeGuerre() {
        super("fauchese_de_guerre", "Faucheuse de Guerre", "La Faucheuse de Guerre moissonne les vies sur le champ de bataille comme du ble. Sa lame abyssale et ses sens aiguises font d'elle l'incarnation de la mort.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.DEATH_HUNT), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                336, 132, 16, 132, 168, 154);
    }
}