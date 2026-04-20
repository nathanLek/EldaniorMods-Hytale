package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PerfectStats extends TitleModel {
    public PerfectStats() { super("perfect_stats", "Stats Parfaites", "Toutes vos stats depassent les 50 points.", Rarity.LEGENDARY, TitleCategory.QUEST, new TitleBonus(5, 5, 5, 5, 5, 5), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 50 && data.getVitality() >= 50 && data.getIntelligence() >= 50 && data.getEndurance() >= 50 && data.getAgility() >= 50 && data.getLuck() >= 50; }
}
