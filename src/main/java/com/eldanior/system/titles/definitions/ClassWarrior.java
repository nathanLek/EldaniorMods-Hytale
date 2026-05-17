package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ClassWarrior extends TitleModel {
    public ClassWarrior() { super("class_warrior", "Voie du Guerrier", "Choisir la classe Guerrier.", Rarity.COMMON, TitleCategory.CLASSE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return "guerrier".equalsIgnoreCase(data.getPlayerClassId()) || "warrior".equalsIgnoreCase(data.getPlayerClassId()); }
}
