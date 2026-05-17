package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Pvp5000 extends TitleModel {
    public Pvp5000() { super("pvp_5000", "Dieu du Carnage", "Tuer 5000 joueurs.", Rarity.DIVINE, TitleCategory.PVP, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 5000; }
}
