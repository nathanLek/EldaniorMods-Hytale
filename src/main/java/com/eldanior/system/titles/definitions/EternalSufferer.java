package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class EternalSufferer extends TitleModel {
    public EternalSufferer() { super("eternal_sufferer", "Souffrance Eternelle", "1000 morts en PvP. La douleur est votre alliee.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(0, 10, 0, 10, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_REDUCTION_FROM_MOB, "all", 0.05))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerDeaths() >= 1000; }
}
