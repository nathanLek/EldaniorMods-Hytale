package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class KdrPerfect extends TitleModel {
    public KdrPerfect() { super("kdr_perfect", "KDR Parfait", "Ratio kill/death de 10.0+ avec 200+ kills.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 0, 5, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 200 && data.getKDR() >= 10.0; }
}
