package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Undying extends TitleModel {
    public Undying() { super("undying_pvp", "Indestructible", "100 morts en PvP. Vous revenez toujours.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 4, 0, 4, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.HEALTH_BONUS_FLAT, "all", 30))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 100; }
}
