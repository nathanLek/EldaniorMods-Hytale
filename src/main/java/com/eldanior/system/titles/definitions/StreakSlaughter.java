package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StreakSlaughter extends TitleModel {
    public StreakSlaughter() { super("streak_slaughter", "Carnage", "15 kills PvP sans mourir.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 0, 5, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBestKillStreak() >= 15; }
}
