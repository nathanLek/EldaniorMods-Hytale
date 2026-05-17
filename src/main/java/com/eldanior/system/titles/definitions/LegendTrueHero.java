package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendTrueHero extends TitleModel {
    public LegendTrueHero() { super("legend_true_hero", "Vrai Heros", "100000 mobs tues et 0 kills PvP.", Rarity.LEGENDARY, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 100000 && data.getPlayerKills() == 0; }
}
