package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class AllStats100 extends TitleModel {
    public AllStats100() { super("all_stats_100", "Perfection Absolue", "Toutes les stats a 100+.", Rarity.LEGENDARY, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 100 && data.getVitality() >= 100 && data.getIntelligence() >= 100 && data.getEndurance() >= 100 && data.getAgility() >= 100 && data.getLuck() >= 100; }
}
