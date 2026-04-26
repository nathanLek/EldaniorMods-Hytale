package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameduVide extends ClassModel {
    public LameduVide() {
        super("lame_du_vide", "Lame du Vide", "Forgee dans le neant, sa lame decoupe la realite elle-meme.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.VOID_STEP, PassiveSkill.SOUL_CRUSHING_PRESSURE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                96, 46, 28, 46, 184, 126);
    }
}
