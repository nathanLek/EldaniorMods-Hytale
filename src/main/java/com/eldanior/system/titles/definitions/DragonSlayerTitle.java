package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DragonSlayerTitle extends TitleModel {
    public DragonSlayerTitle() { super("dragon_slayer", "Tueur de Dragons", "Les dragons les plus feroces tombent sous votre lame.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(10, 10, 0, 0, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "dragon", 0.20))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("dragon") >= 100; }
}