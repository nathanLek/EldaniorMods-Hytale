package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class ProsperityAvatar extends ClassModel {

    public ProsperityAvatar() {
        super(
                "prosperity_avatar",
                "Avatar de la Prospérité",
                "L'Avatar de la Prosperite incarne la richesse absolue. Sa simple presence fait fleurir le commerce et la fortune.",
                Rarity.LEGENDARY,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.MYTH_HUNTER, PassiveSkill.COSMIC_CONSTITUTION),
                List.of(WeaponMastery.ANY),
                null,
                999,
                false,
                200, 200, 240, 200, 300, 1600
        );
    }
}