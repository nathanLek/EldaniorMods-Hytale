package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Cryomancien extends ClassModel {
    public Cryomancien() {
        super("cryomancien", "Cryomancien", "Le Cryomancien commande le froid absolu. Ses sorts gerent ses ennemis dans une prison de glace eternelle.",
                Rarity.COMMON, ClassType.MAGE,
                List.of(PassiveSkill.MANA_BARRIER, PassiveSkill.STONE_SKIN, PassiveSkill.AWAKENED_MIND),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("maitre_du_givre", "glacial_mage", "seigneur_du_froid"), 400, false,
                2, 8, 24, 6, 6, 4);
    }
}
