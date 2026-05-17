package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Saurian10000 extends TitleModel {
    public Saurian10000() { super("saurian_10000", "Extinction des Sauriens", "Tuer 10000 sauriens.", Rarity.LEGENDARY, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("saurian") >= 10000; }
}
