package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class HolyWarrior extends TitleModel {
    public HolyWarrior() { super("holy_warrior", "Guerrier Sacre", "La foi guide votre lame.", Rarity.LEGENDARY, TitleCategory.SPECIAL, new TitleBonus(3, 0, 5, 0, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { String r = data.getChurchRank(); return data.getLevel() >= 100 && data.getTotalMobKills() >= 10000 && r != null && !r.equals("LAIQUE") && !r.equals("RELIGIEUX"); }
}
