package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Slayer extends TitleModel {
    public Slayer() { super("slayer", "Trancheur", "Votre lame ne connait pas le repos.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(2,0,0,1,1,0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 2500; }
}
