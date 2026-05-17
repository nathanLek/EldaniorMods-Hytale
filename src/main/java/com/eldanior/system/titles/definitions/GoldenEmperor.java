package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GoldenEmperor extends TitleModel {
    public GoldenEmperor() { super("golden_emperor", "Empereur Dore", "Posseder 10000000 Or.", Rarity.DIVINE, TitleCategory.ECONOMIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMoney() >= 10000000; }
}
