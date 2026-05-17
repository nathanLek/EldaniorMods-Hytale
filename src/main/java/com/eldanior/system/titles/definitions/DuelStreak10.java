package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DuelStreak10 extends TitleModel {
    public DuelStreak10() { super("duel_streak_10", "Gladiateur Supreme", "Enchainer 10 victoires en duel.", Rarity.EPIC, TitleCategory.DUEL, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDuelBestStreak() >= 10; }
}
