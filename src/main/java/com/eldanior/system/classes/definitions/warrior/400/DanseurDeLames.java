
package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DanseurDeLames extends ClassModel {
    public DanseurDeLames() {
        super("danseur_de_lames", "Danseur de Lames", "Le Danseur de Lames transforme le combat en une danse mortelle. Ses mouvements fluides et hypnotiques dissimulent des attaques devastatrices.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.GALE_STEP, PassiveSkill.RAZOR_SENSES, PassiveSkill.ACROBATIC_POISE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                64, 48, 17, 50, 120, 64);
    }
}