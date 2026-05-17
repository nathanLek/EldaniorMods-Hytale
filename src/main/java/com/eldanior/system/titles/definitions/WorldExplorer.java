package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class WorldExplorer extends TitleModel {
    public WorldExplorer() { super("world_explorer", "Explorateur du Monde", "Decouvrir 100 zones et 20 donjons.", Rarity.LEGENDARY, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTerritoriesDiscovered() >= 100 && data.getDungeonsDiscovered() >= 20; }
}
