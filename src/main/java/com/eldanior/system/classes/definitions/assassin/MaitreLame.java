package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreLame extends ClassModel {
    public MaitreLame() {
        super("maitre_lame", "Maitre des Lames", "Le Maitre des Lames danse avec deux dagues. Chaque mouvement est un poeme ecrit dans le sang de ses ennemis.",
                Rarity.RARE, ClassType.ASSASSIN,
                List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.RAZOR_SENSES),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of("grand_maitre_lame", "lame_diamant", "duelliste_supreme"), 400, false,
                30, 15, 5, 10, 55, 40);
    }
}
