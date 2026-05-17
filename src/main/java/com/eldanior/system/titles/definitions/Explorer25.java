package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Explorer25 extends TitleModel {
    public Explorer25() { super("explorer_25", "Baroudeur", "Decouvrir 25 zones.", Rarity.RARE, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTerritoriesDiscovered() >= 25; }
}
