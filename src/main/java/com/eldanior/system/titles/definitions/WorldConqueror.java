package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class WorldConqueror extends TitleModel {
    public WorldConqueror() { super("world_conqueror", "Conquerant du Monde", "Vous avez soumis chaque espece.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(12, 12, 12, 12, 12, 12), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) {
        if (data.getLevel() < 999) return false;
        for (String k : new String[]{"skeleton","goblin","zombie","trork","outlander","void","spirit","golem","scarak","dragon","saurian"}) {
            if (data.getMobKillCountContaining(k) < 5000) return false;
        }
        return true;
    }
}
