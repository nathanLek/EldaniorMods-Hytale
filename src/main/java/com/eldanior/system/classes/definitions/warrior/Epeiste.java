package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Epeiste extends ClassModel {

    public Epeiste() {
        super(
                "epeiste",
                "Épéiste",
                "L'Epeiste maitrise l'art de l'escrime avec une elegance mortelle. Chaque mouvement est calcule.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(PassiveSkill.DUELIST_SWIFTNESS, PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.WIND_STEP),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of("lame_celeste", "trancheur", "escrimeur_royal"),
                400,
                false,
                24, 16, 4, 10, 16, 6
        );
    }
}