package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PropheteduDestin extends ClassModel {
    public PropheteduDestin() {
        super("prophete_du_destin", "Prophete du Destin", "Le prophete du destin qui connait la fin de toute chose.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.DESTINY_STRIKE, PassiveSkill.CREATOR_PRECISION, PassiveSkill.GENIUS_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                20, 68, 178, 54, 68, 136);
    }
}
