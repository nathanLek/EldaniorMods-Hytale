package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Death150 extends TitleModel {
    public Death150() { super("death_150", "Revenant", "Mourir 150 fois.", Rarity.RARE, TitleCategory.SURVIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 150; }
}
