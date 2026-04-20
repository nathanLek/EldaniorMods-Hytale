package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpLegend extends TitleModel {
    public PvpLegend() { super("pvp_legend", "Legende du PvP", "3000 joueurs vaincus. Les histoires parlent de vous.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(10, 0, 0, 5, 10, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 3000; }
}
