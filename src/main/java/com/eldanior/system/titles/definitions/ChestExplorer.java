package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ChestExplorer extends TitleModel {
    public ChestExplorer() { super("chest_explorer", "Explorateur de Coffres", "Vous avez un flair pour les tresors.", Rarity.RARE, TitleCategory.EXPLORATION, new TitleBonus(0,0,0,0,1,1), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 25; }
}
