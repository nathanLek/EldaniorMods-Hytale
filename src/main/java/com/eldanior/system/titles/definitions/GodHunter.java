package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GodHunter extends TitleModel {
    public GodHunter() { super("god_hunter", "Dieu Chasseur", "Tuer 10000 de chaque type principal.", Rarity.DIVINE, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") >= 10000 && data.getMobKillCountContaining("zombie") >= 10000 && data.getMobKillCountContaining("goblin") >= 10000 && data.getMobKillCountContaining("trork") >= 10000; }
}
