package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PhoenixRising extends TitleModel {
    public PhoenixRising() { super("phoenix_rising", "Phenix Renaissant", "500 morts en PvP. Vous etes le phenix.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(0, 6, 0, 6, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.HEALTH_BONUS_FLAT, "all", 50))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 500; }
}
