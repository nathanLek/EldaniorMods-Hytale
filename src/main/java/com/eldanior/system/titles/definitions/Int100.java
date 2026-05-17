package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Int100 extends TitleModel {
    public Int100() { super("int_100", "Esprit Supreme", "Atteindre 100 en Intelligence.", Rarity.UNIQUE, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getIntelligence() >= 100; }
}
