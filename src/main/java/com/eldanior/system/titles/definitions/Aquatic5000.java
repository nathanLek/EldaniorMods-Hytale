package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Aquatic5000 extends TitleModel {
    public Aquatic5000() { super("aquatic_5000", "Roi des Oceans", "Tuer 5000 creatures aquatiques.", Rarity.EPIC, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("fish") + data.getMobKillCountContaining("shark") + data.getMobKillCountContaining("squid") + data.getMobKillCountContaining("crab") >= 5000; }
}
