package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GardienSacre extends ClassModel {
    public GardienSacre() {
        super("gardien_sacre", "Gardien Sacre", "Le Gardien Sacre protege ses allies avec une devotion indefectible. Son corps est un rempart beni que meme les forces du mal ne peuvent briser.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.FORTIFIED_SKIN, PassiveSkill.ROBUST_CONSTITUTION, PassiveSkill.VITAL_RECOVERY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                20, 40, 3, 30, 10, 7);
    }
}