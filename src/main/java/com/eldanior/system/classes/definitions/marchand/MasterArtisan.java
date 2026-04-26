package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class MasterArtisan extends ClassModel {

    public MasterArtisan() {
        super(
                "master_artisan",
                "Maître Artisan",
                "Le Maitre Artisan forge des armes et armures d'une qualite inegalee. Ses creations sont recherchees dans tout le royaume.",
                Rarity.RARE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.IRON_RESOLVE, PassiveSkill.TREASURE_HUNTER),
                List.of(WeaponMastery.ANY),
                List.of("artisan_legendaire"),
                500,
                false,
                12, 4, 16, 14, 6, 24
        );
    }
}