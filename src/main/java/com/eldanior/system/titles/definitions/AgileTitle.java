package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class AgileTitle extends TitleModel {
    public AgileTitle() { super("agile", "Vif-Argent", "Votre agilite depasse les 50 points.", Rarity.RARE, TitleCategory.QUEST, new TitleBonus(0, 0, 0, 0, 3, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getAgility() >= 50; }
}
