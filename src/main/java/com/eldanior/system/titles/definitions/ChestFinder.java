package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ChestFinder extends TitleModel {
    public ChestFinder() { super("chest_finder", "Decouvreur de Coffres", "Vos premiers tresors.", Rarity.COMMON, TitleCategory.EXPLORATION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 5; }
}
