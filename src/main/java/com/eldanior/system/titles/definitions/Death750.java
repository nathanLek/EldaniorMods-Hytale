package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Death750 extends TitleModel {
    public Death750() { super("death_750", "Immortel Maudit", "Mourir 750 fois.", Rarity.UNIQUE, TitleCategory.SURVIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 750; }
}
