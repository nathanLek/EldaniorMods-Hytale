package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Blooded extends TitleModel {
    public Blooded() { super("blooded", "Baptise du Sang", "Vos premieres victoires au combat.", Rarity.COMMON, TitleCategory.COMBAT, new TitleBonus(1, 0, 0, 0, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 500; }
}