package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ThousandKills extends TitleModel {
    public ThousandKills() { super("thousand_kills", "Millieme Victime", "Mille ames fauchees.", Rarity.COMMON, TitleCategory.COMBAT, new TitleBonus(1,0,0,1,0,0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 1000; }
}
