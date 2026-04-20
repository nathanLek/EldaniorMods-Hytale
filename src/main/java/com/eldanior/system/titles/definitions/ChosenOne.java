package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ChosenOne extends TitleModel {
    public ChosenOne() { super("chosen_one", "L'Elu", "Level max, Roi, un demi-million de kills. La legende absolue.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(20, 20, 20, 20, 20, 20), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.10), new TitleEffect(TitleEffect.TitleEffectType.XP_BONUS_PERCENT, "all", 0.15))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 999 && "ROI".equals(data.getNobilityRank()) && data.getTotalMobKills() >= 500000; }
}
