package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class RichTitle extends TitleModel {
    public RichTitle() { super("rich", "Riche", "L'or brille dans vos coffres.", Rarity.RARE, TitleCategory.SOCIAL, TitleBonus.NONE, List.of(new TitleEffect(TitleEffect.TitleEffectType.MONEY_BONUS_PERCENT, "all", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMoney() >= 1000000; }
}