package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandMaitreLame extends ClassModel {
    public GrandMaitreLame() {
        super("grand_maitre_lame", "Grand Maitre Lame", "Le sommet de l'art du sabre. Ses techniques sont inscrites dans la legende.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.RAZOR_SENSES, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                52, 26, 8, 18, 96, 70);
    }
}
