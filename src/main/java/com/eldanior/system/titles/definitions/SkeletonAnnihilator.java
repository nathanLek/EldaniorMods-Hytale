package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;

public class SkeletonAnnihilator extends TitleModel {
    public SkeletonAnnihilator() {
        super("skeleton_annihilator", "Annihilateur des Morts", "Les armees de squelettes tremblent a votre nom.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 2, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "skeleton", 0.15)));
    }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") >= 10000; }
}
