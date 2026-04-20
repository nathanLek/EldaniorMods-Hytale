package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ScarakExterminator extends TitleModel {
    public ScarakExterminator() { super("scarak_exterminator", "Exterminateur d'Insectes", "Les Scaraks fuient votre presence.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 3, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("scarak") >= 50; }
}