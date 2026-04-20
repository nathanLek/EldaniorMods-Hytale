package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FearInspiring extends TitleModel {
    public FearInspiring() { super("fear_inspiring", "Terreur Incarnee", "500 kills PvP et streak de 20+.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(8, 0, 0, 0, 8, 8), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 500 && data.getBestKillStreak() >= 20; }
}
