package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurDesTenebres extends ClassModel {
    public SeigneurDesTenebres() {
        super("seigneur_des_tenebres", "Seigneur des Tenebres", "Le Seigneur des Tenebres regne sur l'obscurite elle-meme. Sa chasse sanglante et ses pas dimensionnels font de lui un predateur surnaturel.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.DEATH_HUNT, PassiveSkill.SOUL_CRUSHING_PRESSURE, PassiveSkill.VOID_STEP), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                200, 100, 34, 85, 136, 118);
    }
}