package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class MasterHunterAll extends TitleModel {
    public MasterHunterAll() { super("master_hunter_all", "Maitre Chasseur Absolu", "Tuer 1000 de chaque type principal.", Rarity.LEGENDARY, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") >= 1000 && data.getMobKillCountContaining("zombie") >= 1000 && data.getMobKillCountContaining("goblin") >= 1000 && data.getMobKillCountContaining("trork") >= 1000 && data.getMobKillCountContaining("spider") >= 1000 && data.getMobKillCountContaining("void") >= 1000; }
}
