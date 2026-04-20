package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GodlyStats extends TitleModel {
    public GodlyStats() { super("godly_stats", "Stats Divines", "Toutes vos stats depassent les 100 points.", Rarity.DIVINE, TitleCategory.QUEST, new TitleBonus(10, 10, 10, 10, 10, 10), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 100 && data.getVitality() >= 100 && data.getIntelligence() >= 100 && data.getEndurance() >= 100 && data.getAgility() >= 100 && data.getLuck() >= 100; }
}
