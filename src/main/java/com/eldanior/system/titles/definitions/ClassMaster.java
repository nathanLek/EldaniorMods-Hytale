package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ClassMaster extends TitleModel {
    public ClassMaster() { super("class_master", "Maitre de Classe", "Vous avez maitrise votre voie.", Rarity.LEGENDARY, TitleCategory.QUEST, new TitleBonus(5, 5, 5, 5, 5, 5), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return !data.getPlayerClassId().equals("novice") && data.getLevel() >= 500; }
}
