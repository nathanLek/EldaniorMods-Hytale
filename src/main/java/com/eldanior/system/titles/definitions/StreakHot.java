package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StreakHot extends TitleModel {
    public StreakHot() { super("streak_hot", "En Feu", "3 kills PvP sans mourir.", Rarity.COMMON, TitleCategory.COMBAT, new TitleBonus(1, 0, 0, 0, 1, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBestKillStreak() >= 3; }
}
