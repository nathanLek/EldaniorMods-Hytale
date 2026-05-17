package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Vit100 extends TitleModel {
    public Vit100() { super("vit_100", "Forteresse Vivante", "Atteindre 100 en Vitalite.", Rarity.UNIQUE, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getVitality() >= 100; }
}
