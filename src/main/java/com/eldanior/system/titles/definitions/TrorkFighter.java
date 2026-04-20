package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TrorkFighter extends TitleModel {
    public TrorkFighter() { super("trork_fighter", "Combattant des Trorks", "Les Trorks reconnaissent votre force.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(2, 0, 0, 0, 1, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("trork") >= 100; }
}