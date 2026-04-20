package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StreakRampage extends TitleModel {
    public StreakRampage() { super("streak_rampage", "Dechaîne", "5 kills PvP sans mourir.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(2, 0, 0, 0, 2, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBestKillStreak() >= 5; }
}
