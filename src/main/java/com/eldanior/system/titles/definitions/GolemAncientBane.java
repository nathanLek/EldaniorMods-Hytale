package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GolemAncientBane extends TitleModel {
    public GolemAncientBane() { super("golem_ancient_bane", "Fleau des Anciens", "Vous avez detruit les gardiens millnaires.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(10, 0, 0, 10, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "golem", 0.25))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("golem") >= 5000; }
}