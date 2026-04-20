package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ArenaChampion extends TitleModel {
    public ArenaChampion() { super("arena_champion", "Champion d'Arene", "Level 200+ et 500 kills PvP.", Rarity.LEGENDARY, TitleCategory.SPECIAL, new TitleBonus(5, 5, 0, 5, 5, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 200 && data.getPlayerKills() >= 500; }
}
