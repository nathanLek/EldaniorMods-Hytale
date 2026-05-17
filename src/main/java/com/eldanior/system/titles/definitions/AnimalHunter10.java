package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class AnimalHunter10 extends TitleModel {
    public AnimalHunter10() { super("animal_hunter_10", "Chasseur", "Tuer 10 animaux.", Rarity.COMMON, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("deer") + data.getMobKillCountContaining("boar") + data.getMobKillCountContaining("ram") + data.getMobKillCountContaining("chicken") + data.getMobKillCountContaining("cow") >= 10; }
}
