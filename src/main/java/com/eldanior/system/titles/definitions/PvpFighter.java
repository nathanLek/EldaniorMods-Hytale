package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpFighter extends TitleModel {
    public PvpFighter() { super("pvp_fighter", "Combattant PvP", "10 joueurs vaincus.", Rarity.COMMON, TitleCategory.COMBAT, new TitleBonus(1, 0, 0, 0, 1, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 10; }
}
