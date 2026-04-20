package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GolemDestroyer extends TitleModel {
    public GolemDestroyer() { super("golem_destroyer", "Destructeur de Golems", "Les colosses de pierre ne vous resistent plus.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(6, 0, 0, 6, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "golem", 0.15))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("golem") >= 500; }
}