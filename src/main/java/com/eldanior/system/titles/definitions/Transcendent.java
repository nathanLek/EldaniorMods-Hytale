package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Transcendent extends TitleModel {
    public Transcendent() { super("transcendent", "Transcendant", "Vous avez depasse les limites du mortel.", Rarity.DIVINE, TitleCategory.QUEST, new TitleBonus(12, 12, 12, 12, 12, 12), List.of(new TitleEffect(TitleEffect.TitleEffectType.XP_BONUS_PERCENT, "all", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 800; }
}