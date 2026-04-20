package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class EliteTitle extends TitleModel {
    public EliteTitle() { super("elite", "Elite", "Vous faites partie des meilleurs.", Rarity.RARE, TitleCategory.QUEST, new TitleBonus(3, 3, 3, 3, 3, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 100; }
}