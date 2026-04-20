package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpExterminator extends TitleModel {
    public PvpExterminator() { super("pvp_exterminator", "Exterminateur PvP", "10000 joueurs elimines. Vous etes un cauchemar.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(15, 0, 0, 8, 15, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 10000; }
}
