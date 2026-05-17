package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class AnimalHunter1000 extends TitleModel {
    public AnimalHunter1000() { super("animal_hunter_1000", "Grand Chasseur", "Tuer 1000 animaux.", Rarity.EPIC, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("deer") + data.getMobKillCountContaining("boar") + data.getMobKillCountContaining("ram") + data.getMobKillCountContaining("chicken") + data.getMobKillCountContaining("cow") >= 1000; }
}
