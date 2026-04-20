package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpElite extends TitleModel {
    public PvpElite() { super("pvp_elite", "Elite du PvP", "100 joueurs vaincus.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 0, 3, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 100; }
}
