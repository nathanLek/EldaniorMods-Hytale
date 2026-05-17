package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FaithSaint extends TitleModel {
    public FaithSaint() { super("faith_saint", "Saint", "Atteindre 250 de foi.", Rarity.EPIC, TitleCategory.FOI, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getFaith() >= 250; }
}
