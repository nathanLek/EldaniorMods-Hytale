// 1. Le Sang-Dragon (Unique - Hybride/Bruiser)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import java.util.List;

public class SangDragon extends ClassModel {
    public SangDragon() {
        super("sang_dragon", "Sang-Dragon", "Un guerrier dont les veines brûlent d'une magie draconique antique. Vous avez été choisi par le dragon Ancestral",
                Rarity.DIVINE, ClassType.WARRIOR, List.of(), List.of(), 250, false,
                220, 160, 220, 150, 90, 220);
    }
}