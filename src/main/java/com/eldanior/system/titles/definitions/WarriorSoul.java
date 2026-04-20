package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class WarriorSoul extends TitleModel {
    public WarriorSoul() { super("warrior_soul", "Ame de Guerrier", "Le combat coule dans vos veines.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 2, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 5000; }
}