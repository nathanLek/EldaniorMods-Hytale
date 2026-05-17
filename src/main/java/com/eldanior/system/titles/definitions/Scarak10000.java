package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Scarak10000 extends TitleModel {
    public Scarak10000() { super("scarak_10000", "Apocalypse des Scaraks", "Tuer 10000 scaraks.", Rarity.LEGENDARY, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("scarak") >= 10000; }
}
