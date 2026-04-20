package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Stubborn extends TitleModel {
    public Stubborn() { super("stubborn", "Obstine", "50 morts en PvP. Vous ne renoncez jamais.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(0, 2, 0, 2, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 50; }
}
