package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Newcomer extends TitleModel {
    public Newcomer() { super("newcomer", "Nouveau Venu", "Vos tout premiers pas.", Rarity.COMMON, TitleCategory.QUEST, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 5; }
}
