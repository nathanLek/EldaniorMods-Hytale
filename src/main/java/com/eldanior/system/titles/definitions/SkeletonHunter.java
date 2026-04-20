package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.TitleBonus;
import com.eldanior.system.titles.models.TitleModel;
import java.util.List;

public class SkeletonHunter extends TitleModel {
    public SkeletonHunter() {
        super("skeleton_hunter", "Chasseur d'Ossements", "Decerne a ceux qui ont purge les premiers squelettes.", Rarity.COMMON, TitleCategory.COMBAT, new TitleBonus(1, 0, 0, 0, 0, 0), List.of());
    }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") >= 100; }
}