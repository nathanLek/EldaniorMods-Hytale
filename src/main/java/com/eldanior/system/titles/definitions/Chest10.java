package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Chest10 extends TitleModel {
    public Chest10() { super("chest_10", "Fouineur", "Decouvrir 10 coffres.", Rarity.COMMON, TitleCategory.EXPLORATION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 10; }
}
