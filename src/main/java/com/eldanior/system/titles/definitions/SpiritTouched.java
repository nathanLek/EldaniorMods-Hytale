package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class SpiritTouched extends TitleModel {
    public SpiritTouched() { super("spirit_touched", "Touche par les Esprits", "Les esprits vous ont marque de leur presence.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(0, 0, 2, 0, 0, 1), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("spirit") >= 50; }
}