package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Pvp75 extends TitleModel {
    public Pvp75() { super("pvp_75", "Predateur", "Tuer 75 joueurs.", Rarity.RARE, TitleCategory.PVP, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 75; }
}
