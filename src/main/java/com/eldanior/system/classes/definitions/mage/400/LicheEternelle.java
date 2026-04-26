package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LicheEternelle extends ClassModel {
    public LicheEternelle() {
        super("liche_eternelle", "Liche Eternelle", "Une liche dont l'immortalite est absolue. Meme les dieux la craignent.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.HYDRA_BLOOD, PassiveSkill.HEART_OF_ETERNITY, PassiveSkill.UNBREAKABLE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                24, 90, 186, 100, 36, 82);
    }
}
