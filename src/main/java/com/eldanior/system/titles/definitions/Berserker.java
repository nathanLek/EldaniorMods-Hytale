package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Berserker extends TitleModel {
    public Berserker() { super("berserker", "Berserker", "Votre rage au combat est sans limite.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 0, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.03))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 10000 && data.getLevel() >= 100; }
}