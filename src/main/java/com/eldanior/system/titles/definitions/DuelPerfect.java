package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DuelPerfect extends TitleModel {
    public DuelPerfect() { super("duel_perfect", "Perfection", "Gagner 100 duels sans aucune defaite.", Rarity.DIVINE, TitleCategory.DUEL, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDuelWins() >= 100 && data.getDuelLosses() == 0; }
}
