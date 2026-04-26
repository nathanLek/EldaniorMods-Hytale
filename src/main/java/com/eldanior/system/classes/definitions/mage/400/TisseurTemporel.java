package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TisseurTemporel extends ClassModel {
    public TisseurTemporel() {
        super("tisseur_temporel", "Tisseur Temporel", "Il tisse le temps comme un fil. Le passe et le futur sont ses outils.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.DIMENSIONAL_STEP, PassiveSkill.GENIUS_MIND, PassiveSkill.FATE_DODGE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                28, 68, 178, 54, 126, 82);
    }
}
