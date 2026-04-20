package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpWarlord extends TitleModel {
    public PvpWarlord() { super("pvp_warlord", "Seigneur de Guerre PvP", "500 joueurs vaincus.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(6, 0, 0, 2, 6, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 500; }
}
