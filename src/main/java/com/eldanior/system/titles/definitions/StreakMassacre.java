package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StreakMassacre extends TitleModel {
    public StreakMassacre() { super("streak_massacre", "Massacre", "7 kills PvP sans mourir.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 0, 3, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBestKillStreak() >= 7; }
}
