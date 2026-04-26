package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Oracle extends ClassModel {
    public Oracle() {
        super("oracle", "Oracle", "L'Oracle voit au-dela du voile du temps. Ses visions lui permettent d'anticiper chaque mouvement ennemi.",
                Rarity.EPIC, ClassType.MAGE,
                List.of(PassiveSkill.EAGLE_VISION, PassiveSkill.DESTINY_STRIKE, PassiveSkill.PURE_MAGIC),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("oracle_absolu", "visionnaire_supreme", "prophete_du_destin"), 400, false,
                10, 40, 100, 30, 40, 80);
    }
}
