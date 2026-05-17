package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Dungeon3 extends TitleModel {
    public Dungeon3() { super("dungeon_3", "Explorateur de Donjons", "Decouvrir 3 donjons.", Rarity.COMMON, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDungeonsDiscovered() >= 3; }
}
