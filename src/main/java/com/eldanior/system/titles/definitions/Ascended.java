package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Ascended extends TitleModel {
    public Ascended() { super("ascended", "Ascensionne", "Vous avez transcende votre condition mortelle.", Rarity.LEGENDARY, TitleCategory.QUEST, new TitleBonus(10,10,10,10,10,10), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 700; }
}
