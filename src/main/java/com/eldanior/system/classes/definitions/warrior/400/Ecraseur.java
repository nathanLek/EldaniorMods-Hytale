package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Ecraseur extends ClassModel {
    public Ecraseur() {
        super("ecraseur", "Ecraseur", "L'Ecraseur pulverise tout sur son passage avec une force titanesque. Ses coups sont si puissants qu'ils font trembler le sol sous les pieds de ses ennemis.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.DEEP_SLASH, PassiveSkill.SEISMIC_STRIKE, PassiveSkill.IRON_BODY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                50, 34, 3, 13, 13, 7);
    }
}