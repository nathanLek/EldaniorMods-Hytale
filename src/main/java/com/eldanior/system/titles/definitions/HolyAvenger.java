package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class HolyAvenger extends TitleModel {
    public HolyAvenger() { super("holy_avenger", "Vengeur Sacre", "Pretre+ et 200 kills PvP. La justice divine.", Rarity.LEGENDARY, TitleCategory.SPECIAL, new TitleBonus(3, 0, 5, 0, 3, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { String r = data.getChurchRank(); return data.getPlayerKills() >= 200 && r != null && !r.equals("LAIQUE") && !r.equals("RELIGIEUX"); }
}
