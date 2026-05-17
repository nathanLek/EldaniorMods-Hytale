package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Magnate extends TitleModel {
    public Magnate() { super("magnate", "Magnat", "Posseder 100000 Or.", Rarity.EPIC, TitleCategory.ECONOMIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMoney() >= 100000; }
}
