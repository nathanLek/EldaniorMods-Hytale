package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpFirst extends TitleModel {
    public PvpFirst() { super("pvp_first", "Premier Sang PvP", "Tuer 1 joueur.", Rarity.COMMON, TitleCategory.PVP, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 1; }
}
