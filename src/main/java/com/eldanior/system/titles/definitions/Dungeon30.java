package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Dungeon30 extends TitleModel {
    public Dungeon30() { super("dungeon_30", "Seigneur des Donjons", "Decouvrir 30 donjons.", Rarity.UNIQUE, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDungeonsDiscovered() >= 30; }
}
