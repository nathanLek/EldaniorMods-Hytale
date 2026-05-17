package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FaithDivine extends TitleModel {
    public FaithDivine() { super("faith_divine", "Elu Divin", "Atteindre 1000 de foi.", Rarity.LEGENDARY, TitleCategory.FOI, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getFaith() >= 1000; }
}
