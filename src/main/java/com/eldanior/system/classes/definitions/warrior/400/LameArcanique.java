package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameArcanique extends ClassModel {
    public LameArcanique() {
        super("lame_arcanique", "Lame Arcanique", "La Lame Arcanique canalise l'energie magique pure a travers son epee. Chaque coup libere une deflagration de mana qui consume tout sur son passage.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.SPELLBLADE, PassiveSkill.MANA_STREAM, PassiveSkill.GENIUS_MIND), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                66, 64, 102, 48, 52, 32);
    }
}