package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class SaurianTracker extends TitleModel {
    public SaurianTracker() { super("saurian_tracker", "Traqueur de Sauriens", "Vous avez piste les reptiles d'Eldanior.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(1, 0, 0, 0, 2, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("saurian") >= 100; }
}
