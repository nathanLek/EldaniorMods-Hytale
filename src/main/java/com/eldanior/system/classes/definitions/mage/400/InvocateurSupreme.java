package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class InvocateurSupreme extends ClassModel {
    public InvocateurSupreme() {
        super("invocateur_supreme", "Invocateur Supreme", "Le plus grand invocateur. Ses creatures sont les plus puissantes.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.AWAKENED_MIND, PassiveSkill.MANA_STREAM, PassiveSkill.EXPANDED_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 14, 40, 14, 7, 7);
    }
}
