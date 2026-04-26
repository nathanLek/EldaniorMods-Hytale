package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EscrimeurRoyal extends ClassModel {
    public EscrimeurRoyal() {
        super("escrimeur_royal", "Escrimeur Royal", "L'Escrimeur Royal incarne la noblesse du combat a l'epee. Forme dans les plus grandes academies, son style est aussi mortel qu'elegant.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.SWORD_MASTERY, PassiveSkill.GALE_STEP), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                38, 28, 7, 16, 27, 10);
    }
}