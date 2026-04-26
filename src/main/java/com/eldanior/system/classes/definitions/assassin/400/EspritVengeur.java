package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EspritVengeur extends ClassModel {
    public EspritVengeur() {
        super("esprit_vengeur", "Esprit Vengeur", "Un esprit anime par une vengeance eternelle. Implacable et immortel.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.SOUL_CRUSHING_PRESSURE, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                140, 88, 28, 64, 276, 196);
    }
}
