package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EnchanteurDeGuerre extends ClassModel {
    public EnchanteurDeGuerre() {
        super("enchanteur_de_guerre", "Enchanteur de Guerre", "L'Enchanteur de Guerre fusionne l'art de la guerre et la magie arcanique. Ses armes enchantees deviennent des conduits de puissance devastatrice.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.BRILLIANT_MIND, PassiveSkill.SPELLBLADE, PassiveSkill.MANA_OCEAN), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                68, 68, 100, 50, 50, 34);
    }
}