package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ClassLevel50 extends TitleModel {
    public ClassLevel50() { super("class_level_50", "Specialiste", "Atteindre le niveau 50 avec une classe.", Rarity.RARE, TitleCategory.CLASSE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 50 && !"novice".equalsIgnoreCase(data.getPlayerClassId()); }
}
