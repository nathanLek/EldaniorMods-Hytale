package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Archonte extends ClassModel {
    public Archonte() {
        super("archonte", "Archonte", "L'Archonte detient une autorite supreme sur les forces magiques. Sa volonte façonne la realite elle-meme.",
                Rarity.UNIQUE, ClassType.MAGE,
                List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.MANA_CITADEL),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("archonte_supreme", "seigneur_arcanique", "pilier_des_mondes"), 400, false,
                30, 80, 200, 80, 40, 60);
    }
}
