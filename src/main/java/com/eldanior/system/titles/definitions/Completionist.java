package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Completionist extends TitleModel {
    public Completionist() { super("completionist", "Completionniste", "Vous avez tue au moins 1000 creatures de chaque espece.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(5, 5, 5, 5, 5, 5), List.of(new TitleEffect(TitleEffect.TitleEffectType.XP_BONUS_PERCENT, "all", 0.15))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) {
        for (String key : new String[]{"skeleton", "goblin", "zombie", "trork", "outlander", "void", "spirit", "golem", "scarak", "dragon", "saurian"}) {
            if (data.getMobKillCountContaining(key) < 1000) return false;
        }
        return true;
    }
}