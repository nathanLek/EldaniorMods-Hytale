package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class MarchandDivin extends ClassModel {

    public MarchandDivin() {
        super(
                "marchand_divin",
                "Marchand Divin",
                "Le Marchand Divin transcende le commerce mortel. Il echange des ames et des destins comme d'autres echangent des pieces.",
                Rarity.DIVINE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.CREATOR_PRECISION, PassiveSkill.FATE_DODGE),
                List.of(WeaponMastery.ANY),
                List.of(),
                250,
                false,
                200, 200, 300, 200, 300, 1200
        );
    }
}