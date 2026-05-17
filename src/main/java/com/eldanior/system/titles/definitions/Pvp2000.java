package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Pvp2000 extends TitleModel {
    public Pvp2000() { super("pvp_2000", "Genocide", "Tuer 2000 joueurs.", Rarity.LEGENDARY, TitleCategory.PVP, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 2000; }
}
