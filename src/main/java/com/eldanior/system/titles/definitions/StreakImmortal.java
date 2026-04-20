package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StreakImmortal extends TitleModel {
    public StreakImmortal() { super("streak_immortal", "Immortel du PvP", "100 kills PvP sans mourir. Impossible.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(15, 0, 0, 8, 15, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBestKillStreak() >= 100; }
}
