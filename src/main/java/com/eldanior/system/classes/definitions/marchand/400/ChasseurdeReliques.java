package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChasseurdeReliques extends ClassModel {
    public ChasseurdeReliques() {
        super("chasseur_de_reliques", "Chasseur de Reliques", "Le chasseur de reliques supreme dont les decouvertes changent le monde.",
                Rarity.EPIC, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.LEGEND_HUNTER, PassiveSkill.HAWK_EYE), List.of(WeaponMastery.ANY), List.of(), 400, false,
                34, 20, 34, 28, 42, 122);
    }
}
