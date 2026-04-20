package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class EternalChampion extends TitleModel {
    public EternalChampion() { super("eternal_champion", "Champion Eternel", "Le temps n a aucune prise sur votre force.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(10, 10, 10, 10, 10, 10), List.of(new TitleEffect(TitleEffect.TitleEffectType.XP_BONUS_PERCENT, "all", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 800 && data.getTotalMobKills() >= 100000; }
}
