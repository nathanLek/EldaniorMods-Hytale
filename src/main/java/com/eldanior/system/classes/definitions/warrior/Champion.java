package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import java.util.List;

public class Champion extends ClassModel {
    public Champion() {
        super("champion", "Champion", "Un combattant d'exception, idolâtré par les foules.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(), List.of(), 250, false,
                60, 40, 10, 40, 30, 20);
    }
}