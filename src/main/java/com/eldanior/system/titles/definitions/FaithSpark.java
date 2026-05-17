package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FaithSpark extends TitleModel {
    public FaithSpark() { super("faith_spark", "Etincelle de Foi", "Atteindre 5 de foi.", Rarity.COMMON, TitleCategory.FOI, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getFaith() >= 5; }
}
