package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Death250 extends TitleModel {
    public Death250() { super("death_250", "Phenix Eternel", "Mourir 250 fois.", Rarity.EPIC, TitleCategory.SURVIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 250; }
}
