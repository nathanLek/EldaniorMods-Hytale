package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class GuildMaster extends ClassModel {

    public GuildMaster() {
        super(
                "guild_master",
                "Chef de Guilde",
                "Le Maitre de Guilde dirige un reseau commercial puissant. Son influence politique et economique est immense.",
                Rarity.EPIC,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.MARATHON_RUNNER),
                List.of(WeaponMastery.ANY),
                List.of("grand_maitre_de_guilde"),
                800,
                false,
                28, 10, 30, 24, 8, 30
        );
    }
}