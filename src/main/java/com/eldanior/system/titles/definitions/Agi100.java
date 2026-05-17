package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Agi100 extends TitleModel {
    public Agi100() { super("agi_100", "Vitesse Absolue", "Atteindre 100 en Agilite.", Rarity.UNIQUE, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getAgility() >= 100; }
}
