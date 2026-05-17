package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendUltimateWarrior extends TitleModel {
    public LegendUltimateWarrior() { super("legend_ultimate_warrior", "Guerrier Ultime", "Force 150+ et 50000 mobs tues.", Rarity.LEGENDARY, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 150 && data.getTotalMobKills() >= 50000; }
}
