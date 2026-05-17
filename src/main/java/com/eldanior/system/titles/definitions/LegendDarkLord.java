package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendDarkLord extends TitleModel {
    public LegendDarkLord() { super("legend_dark_lord", "Seigneur Noir", "1000 kills PvP et PK actif.", Rarity.LEGENDARY, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 1000 && data.isPK(); }
}
