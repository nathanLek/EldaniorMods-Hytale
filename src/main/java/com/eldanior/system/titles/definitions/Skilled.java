package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Skilled extends TitleModel {
    public Skilled() { super("skilled", "Competent", "Vos competences se developpent.", Rarity.COMMON, TitleCategory.QUEST, new TitleBonus(1,1,1,1,1,1), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 30; }
}
