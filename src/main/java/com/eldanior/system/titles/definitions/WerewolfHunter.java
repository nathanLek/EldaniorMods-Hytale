package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class WerewolfHunter extends TitleModel {
    public WerewolfHunter() { super("werewolf_hunter", "Chasseur de Loup-Garou", "La pleine lune ne vous fait plus peur.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 0, 0, 0, 3, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "werewolf", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("werewolf") >= 10; }
}