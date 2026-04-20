package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TankTitle extends TitleModel {
    public TankTitle() { super("tank", "Forteresse Vivante", "Rien ne peut vous abattre.", Rarity.LEGENDARY, TitleCategory.SPECIAL, new TitleBonus(0, 10, 0, 10, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_REDUCTION_FROM_MOB, "all", 0.05))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getVitality() >= 100 && data.getEndurance() >= 100; }
}
