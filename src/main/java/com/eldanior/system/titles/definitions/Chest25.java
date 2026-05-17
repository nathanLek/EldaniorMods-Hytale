package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Chest25 extends TitleModel {
    public Chest25() { super("chest_25", "Aventurier Curieux", "Decouvrir 25 coffres.", Rarity.COMMON, TitleCategory.EXPLORATION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 25; }
}
