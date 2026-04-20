package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class KdrDominator extends TitleModel {
    public KdrDominator() { super("kdr_dominator", "KDR Dominateur", "Ratio kill/death de 5.0+ avec 100+ kills.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 0, 3, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 100 && data.getKDR() >= 5.0; }
}
