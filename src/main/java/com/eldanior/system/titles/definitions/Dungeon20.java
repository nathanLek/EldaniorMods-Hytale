package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Dungeon20 extends TitleModel {
    public Dungeon20() { super("dungeon_20", "Conquerant des Profondeurs", "Decouvrir 20 donjons.", Rarity.EPIC, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDungeonsDiscovered() >= 20; }
}
