package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Death75 extends TitleModel {
    public Death75() { super("death_75", "Chat a 9 Vies", "Mourir 75 fois.", Rarity.RARE, TitleCategory.SURVIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 75; }
}
