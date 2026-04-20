package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Warlord extends TitleModel {
    public Warlord() { super("warlord", "Seigneur de Guerre", "Votre presence commande le respect.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(6,6,6,6,6,6), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 400; }
}
