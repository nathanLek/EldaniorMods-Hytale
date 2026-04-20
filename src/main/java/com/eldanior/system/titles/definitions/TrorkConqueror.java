package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TrorkConqueror extends TitleModel {
    public TrorkConqueror() { super("trork_conqueror", "Conquerant des Trorks", "Vous avez soumis les hordes de Trorks.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(6, 0, 0, 0, 4, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "trork", 0.20))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("trork") >= 10000; }
}