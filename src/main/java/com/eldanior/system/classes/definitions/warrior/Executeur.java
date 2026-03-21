// 2. L'Exécuteur (Unique - Dégâts Brutaux)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import java.util.List;

public class Executeur extends ClassModel {
    public Executeur() {
        super("executeur", "Exécuteur", "Le juge et le bourreau. Ses coups sont portés pour tuer instantanément.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(), List.of(), 250, false,
                100, 40, 5, 40, 50, 45);
    }
}