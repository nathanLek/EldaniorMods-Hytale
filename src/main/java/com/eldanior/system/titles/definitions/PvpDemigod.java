package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpDemigod extends TitleModel {
    public PvpDemigod() { super("pvp_demigod", "Demi-Dieu du PvP", "5000 joueurs vaincus. Vous etes une legende.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(12, 0, 0, 5, 12, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 5000; }
}
