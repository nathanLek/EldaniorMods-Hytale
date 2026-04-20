package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class OutlanderAnnihilator extends TitleModel {
    public OutlanderAnnihilator() { super("outlander_annihilator", "Annihilateur des Outlanders", "Vous avez eradique la menace Outlander.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(6, 0, 0, 6, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "outlander", 0.25))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("outlander") >= 10000; }
}