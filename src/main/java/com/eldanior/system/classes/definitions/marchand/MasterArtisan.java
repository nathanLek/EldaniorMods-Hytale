package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class MasterArtisan extends ClassModel {

    public MasterArtisan() {
        super(
                "master_artisan",
                "Maître Artisan",
                "Un travailleur acharné dont le corps s'est endurci à la tâche.",
                Rarity.RARE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                List.of("guild_master"),
                500,
                false,
                6, 2, 8, 7, 3, 12
        );
    }
}