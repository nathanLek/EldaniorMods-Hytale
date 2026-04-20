package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;

public class GoblinNemesis extends TitleModel {
    public GoblinNemesis() {
        super("goblin_nemesis", "Nemesis des Gobelins", "Les gobelins fuient a votre approche.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(4, 0, 0, 2, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "goblin", 0.20)));
    }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("goblin") >= 2000; }
}