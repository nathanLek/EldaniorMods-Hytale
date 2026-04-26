package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Apocalypse extends ClassModel {
    public Apocalypse() {
        super("apocalypse", "Apocalypse", "L'Apocalypse est l'incarnation de la fin des temps. Sa lame de genese et sa vitesse divine annoncent la destruction totale de tout ce qui existe.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.GOD_SLAYER_SWIFTNESS, PassiveSkill.SOUL_CRUSHING_PRESSURE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                476, 204, 68, 204, 204, 136);
    }
}