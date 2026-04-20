package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class RoyalGuard extends TitleModel {
    public RoyalGuard() { super("royal_guard", "Garde Royal", "Chevalier au service du Roi.", Rarity.RARE, TitleCategory.SOCIAL, new TitleBonus(2, 2, 0, 2, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return "CHEVALIER".equals(data.getNobilityRank()) && data.getLevel() >= 50; }
}
