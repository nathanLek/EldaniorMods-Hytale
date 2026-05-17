package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Bandit50 extends TitleModel {
    public Bandit50() { super("bandit_50", "Justicier", "Tuer 50 bandits.", Rarity.COMMON, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("bandit") >= 50; }
}
