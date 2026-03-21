package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class ProsperityAvatar extends ClassModel {

    public ProsperityAvatar() {
        super(
                "prosperity_avatar",
                "Avatar de la Prospérité",
                "Béni par les dieux, il plie les lois des probabilités à sa simple volonté. L'or coule dans ses veines.",
                Rarity.LEGENDARY,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                null,
                999,
                false,
                100, 100, 120, 100, 150, 800
        );
    }
}