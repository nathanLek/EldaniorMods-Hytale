package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Duelist extends TitleModel {
    public Duelist() { super("duelist", "Duelliste", "50 kills PvP et 0 mort. Le duelliste parfait.", Rarity.LEGENDARY, TitleCategory.SPECIAL, new TitleBonus(5, 0, 0, 0, 5, 5), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 50 && data.getPlayerDeaths() == 0; }
}
