package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArchonRunique extends ClassModel {
    public ArchonRunique() {
        super("archon_runique", "Archon Runique", "L'Archon Runique maitrise les runes primordiales qui faconnent la realite. Son corps cosmique et son esprit infini canalisent une puissance arcanique absolue.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.COSMIC_BODY, PassiveSkill.GENIUS_MIND, PassiveSkill.MANA_INFINITY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                136, 270, 204, 270, 34, 34);
    }
}