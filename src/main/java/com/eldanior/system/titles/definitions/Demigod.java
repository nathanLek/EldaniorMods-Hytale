package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Demigod extends TitleModel {
    public Demigod() { super("demigod", "Demi-Dieu", "Mi-mortel mi-divin.", Rarity.DIVINE, TitleCategory.QUEST, new TitleBonus(13,13,13,13,13,13), List.of(new TitleEffect(TitleEffect.TitleEffectType.XP_BONUS_PERCENT, "all", 0.05))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 900; }
}
