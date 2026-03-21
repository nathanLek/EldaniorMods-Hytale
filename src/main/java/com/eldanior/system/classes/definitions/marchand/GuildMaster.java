package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class GuildMaster extends ClassModel {

    public GuildMaster() {
        super(
                "guild_master",
                "Chef de Guilde",
                "Une figure d'autorité intouchable, véritable forteresse vivante.",
                Rarity.EPIC,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                List.of("world_forger"),
                800,
                false,
                14, 5, 15, 12, 4, 15
        );
    }
}