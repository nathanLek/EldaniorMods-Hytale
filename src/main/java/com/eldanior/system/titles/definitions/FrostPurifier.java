package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FrostPurifier extends TitleModel {
    public FrostPurifier() { super("frost_purifier", "Purificateur du Givre", "Vous avez purge les squelettes de glace.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 0, 3, 3, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "frost", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton frost") >= 100; }
}
