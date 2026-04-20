package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class MassDestroyer extends TitleModel {
    public MassDestroyer() { super("mass_destroyer", "Destructeur de Masse", "Des milliers ont peri par vos mains.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(5,0,0,3,0,0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.02))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 25000; }
}
