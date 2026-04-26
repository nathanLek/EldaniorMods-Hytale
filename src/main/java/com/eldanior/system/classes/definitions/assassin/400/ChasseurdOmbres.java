package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChasseurdOmbres extends ClassModel {
    public ChasseurdOmbres() {
        super("chasseur_d_ombres", "Chasseur d'Ombres", "Il traque meme les ombres. Personne ne peut se cacher de lui.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.DARK_VISION, PassiveSkill.RAZOR_SENSES, PassiveSkill.BLOOD_HUNT), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                36, 24, 10, 24, 84, 84);
    }
}
