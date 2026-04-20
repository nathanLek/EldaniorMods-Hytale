package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ChestHoarder extends TitleModel {
    public ChestHoarder() { super("chest_hoarder", "Accumulateur de Tresors", "Votre collection de tresors est legendaire.", Rarity.LEGENDARY, TitleCategory.EXPLORATION, new TitleBonus(0,0,0,0,3,5), List.of(new TitleEffect(TitleEffect.TitleEffectType.MONEY_BONUS_PERCENT, "all", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getChestsDiscovered() >= 500; }
}
