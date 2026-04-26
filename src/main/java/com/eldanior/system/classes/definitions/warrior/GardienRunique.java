// 3. Le Gardien Runique (Unique - Tank Magique)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GardienRunique extends ClassModel {
    public GardienRunique() {
        super("gardien_runique", "Gardien Runique", "Son armure est gravee de sceaux le rendant insensible aux elements. Le Gardien Runique allie magie et defense.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.DIAMOND_BODY, PassiveSkill.GENIUS_MIND, PassiveSkill.MANA_CITADEL), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of("archon_runique", "gardien_primordial", "sage_de_guerre"), 400, false,
                80, 160, 120, 160, 20, 20);
    }
}