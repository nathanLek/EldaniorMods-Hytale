package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GardiendelaNature extends ClassModel {
    public GardiendelaNature() {
        super("gardien_de_la_nature", "Gardien de la Nature", "Le gardien ultime de la nature dont les pouvoirs regenerent la terre.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.ENRICHED_BLOOD, PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.MANA_STREAM), List.of(WeaponMastery.STAFF), List.of(), 400, false,
                12, 66, 74, 50, 24, 36);
    }
}
