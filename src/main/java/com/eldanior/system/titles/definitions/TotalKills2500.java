package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TotalKills2500 extends TitleModel {
    public TotalKills2500() { super("total_kills_2500", "Tueur en Serie", "Tuer 2500 monstres.", Rarity.RARE, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 2500; }
}
