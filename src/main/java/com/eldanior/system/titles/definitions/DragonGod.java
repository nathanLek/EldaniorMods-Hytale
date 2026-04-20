package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DragonGod extends TitleModel {
    public DragonGod() { super("dragon_god", "Dieu des Dragons", "Meme les anciens dragons s'inclinent devant vous.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(15, 10, 5, 5, 5, 5), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "dragon", 0.30))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("dragon") >= 1000; }
}