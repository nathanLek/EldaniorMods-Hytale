package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;

public class ZombieEradicator extends TitleModel {
    public ZombieEradicator() {
        super("zombie_eradicator", "Eradicateur de Morts-Vivants", "Aucun mort-vivant ne se releve apres votre passage.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(0, 6, 0, 4, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_REDUCTION_FROM_MOB, "zombie", 0.10)));
    }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("zombie") >= 10000; }
}