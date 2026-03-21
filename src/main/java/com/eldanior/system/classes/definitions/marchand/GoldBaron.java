package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class GoldBaron extends ClassModel {

    public GoldBaron() {
        super(
                "gold_baron",
                "Baron de l'Or",
                "La chance incarnée. Les dieux de la fortune veillent personnellement sur lui.",
                Rarity.EPIC,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                List.of("prosperity_avatar"),
                800,
                false,
                6, 6, 8, 6, 10, 45 // La Chance crève le plafond !
        );
    }
}