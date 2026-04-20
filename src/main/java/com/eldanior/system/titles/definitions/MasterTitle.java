package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class MasterTitle extends TitleModel {
    public MasterTitle() { super("master", "Maitre", "Votre maitrise est reconnue par tous.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(5, 5, 5, 5, 5, 5), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 300; }
}