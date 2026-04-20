package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;

public class ZombiePurifier extends TitleModel {
    public ZombiePurifier() {
        super("zombie_purifier", "Purificateur", "Les morts-vivants retournent a la terre sous vos coups.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 4, 0, 3, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "zombie", 0.10)));
    }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("zombie") >= 2000; }
}