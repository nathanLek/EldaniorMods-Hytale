package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GodOfWar extends TitleModel {
    public GodOfWar() { super("god_of_war", "Dieu de la Guerre", "Un demi-million d'ames fauchees par votre lame.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(15, 0, 0, 10, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTotalMobKills() >= 500000; }
}