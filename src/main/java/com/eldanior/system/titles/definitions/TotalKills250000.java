package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TotalKills250000 extends TitleModel {
    public TotalKills250000() { super("total_kills_250000", "Armageddon", "Tuer 250000 monstres.", Rarity.LEGENDARY, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 250000; }
}
