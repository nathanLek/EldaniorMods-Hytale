package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FaithDevout extends TitleModel {
    public FaithDevout() { super("faith_devout", "Devot", "Atteindre 20 de foi.", Rarity.COMMON, TitleCategory.FOI, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getFaith() >= 20; }
}
