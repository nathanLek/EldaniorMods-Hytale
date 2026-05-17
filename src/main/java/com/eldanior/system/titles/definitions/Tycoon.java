package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Tycoon extends TitleModel {
    public Tycoon() { super("tycoon", "Tycoon", "Posseder 500000 Or.", Rarity.UNIQUE, TitleCategory.ECONOMIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMoney() >= 500000; }
}
