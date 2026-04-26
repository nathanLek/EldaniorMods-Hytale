package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SagittaireElemental extends ClassModel {
    public SagittaireElemental() {
        super("sagittaire_elemental", "Sagittaire Elemental", "Le sagittaire maitre de tous les elements.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.ARCANE_DEVASTATION, PassiveSkill.BRILLIANT_MIND, PassiveSkill.VOID_STEP), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                48, 50, 90, 32, 118, 100);
    }
}
