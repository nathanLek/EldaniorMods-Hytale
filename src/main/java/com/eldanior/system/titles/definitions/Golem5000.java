package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Golem5000 extends TitleModel {
    public Golem5000() { super("golem_5000", "Ruine des Golems", "Tuer 5000 golems.", Rarity.LEGENDARY, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("golem") >= 5000; }
}
