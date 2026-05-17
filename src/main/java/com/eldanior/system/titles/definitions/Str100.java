package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Str100 extends TitleModel {
    public Str100() { super("str_100", "Force Titanesque", "Atteindre 100 en Force.", Rarity.UNIQUE, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 100; }
}
