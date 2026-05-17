package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class UltimateExplorer extends TitleModel {
    public UltimateExplorer() { super("ultimate_explorer", "Explorateur Ultime", "Decouvrir 200 zones, 50 donjons et 1000 coffres.", Rarity.DIVINE, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTerritoriesDiscovered() >= 200 && data.getDungeonsDiscovered() >= 50 && data.getChestsDiscovered() >= 1000; }
}
