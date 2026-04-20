package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Veteran extends TitleModel {
    public Veteran() { super("veteran", "Veteran", "L'experience vous a forge.", Rarity.COMMON, TitleCategory.QUEST, new TitleBonus(2, 2, 2, 2, 2, 2), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 50; }
}