package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ClassArcherC extends TitleModel {
    public ClassArcherC() { super("class_archer_c", "Voie de l'Archer", "Choisir la classe Archer.", Rarity.COMMON, TitleCategory.CLASSE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return "archer".equalsIgnoreCase(data.getPlayerClassId()); }
}
