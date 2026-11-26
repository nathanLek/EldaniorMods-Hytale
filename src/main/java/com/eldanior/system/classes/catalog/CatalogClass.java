package com.eldanior.system.classes.catalog;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

/** Classe ordinaire du catalogue (ClassCatalog, genere par scripts/classes/class_catalog.py depuis classes.json). */
final class CatalogClass extends ClassModel {

    CatalogClass(String id, String name, String desc, Rarity rarity, ClassType type, int tier, int promotion,
                 List<PassiveSkill.Ranked> passives, List<WeaponMastery> weapons, List<String> next, int[] s,
                 String[] runes, String[] metiers) {
        super(id, name, desc, rarity, type, passives, weapons, next, promotion, false, s[0], s[1], s[2], s[3], s[4], s[5]);
        tier(tier);
        if (runes.length > 0) runes(runes);
        if (metiers.length > 0) metiers(metiers);
    }
}
