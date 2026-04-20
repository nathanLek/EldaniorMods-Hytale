package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ScarakHivebane extends TitleModel {
    public ScarakHivebane() { super("scarak_hivebane", "Destructeur de Ruche", "Vous avez aneanti des colonies entieres.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(6, 0, 0, 5, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "scarak", 0.15))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("scarak") >= 500; }
}