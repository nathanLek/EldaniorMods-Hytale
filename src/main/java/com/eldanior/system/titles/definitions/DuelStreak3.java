package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DuelStreak3 extends TitleModel {
    public DuelStreak3() { super("duel_streak_3", "Imbattable", "Enchainer 3 victoires en duel.", Rarity.COMMON, TitleCategory.DUEL, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDuelBestStreak() >= 3; }
}
