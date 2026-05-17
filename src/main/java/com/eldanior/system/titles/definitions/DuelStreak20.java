package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DuelStreak20 extends TitleModel {
    public DuelStreak20() { super("duel_streak_20", "Dieu de l'Arene", "Enchainer 20 victoires en duel.", Rarity.LEGENDARY, TitleCategory.DUEL, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDuelBestStreak() >= 20; }
}
