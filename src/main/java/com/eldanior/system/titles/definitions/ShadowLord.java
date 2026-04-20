package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ShadowLord extends TitleModel {
    public ShadowLord() { super("shadow_lord", "Seigneur de l'Ombre", "Puissance, noblesse et destruction.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(8, 0, 0, 8, 8, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.05))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { String r = data.getNobilityRank(); return data.getLevel() >= 500 && data.getTotalMobKills() >= 50000 && r != null && !r.equals("ROTURIER"); }
}
