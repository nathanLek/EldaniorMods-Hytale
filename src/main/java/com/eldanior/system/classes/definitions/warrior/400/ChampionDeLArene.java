package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChampionDeLArene extends ClassModel {
    public ChampionDeLArene() {
        super("champion_de_l_arene", "Champion de l'Arene", "Le Champion de l'Arene est l'idole des foules et la terreur de ses adversaires. Sa maitrise du combat spectaculaire et sa ferocite sont inegalees.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.RAZOR_SENSES, PassiveSkill.WAR_FRENZY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                85, 50, 7, 50, 85, 50);
    }
}