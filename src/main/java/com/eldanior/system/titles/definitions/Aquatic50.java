package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Aquatic50 extends TitleModel {
    public Aquatic50() { super("aquatic_50", "Pecheur Aguerri", "Tuer 50 creatures aquatiques.", Rarity.COMMON, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("fish") + data.getMobKillCountContaining("shark") + data.getMobKillCountContaining("squid") + data.getMobKillCountContaining("crab") >= 50; }
}
