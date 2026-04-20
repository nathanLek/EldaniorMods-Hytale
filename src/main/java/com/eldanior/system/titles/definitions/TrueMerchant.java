package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TrueMerchant extends TitleModel {
    public TrueMerchant() { super("true_merchant", "Vrai Marchand", "L or est votre meilleure arme.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(0, 0, 0, 0, 0, 5), List.of(new TitleEffect(TitleEffect.TitleEffectType.MONEY_BONUS_PERCENT, "all", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerClassId().contains("merchant") && data.getLevel() >= 100; }
}
