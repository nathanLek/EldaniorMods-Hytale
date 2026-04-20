package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class NobleWarrior extends TitleModel {
    public NobleWarrior() { super("noble_warrior", "Guerrier Noble", "Noble et mortel.", Rarity.LEGENDARY, TitleCategory.SPECIAL, new TitleBonus(5, 0, 0, 5, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { String r = data.getNobilityRank(); return data.getLevel() >= 100 && data.getTotalMobKills() >= 10000 && r != null && !r.equals("ROTURIER") && !r.equals("CHEVALIER"); }
}
