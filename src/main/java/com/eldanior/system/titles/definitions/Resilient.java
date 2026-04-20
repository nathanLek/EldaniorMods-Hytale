package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Resilient extends TitleModel {
    public Resilient() { super("resilient", "Resilient", "10 morts en PvP et toujours debout.", Rarity.COMMON, TitleCategory.COMBAT, new TitleBonus(0, 1, 0, 1, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 10; }
}
