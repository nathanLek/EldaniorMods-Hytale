package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DeathLord extends TitleModel {
    public DeathLord() { super("death_lord", "Seigneur de la Mort", "La faucheuse est votre alliee.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(10,0,0,7,0,0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.07))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 100000; }
}
