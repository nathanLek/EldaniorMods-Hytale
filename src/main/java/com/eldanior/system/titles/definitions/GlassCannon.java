package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GlassCannon extends TitleModel {
    public GlassCannon() { super("glass_cannon", "Canon de Verre", "Force immense, defense fragile.", Rarity.EPIC, TitleCategory.SPECIAL, new TitleBonus(8, 0, 0, 0, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.05))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getStrength() >= 100 && data.getVitality() <= 20; }
}
