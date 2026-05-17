package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DuelStreak5 extends TitleModel {
    public DuelStreak5() { super("duel_streak_5", "Invaincu", "Enchainer 5 victoires en duel.", Rarity.RARE, TitleCategory.DUEL, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDuelBestStreak() >= 5; }
}
