package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TrueArcher extends TitleModel {
    public TrueArcher() { super("true_archer", "Vrai Archer", "Votre precision est legendaire.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(0, 0, 0, 0, 5, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerClassId().contains("archer") && data.getLevel() >= 100; }
}
