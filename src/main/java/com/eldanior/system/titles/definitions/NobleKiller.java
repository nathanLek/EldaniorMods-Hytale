package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class NobleKiller extends TitleModel {
    public NobleKiller() { super("noble_killer", "Tueur Noble", "Noble avec 100 kills PvP.", Rarity.EPIC, TitleCategory.SPECIAL, new TitleBonus(3, 0, 0, 0, 3, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { String r = data.getNobilityRank(); return data.getPlayerKills() >= 100 && r != null && !r.equals("ROTURIER") && !r.equals("CHEVALIER"); }
}
