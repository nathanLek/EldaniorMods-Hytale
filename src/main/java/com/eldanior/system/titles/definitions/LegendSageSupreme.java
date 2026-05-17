package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendSageSupreme extends TitleModel {
    public LegendSageSupreme() { super("legend_sage_supreme", "Sage Supreme", "Intelligence 150+ et Foi 500+.", Rarity.LEGENDARY, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getIntelligence() >= 150 && data.getFaith() >= 500; }
}
