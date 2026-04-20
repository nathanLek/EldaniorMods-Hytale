package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StreakLegendary extends TitleModel {
    public StreakLegendary() { super("streak_legendary", "Serie Legendaire", "30 kills PvP sans mourir.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(8, 0, 0, 3, 8, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBestKillStreak() >= 30; }
}
