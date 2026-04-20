package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class MaxPower extends TitleModel {
    public MaxPower() { super("max_power", "Puissance Absolue", "Le sommet ultime. Rien ne vous surpasse.", Rarity.DIVINE, TitleCategory.QUEST, new TitleBonus(15, 15, 15, 15, 15, 15), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 999; }
}