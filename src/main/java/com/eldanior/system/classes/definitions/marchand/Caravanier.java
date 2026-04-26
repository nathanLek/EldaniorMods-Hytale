package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class Caravanier extends ClassModel {

    public Caravanier() {
        super(
                "caravanier",
                "Caravanier",
                "Le Caravanier parcourt les routes commerciales les plus dangereuses. Sa robustesse et sa chance le protegent.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.TIRELESS_BREATH, PassiveSkill.TREASURE_HUNTER),
                List.of(WeaponMastery.ANY),
                List.of("grand_caravanier"),
                400,
                false,
                6, 8, 4, 8, 8, 18
        );
    }
}