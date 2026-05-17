package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Int75 extends TitleModel {
    public Int75() { super("int_75", "Genie", "Atteindre 75 en Intelligence.", Rarity.EPIC, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getIntelligence() >= 75; }
}
