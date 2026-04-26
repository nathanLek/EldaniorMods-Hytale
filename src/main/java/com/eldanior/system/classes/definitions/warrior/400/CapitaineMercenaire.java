package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CapitaineMercenaire extends ClassModel {
    public CapitaineMercenaire() {
        super("capitaine_mercenaire", "Capitaine Mercenaire", "Le Capitaine Mercenaire dirige sa compagnie avec ruse et autorite. Son flair pour les opportunites et sa chance legendaire font de chaque contrat un succes.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.KEEN_SENSES, PassiveSkill.CRITICAL_LUCK, PassiveSkill.COMBAT_INTUITION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                34, 20, 7, 17, 33, 27);
    }
}