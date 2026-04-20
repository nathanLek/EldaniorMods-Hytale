package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class UndeadPurger extends TitleModel {
    public UndeadPurger() { super("undead_purger", "Purgeur de Morts-Vivants", "Les morts-vivants sont votre specialite.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(4, 0, 0, 4, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "skeleton", 0.05), new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "zombie", 0.05))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") >= 1000 && data.getMobKillCountContaining("zombie") >= 1000; }
}
