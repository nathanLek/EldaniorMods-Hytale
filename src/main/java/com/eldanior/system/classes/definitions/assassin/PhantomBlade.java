package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PhantomBlade extends ClassModel {
    public PhantomBlade() {
        super("phantom_blade", "Lame Fantome", "La Lame Fantome transcende les limites physiques. Ses attaques traversent les armures et frappent l'ame.",
                Rarity.EPIC, ClassType.ASSASSIN,
                List.of(PassiveSkill.CRUSHING_PRESSURE, PassiveSkill.DESTINY_STRIKE, PassiveSkill.STORM_STEP),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of("lame_spectrale", "phantome_eternel", "lame_du_vide"), 400, false,
                60, 25, 15, 25, 110, 70);
    }
}
