package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Epeiste extends ClassModel {

    public Epeiste() {
        super(
                "epeiste",
                "Épéiste",
                "Un guerrier qui affine sa technique a l'epee, alliant force et precision.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                12, 8, 2, 5, 8, 3
        );
    }
}