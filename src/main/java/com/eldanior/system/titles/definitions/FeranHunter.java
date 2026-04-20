package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FeranHunter extends TitleModel {
    public FeranHunter() { super("feran_hunter", "Chasseur de Ferans", "Vous avez trahi la confiance des Ferans.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(2, 0, 0, 0, 2, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "feran", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("feran") >= 100; }
}
