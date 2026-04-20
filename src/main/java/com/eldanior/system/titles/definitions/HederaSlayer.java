package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class HederaSlayer extends TitleModel {
    public HederaSlayer() { super("hedera_slayer", "Tueur de Hedera", "Vous avez terrasse la bete vegetale.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 3, 0, 0, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "hedera", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("hedera") >= 10; }
}
