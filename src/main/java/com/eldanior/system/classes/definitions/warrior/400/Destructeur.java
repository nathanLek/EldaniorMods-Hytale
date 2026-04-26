package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Destructeur extends ClassModel {
    public Destructeur() {
        super("destructeur", "Destructeur", "Le Destructeur incarne la devastation pure. Sa puissance d'attaque phenomenale reduit en cendres tout ce qui se dresse sur son chemin.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.WAR_FRENZY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                152, 68, 7, 68, 50, 27);
    }
}