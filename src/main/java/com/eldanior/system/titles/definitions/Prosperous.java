package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Prosperous extends TitleModel {
    public Prosperous() { super("prosperous", "Prospere", "Posseder 2500 Or.", Rarity.RARE, TitleCategory.ECONOMIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMoney() >= 2500; }
}
