package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Chest2500 extends TitleModel {
    public Chest2500() { super("chest_2500", "Dieu des Tresors", "Decouvrir 2500 coffres.", Rarity.DIVINE, TitleCategory.EXPLORATION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 2500; }
}
