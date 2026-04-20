package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ChestMaster extends TitleModel {
    public ChestMaster() { super("chest_master", "Maitre des Coffres", "Vous avez decouvert un millier de coffres.", Rarity.DIVINE, TitleCategory.EXPLORATION, new TitleBonus(0,0,0,0,5,8), List.of(new TitleEffect(TitleEffect.TitleEffectType.MONEY_BONUS_PERCENT, "all", 0.20))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 1000; }
}
