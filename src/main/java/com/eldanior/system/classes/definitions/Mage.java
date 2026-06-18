package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Mage extends ClassModel {

    public Mage() {
        super(
                "mage",
                "Mage",
                "Maitre des arcanes, le Mage canalise une puissance magique devastatrice. Son intelligence superieure lui permet de manipuler les elements a sa guise.",
                Rarity.COMMON,
                ClassType.MAGE,
                List.of(PassiveSkill.MANA_FONT, PassiveSkill.AWAKENED_MIND),
                List.of("BATON_MAGIQUE"),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("elementaliste", "enchanteur", "necromancien", "invocateur", "guerisseur", "pyromancien", "cryomancien", "archimage", "sorcier", "druide", "illusionniste", "mystique", "thaumaturge", "alchimiste", "sage", "magus", "liche", "oracle", "maitre_elementaire", "chronoturge", "archonte", "mage_void", "primordial", "demiurge", "prophete", "avatar_arcanique", "dieu_des_arcanes"),
                180,
                false,
                2, 4, 20, 4, 4, 2
        );
    }
}
