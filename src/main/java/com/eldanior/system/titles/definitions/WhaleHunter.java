package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class WhaleHunter extends TitleModel {
    public WhaleHunter() { super("whale_hunter", "Chasseur de Baleine", "Vous avez vaincu le leviathan.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 3, 0, 3, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "whale", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("whale") >= 10; }
}
