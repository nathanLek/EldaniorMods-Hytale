package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class AllStats50 extends TitleModel {
    public AllStats50() { super("all_stats_50", "Equilibre Parfait", "Toutes les stats a 50+.", Rarity.EPIC, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 50 && data.getVitality() >= 50 && data.getIntelligence() >= 50 && data.getEndurance() >= 50 && data.getAgility() >= 50 && data.getLuck() >= 50; }
}
