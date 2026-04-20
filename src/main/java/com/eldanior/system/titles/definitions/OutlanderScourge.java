package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class OutlanderScourge extends TitleModel {
    public OutlanderScourge() { super("outlander_scourge", "Fleau des Outlanders", "Les cultistes tremblent a votre nom.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(4, 0, 0, 4, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "outlander", 0.15))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("outlander") >= 2000; }
}