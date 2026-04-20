package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TrueWarrior extends TitleModel {
    public TrueWarrior() { super("true_warrior", "Vrai Guerrier", "Un guerrier accompli.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(5, 0, 0, 3, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerClassId().contains("warrior") && data.getLevel() >= 100; }
}
