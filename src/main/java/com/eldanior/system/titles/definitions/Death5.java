package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Death5 extends TitleModel {
    public Death5() { super("death_5", "Maladroit", "Mourir 5 fois.", Rarity.COMMON, TitleCategory.SURVIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 5; }
}
