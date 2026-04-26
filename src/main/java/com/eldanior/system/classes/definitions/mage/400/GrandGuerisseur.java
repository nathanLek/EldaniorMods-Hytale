package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandGuerisseur extends ClassModel {
    public GrandGuerisseur() {
        super("grand_guerisseur", "Grand Guerisseur", "Le plus grand guerisseur connu. Ses soins guerissent meme les blessures de l'ame.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.NATURAL_RECOVERY, PassiveSkill.VITAL_RECOVERY, PassiveSkill.OVERFLOWING_LIFE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 24, 34, 14, 4, 7);
    }
}
