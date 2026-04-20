package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class SpiritSovereign extends TitleModel {
    public SpiritSovereign() { super("spirit_sovereign", "Souverain Elementaire", "Vous regnez sur les forces de la nature.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(0, 0, 8, 0, 0, 5), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "spirit", 0.20))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("spirit") >= 5000; }
}