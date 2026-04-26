package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ProspecteurRoyal extends ClassModel {
    public ProspecteurRoyal() {
        super("prospecteur_royal", "Prospecteur Royal", "Le prospecteur du roi, decouvreur de tresors legendaires.",
                Rarity.RARE, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.RELIC_HUNTER, PassiveSkill.EAGLE_EYE), List.of(WeaponMastery.ANY), List.of(), 400, false,
                7, 10, 7, 10, 14, 38);
    }
}
