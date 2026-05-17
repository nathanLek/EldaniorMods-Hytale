package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Undead100 extends TitleModel {
    public Undead100() { super("undead_100", "Purificateur", "Tuer 100 morts-vivants.", Rarity.COMMON, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") + data.getMobKillCountContaining("zombie") >= 100; }
}
