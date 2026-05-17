package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class AllStats200 extends TitleModel {
    public AllStats200() { super("all_stats_200", "Divinite", "Toutes les stats a 200+.", Rarity.DIVINE, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 200 && data.getVitality() >= 200 && data.getIntelligence() >= 200 && data.getEndurance() >= 200 && data.getAgility() >= 200 && data.getLuck() >= 200; }
}
