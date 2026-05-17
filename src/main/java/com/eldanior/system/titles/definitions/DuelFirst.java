package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DuelFirst extends TitleModel {
    public DuelFirst() { super("duel_first", "Premier Duel", "Gagner 1 duel.", Rarity.COMMON, TitleCategory.DUEL, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDuelWins() >= 1; }
}
