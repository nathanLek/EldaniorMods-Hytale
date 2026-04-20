package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpAddict extends TitleModel {
    public PvpAddict() { super("pvp_addict", "Accro au PvP", "1000 combats PvP (kills + morts).", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 3, 0, 3, 3, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return (data.getPlayerKills() + data.getPlayerDeaths()) >= 1000; }
}
