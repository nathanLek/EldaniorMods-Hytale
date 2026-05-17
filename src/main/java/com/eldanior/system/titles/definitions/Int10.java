package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Int10 extends TitleModel {
    public Int10() { super("int_10", "Eveille", "Atteindre 10 en Intelligence.", Rarity.COMMON, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getIntelligence() >= 10; }
}
