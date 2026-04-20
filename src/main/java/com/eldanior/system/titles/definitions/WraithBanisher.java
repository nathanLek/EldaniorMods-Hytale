package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class WraithBanisher extends TitleModel {
    public WraithBanisher() { super("wraith_banisher", "Banisseur de Spectre", "Vous avez renvoye le spectre dans l'au-dela.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 0, 3, 0, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "wraith", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("wraith") >= 10; }
}