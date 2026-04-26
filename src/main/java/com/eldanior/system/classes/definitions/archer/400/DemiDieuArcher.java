package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiDieuArcher extends ClassModel {
    public DemiDieuArcher() {
        super("demi_dieu_archer", "Demi-Dieu Archer", "A mi-chemin entre le mortel et le divin. Ses fleches percent les cieux.",
                Rarity.DIVINE, ClassType.ARCHER, List.of(PassiveSkill.CREATOR_EDGE, PassiveSkill.CREATOR_PRECISION, PassiveSkill.CREATOR_SWIFTNESS), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                255, 204, 102, 136, 595, 595);
    }
}
