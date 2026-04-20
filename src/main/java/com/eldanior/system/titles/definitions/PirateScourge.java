package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PirateScourge extends TitleModel {
    public PirateScourge() { super("pirate_scourge", "Fleau des Pirates", "Les pirates squelettes craignent votre nom.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 0, 3, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "pirate", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton pirate") >= 100; }
}
