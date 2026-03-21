package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import java.util.List;

public class DivineApotre extends ClassModel {
    public DivineApotre() {
        super("DivineApotre", "Apotre Divin", "Dieu t'accord ça bénédiction. Il semble placer de grand espoir en toi.",
                Rarity.DIVINE, ClassType.WARRIOR, List.of(), List.of(), 250, false,
                220, 180, 140, 180, 180, 240);
    }
}