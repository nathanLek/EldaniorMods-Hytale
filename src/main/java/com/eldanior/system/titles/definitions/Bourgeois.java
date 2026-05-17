package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Bourgeois extends TitleModel {
    public Bourgeois() { super("bourgeois", "Bourgeois", "Posseder 5000 Or.", Rarity.RARE, TitleCategory.ECONOMIE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMoney() >= 5000; }
}
