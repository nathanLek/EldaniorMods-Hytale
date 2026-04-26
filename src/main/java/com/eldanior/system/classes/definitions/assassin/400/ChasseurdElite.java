package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChasseurdElite extends ClassModel {
    public ChasseurdElite() {
        super("chasseur_d_elite", "Chasseur d'Elite", "Un predateur d'elite forme pour traquer les cibles les plus dangereuses.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.HAWK_EYE, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW, WeaponMastery.SWORD), List.of(), 400, false,
                44, 34, 8, 26, 78, 78);
    }
}
