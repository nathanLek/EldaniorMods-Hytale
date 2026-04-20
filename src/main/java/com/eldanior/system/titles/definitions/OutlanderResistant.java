package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class OutlanderResistant extends TitleModel {
    public OutlanderResistant() { super("outlander_resistant", "Resistant aux Outlanders", "Vous tenez bon face aux envahisseurs.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(2, 0, 0, 2, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("outlander") >= 100; }
}