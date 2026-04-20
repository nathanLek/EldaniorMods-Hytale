package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StreakUnstoppable extends TitleModel {
    public StreakUnstoppable() { super("streak_unstoppable", "Inarretable", "20 kills PvP sans mourir.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(7, 0, 0, 0, 7, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBestKillStreak() >= 20; }
}
