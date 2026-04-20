package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DarkPath extends TitleModel {
    public DarkPath() { super("dark_path", "Voie Sombre", "Vous avez massacre toutes les races pacifiques.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 0, 5, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "all", 0.03))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("feran") >= 100 && data.getMobKillCountContaining("slothian") >= 100 && data.getMobKillCountContaining("kweebec") >= 100; }
}
