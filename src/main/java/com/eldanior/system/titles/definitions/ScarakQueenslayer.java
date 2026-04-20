package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ScarakQueenslayer extends TitleModel {
    public ScarakQueenslayer() { super("scarak_queenslayer", "Tueur de Reine", "Vous avez detruit le coeur de la ruche.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(10, 0, 0, 8, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "scarak", 0.25))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("scarak") >= 5000; }
}