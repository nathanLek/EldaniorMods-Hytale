package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ClassLevel100 extends TitleModel {
    public ClassLevel100() { super("class_level_100", "Maitre de Classe", "Atteindre le niveau 100 avec une classe.", Rarity.EPIC, TitleCategory.CLASSE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 100 && !"novice".equalsIgnoreCase(data.getPlayerClassId()); }
}
