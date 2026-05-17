package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Chest750 extends TitleModel {
    public Chest750() { super("chest_750", "Roi des Tresors", "Decouvrir 750 coffres.", Rarity.UNIQUE, TitleCategory.EXPLORATION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 750; }
}
