package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DuelChampion extends TitleModel {
    public DuelChampion() { super("duel_champion", "Champion de Duel", "Gagner 100 duels.", Rarity.RARE, TitleCategory.DUEL, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDuelWins() >= 100; }
}
