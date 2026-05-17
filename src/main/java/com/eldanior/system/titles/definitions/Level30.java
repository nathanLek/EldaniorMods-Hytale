package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Level30 extends TitleModel {
    public Level30() { super("level_30", "Confirme", "Atteindre le niveau 30.", Rarity.RARE, TitleCategory.PROGRESSION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 30; }
}
