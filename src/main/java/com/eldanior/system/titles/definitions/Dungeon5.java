package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Dungeon5 extends TitleModel {
    public Dungeon5() { super("dungeon_5", "Chasseur de Donjons", "Decouvrir 5 donjons.", Rarity.RARE, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDungeonsDiscovered() >= 5; }
}
