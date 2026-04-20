package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Predator extends TitleModel {
    public Predator() { super("predator", "Predateur", "Level 300+ et streak de 10+.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 0, 8, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 300 && data.getBestKillStreak() >= 10; }
}
