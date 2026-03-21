// 3. Le Gardien Runique (Unique - Tank Magique)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import java.util.List;

public class GardienRunique extends ClassModel {
    public GardienRunique() {
        super("gardien_runique", "Gardien Runique", "Son armure est gravée de sceaux le rendant insensible aux éléments.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(), List.of(), 250, false,
                40, 80, 60, 80, 10, 10);
    }
}