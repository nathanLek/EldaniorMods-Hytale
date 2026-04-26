package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GardienPrimordial extends ClassModel {
    public GardienPrimordial() {
        super("gardien_primordial", "Gardien Primordial", "Le Gardien Primordial protege les secrets ancestraux depuis l'aube des temps. Son corps diamante et sa forteresse de mana repoussent toute intrusion.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.DIAMOND_BODY, PassiveSkill.AWAKENED_MIND, PassiveSkill.MANA_CITADEL), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                132, 266, 200, 268, 32, 36);
    }
}