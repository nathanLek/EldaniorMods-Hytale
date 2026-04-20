package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StreakGodlike extends TitleModel {
    public StreakGodlike() { super("streak_godlike", "Divin", "50 kills PvP sans mourir.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(10, 0, 0, 5, 10, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBestKillStreak() >= 50; }
}
