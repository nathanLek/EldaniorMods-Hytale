package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class BalancedTitle extends TitleModel {
    public BalancedTitle() { super("balanced", "Equilibre", "Toutes vos stats depassent les 25 points.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(3, 3, 3, 3, 3, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 25 && data.getVitality() >= 25 && data.getIntelligence() >= 25 && data.getEndurance() >= 25 && data.getAgility() >= 25 && data.getLuck() >= 25; }
}
