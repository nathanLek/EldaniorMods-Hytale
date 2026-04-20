package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpDestroyer extends TitleModel {
    public PvpDestroyer() { super("pvp_destroyer", "Destructeur", "750 joueurs reduits en poussiere.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(7, 0, 0, 3, 7, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 750; }
}
