package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Enchanteur extends ClassModel {
    public Enchanteur() {
        super("enchanteur", "Enchanteur", "L'Enchanteur tisse des sortileges de protection et de renforcement. Sa magie soutient ses allies dans les moments critiques.",
                Rarity.COMMON, ClassType.MAGE,
                List.of(PassiveSkill.MANA_BARRIER, PassiveSkill.MANA_FONT, PassiveSkill.ROBUST_CONSTITUTION),
                List.of("LIEN_ENCHANTEMENT"),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("grand_enchanteur", "tisseur_de_runes", "enchanteur_royal"), 400, false,
                2, 10, 22, 8, 4, 4);
    }
}
