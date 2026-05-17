package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class MostWanted extends TitleModel {
    public MostWanted() { super("most_wanted", "Le Plus Recherche", "Avoir une prime de 1000+ Or.", Rarity.EPIC, TitleCategory.PVP, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBounty() >= 1000; }
}
