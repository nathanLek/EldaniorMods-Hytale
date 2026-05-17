package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Level250 extends TitleModel {
    public Level250() { super("level_250", "Immortel", "Atteindre le niveau 250.", Rarity.UNIQUE, TitleCategory.PROGRESSION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 250; }
}
