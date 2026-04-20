package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class IronWall extends TitleModel {
    public IronWall() { super("iron_wall", "Mur de Fer", "Votre endurance depasse les 50 points.", Rarity.RARE, TitleCategory.QUEST, new TitleBonus(0, 0, 0, 3, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.HEALTH_BONUS_FLAT, "all", 50))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getEndurance() >= 50; }
}