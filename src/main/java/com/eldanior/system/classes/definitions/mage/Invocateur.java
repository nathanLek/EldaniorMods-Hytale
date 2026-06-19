package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Invocateur extends ClassModel {
    public Invocateur() {
        super("invocateur", "Invocateur", "L'Invocateur fait appel aux esprits et aux creatures d'autres plans pour combattre a ses cotes.",
                Rarity.COMMON, ClassType.MAGE,
                List.of(PassiveSkill.MANA_FONT, PassiveSkill.AWAKENED_MIND, PassiveSkill.TIRELESS_BREATH),
                List.of("FAMILIER_ARCANIQUE", "TOTEM_DE_GARDE", "PORTAIL_DIMENSIONNEL"),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("invocateur_supreme", "conjurateur_maitre", "maitre_d_invocation"), 400, false,
                2, 8, 24, 8, 4, 4);
    }
}
