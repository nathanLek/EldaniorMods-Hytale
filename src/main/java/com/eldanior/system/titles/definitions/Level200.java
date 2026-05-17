package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Level200 extends TitleModel {
    public Level200() { super("level_200", "Legende Vivante", "Atteindre le niveau 200.", Rarity.UNIQUE, TitleCategory.PROGRESSION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 200; }
}
