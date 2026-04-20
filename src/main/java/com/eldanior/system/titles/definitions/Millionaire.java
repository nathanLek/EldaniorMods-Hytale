package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Millionaire extends TitleModel {
    public Millionaire() { super("millionaire", "Millionnaire", "Votre fortune est legendaire.", Rarity.LEGENDARY, TitleCategory.SOCIAL, TitleBonus.NONE, List.of(new TitleEffect(TitleEffect.TitleEffectType.MONEY_BONUS_PERCENT, "all", 0.20))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMoney() >= 10000000; }
}