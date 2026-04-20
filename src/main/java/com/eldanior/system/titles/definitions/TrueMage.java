package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TrueMage extends TitleModel {
    public TrueMage() { super("true_mage", "Vrai Mage", "Un mage accompli.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(0, 0, 5, 0, 0, 3), List.of(new TitleEffect(TitleEffect.TitleEffectType.MANA_BONUS_FLAT, "all", 50))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerClassId().contains("mage") && data.getLevel() >= 100; }
}
