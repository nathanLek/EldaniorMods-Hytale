package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TrorkWarbane extends TitleModel {
    public TrorkWarbane() { super("trork_warbane", "Fleau des Trorks", "Les clans Trorks murmurent votre nom avec terreur.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(4, 0, 0, 0, 3, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "trork", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("trork") >= 2000; }
}