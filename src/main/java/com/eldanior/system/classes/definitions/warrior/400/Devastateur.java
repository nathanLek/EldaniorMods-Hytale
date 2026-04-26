package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Devastateur extends ClassModel {
    public Devastateur() {
        super("devastateur", "Devastateur", "Le Devastateur ne laisse que ruines et desolation dans son sillage. Sa force brute est telle que meme les armures les plus solides cedent sous ses coups.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.DEEP_SLASH, PassiveSkill.SHARP_BLADE, PassiveSkill.SEISMIC_STRIKE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                50, 16, 3, 13, 18, 10);
    }
}