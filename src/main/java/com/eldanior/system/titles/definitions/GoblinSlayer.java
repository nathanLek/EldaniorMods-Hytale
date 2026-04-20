package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.TitleBonus;
import com.eldanior.system.titles.models.TitleEffect;
import com.eldanior.system.titles.models.TitleModel;

import java.util.List;

public class GoblinSlayer extends TitleModel {

    public GoblinSlayer() {
        super(
                "goblin_slayer",
                "Tueur de Gobelins",
                "Decerne aux guerriers ayant elimine des hordes de gobelins.",
                Rarity.RARE,
                TitleCategory.COMBAT,
                new TitleBonus(2, 0, 0, 1, 0, 0),
                List.of(
                        new TitleEffect(
                                TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB,
                                "goblin_scrapper",
                                0.10
                        )
                )
        );
    }

    @Override
    public boolean checkUnlockCondition(PlayerLevelData data) {
        return data.getMobKillCount("goblin_scrapper") >= 200;
    }
}