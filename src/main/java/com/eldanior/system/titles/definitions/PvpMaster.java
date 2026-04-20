package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpMaster extends TitleModel {
    public PvpMaster() { super("pvp_master", "Maitre du PvP", "Level 500+, 1000 kills PvP, KDR 3.0+.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(10, 5, 0, 5, 10, 5), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 500 && data.getPlayerKills() >= 1000 && data.getKDR() >= 3.0; }
}
