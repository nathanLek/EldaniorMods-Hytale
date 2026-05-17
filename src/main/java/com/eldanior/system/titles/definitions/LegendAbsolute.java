package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendAbsolute extends TitleModel {
    public LegendAbsolute() { super("legend_absolute", "L'Absolu", "Niveau 500+, toutes stats 100+, 100+ dignite.", Rarity.DIVINE, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 500 && data.getStrength() >= 100 && data.getVitality() >= 100 && data.getIntelligence() >= 100 && data.getEndurance() >= 100 && data.getAgility() >= 100 && data.getLuck() >= 100 && data.getDignity() >= 100; }
}
