package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DuelFighter extends TitleModel {
    public DuelFighter() { super("duel_fighter", "Combattant", "Gagner 10 duels.", Rarity.COMMON, TitleCategory.DUEL, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDuelWins() >= 10; }
}
