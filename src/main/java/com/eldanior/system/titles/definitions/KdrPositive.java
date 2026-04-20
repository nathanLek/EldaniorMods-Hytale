package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class KdrPositive extends TitleModel {
    public KdrPositive() { super("kdr_positive", "KDR Positif", "Votre ratio kill/death depasse 2.0 avec 50+ kills.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(1, 1, 0, 0, 1, 1), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 50 && data.getKDR() >= 2.0; }
}
