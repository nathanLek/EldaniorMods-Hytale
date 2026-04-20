package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class SlothianBetrayer extends TitleModel {
    public SlothianBetrayer() { super("slothian_betrayer", "Traitre des Slothians", "Les Slothians ne vous font plus confiance.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(0, 2, 2, 0, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "slothian", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("slothian") >= 100; }
}
