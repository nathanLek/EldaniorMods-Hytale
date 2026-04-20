package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Adventurer extends TitleModel {
    public Adventurer() { super("adventurer", "Aventurier", "Vos premiers pas dans le monde d'Eldanior.", Rarity.COMMON, TitleCategory.QUEST, new TitleBonus(0, 0, 0, 0, 1, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 10; }
}