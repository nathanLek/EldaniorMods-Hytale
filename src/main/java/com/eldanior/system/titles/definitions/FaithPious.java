package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FaithPious extends TitleModel {
    public FaithPious() { super("faith_pious", "Pieux", "Atteindre 50 de foi.", Rarity.RARE, TitleCategory.FOI, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getFaith() >= 50; }
}
