package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpExecutioner extends TitleModel {
    public PvpExecutioner() { super("pvp_executioner", "Bourreau", "2000 joueurs executes.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(9, 0, 0, 4, 9, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 2000; }
}
