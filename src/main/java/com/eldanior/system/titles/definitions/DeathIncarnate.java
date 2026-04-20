package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DeathIncarnate extends TitleModel {
    public DeathIncarnate() { super("death_incarnate", "Incarnation de la Mort", "La mort elle-meme vous craint.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(8, 0, 0, 5, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.05))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 50000; }
}