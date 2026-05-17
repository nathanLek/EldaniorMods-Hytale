package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Undead10000 extends TitleModel {
    public Undead10000() { super("undead_10000", "Fléau des Morts", "Tuer 10000 morts-vivants.", Rarity.EPIC, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") + data.getMobKillCountContaining("zombie") >= 10000; }
}
