package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ChestSeeker extends TitleModel {
    public ChestSeeker() { super("chest_seeker", "Chercheur de Tresors", "Aucun coffre ne vous echappe.", Rarity.EPIC, TitleCategory.EXPLORATION, new TitleBonus(0,0,0,0,2,3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 100; }
}
