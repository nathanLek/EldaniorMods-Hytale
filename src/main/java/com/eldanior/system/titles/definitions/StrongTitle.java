package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class StrongTitle extends TitleModel {
    public StrongTitle() { super("strong", "Colosse", "Votre force depasse les 50 points.", Rarity.RARE, TitleCategory.QUEST, new TitleBonus(3, 0, 0, 0, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 50; }
}
