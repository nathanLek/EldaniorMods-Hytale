package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;

public class ZombieSurvivor extends TitleModel {
    public ZombieSurvivor() {
        super("zombie_survivor", "Survivant de la Horde", "Vous avez survecu aux hordes de morts-vivants.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(0, 2, 0, 1, 0, 0), List.of());
    }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("zombie") >= 100; }
}