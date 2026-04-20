package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;

public class SkeletonCrusher extends TitleModel {
    public SkeletonCrusher() {
        super("skeleton_crusher", "Briseur de Squelettes", "Les os se brisent sous vos coups.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 0, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "skeleton", 0.05)));
    }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") >= 1000; }
}
