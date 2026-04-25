package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameMage extends ClassModel {

    public LameMage() {
        super(
                "lame_mage",
                "Lame-Mage",
                "La Lame-Mage fusionne l'art de l'epee et la puissance arcanique. Ses attaques sont impregnees de magie devastatrice.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.SPELLBLADE, PassiveSkill.MANA_STREAM),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                40, 40, 60, 30, 30, 20
        );
    }
}