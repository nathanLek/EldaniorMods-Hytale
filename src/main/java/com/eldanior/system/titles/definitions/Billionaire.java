package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Billionaire extends TitleModel {
    public Billionaire() { super("billionaire", "Milliardaire", "Votre richesse depasse l entendement.", Rarity.DIVINE, TitleCategory.SOCIAL, TitleBonus.NONE, List.of(new TitleEffect(TitleEffect.TitleEffectType.MONEY_BONUS_PERCENT, "all", 0.30))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMoney() >= 100000000; }
}
