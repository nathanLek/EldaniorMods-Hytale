package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class AllRounder extends TitleModel {
    public AllRounder() { super("all_rounder", "Polyvalent PvE", "Tuer 100 de chaque type principal.", Rarity.UNIQUE, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") >= 100 && data.getMobKillCountContaining("zombie") >= 100 && data.getMobKillCountContaining("goblin") >= 100 && data.getMobKillCountContaining("trork") >= 100 && data.getMobKillCountContaining("spider") >= 100; }
}
