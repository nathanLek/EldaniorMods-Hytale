package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ExplorerWarrior extends TitleModel {
    public ExplorerWarrior() { super("explorer_warrior", "Guerrier Explorateur", "Vous combattez et explorez sans relache.", Rarity.EPIC, TitleCategory.SPECIAL, new TitleBonus(2, 0, 0, 2, 2, 2), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 50 && data.getTotalMobKills() >= 10000; }
}
