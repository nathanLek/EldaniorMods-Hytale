package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Explorer10 extends TitleModel {
    public Explorer10() { super("explorer_10", "Voyageur", "Decouvrir 10 zones.", Rarity.COMMON, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTerritoriesDiscovered() >= 10; }
}
