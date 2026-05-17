package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DungeonWarrior extends TitleModel {
    public DungeonWarrior() { super("dungeon_warrior", "Guerrier des Profondeurs", "Decouvrir 10 donjons et tuer 10000 monstres.", Rarity.EPIC, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDungeonsDiscovered() >= 10 && data.getTotalMobKills() >= 10000; }
}
