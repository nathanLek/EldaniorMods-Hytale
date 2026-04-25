// 2. L'Exécuteur (Unique - Dégâts Brutaux)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Executeur extends ClassModel {
    public Executeur() {
        super("executeur", "Exécuteur", "Le juge et le bourreau. L'Executeur porte des coups destines a tuer instantanement.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.FATAL_PRECISION, PassiveSkill.DEATH_HUNT), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                200, 80, 10, 80, 100, 90);
    }
}