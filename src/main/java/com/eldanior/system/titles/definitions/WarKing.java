package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class WarKing extends TitleModel {
    public WarKing() { super("war_king", "Roi de Guerre", "Roi avec 500 kills PvP.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(10, 5, 0, 5, 10, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 500 && "ROI".equals(data.getNobilityRank()); }
}
