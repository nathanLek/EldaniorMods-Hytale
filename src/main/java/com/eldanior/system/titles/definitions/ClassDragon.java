package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ClassDragon extends TitleModel {
    public ClassDragon() { super("class_dragon", "Sang du Dragon", "Devenir un Dragon Ancestral.", Rarity.DIVINE, TitleCategory.CLASSE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return "dragon".equalsIgnoreCase(data.getPlayerClassId()); }
}
