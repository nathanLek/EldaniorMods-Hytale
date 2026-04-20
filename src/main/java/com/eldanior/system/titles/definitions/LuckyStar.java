package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LuckyStar extends TitleModel {
    public LuckyStar() { super("lucky_star", "Etoile Chanceuse", "Votre chance depasse les 50 points.", Rarity.RARE, TitleCategory.QUEST, new TitleBonus(0, 0, 0, 0, 0, 3), List.of(new TitleEffect(TitleEffect.TitleEffectType.MONEY_BONUS_PERCENT, "all", 0.05))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLuck() >= 50; }
}