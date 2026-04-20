package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class SaurianExtinction extends TitleModel {
    public SaurianExtinction() { super("saurian_extinction", "Extinction des Sauriens", "Vous avez presque eteint leur espece.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 0, 6, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "saurian", 0.25))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("saurian") >= 10000; }
}