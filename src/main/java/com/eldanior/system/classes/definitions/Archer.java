package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Archer extends ClassModel {

    public Archer() {
        super(
                "archer",
                "Archer",
                "L'Archer maitrise le combat a distance avec une precision chirurgicale. Son oeil de faucon et sa dexterite lui permettent d'atteindre n'importe quelle cible.",
                Rarity.COMMON,
                ClassType.ARCHER,
                List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.LIGHT_REFLEXES),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER),
                List.of("tireur", "chasseur_archer", "arbaletrier", "eclaireur_archer", "franc_tireur", "ranger_elite", "arc_mystique", "maitre_chasse", "sniper_divin", "tireur_elementaire", "general_archer", "oeil_de_faucon", "arc_ancien", "avatar_arc", "dieu_arc"),
                180,
                false,
                4, 4, 4, 2, 12, 10
        );
    }
}
