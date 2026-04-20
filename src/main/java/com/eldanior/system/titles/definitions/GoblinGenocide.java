package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;

public class GoblinGenocide extends TitleModel {
    public GoblinGenocide() {
        super("goblin_genocide", "Fleau des Gobelins", "Vous avez decime des nations entieres de gobelins.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(6, 0, 0, 0, 3, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "goblin", 0.30)));
    }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("goblin") >= 10000; }
}