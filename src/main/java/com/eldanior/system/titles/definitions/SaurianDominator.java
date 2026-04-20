package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class SaurianDominator extends TitleModel {
    public SaurianDominator() { super("saurian_dominator", "Dominateur des Sauriens", "Les Sauriens courbent l'echine devant vous.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 0, 4, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "saurian", 0.15))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("saurian") >= 2000; }
}