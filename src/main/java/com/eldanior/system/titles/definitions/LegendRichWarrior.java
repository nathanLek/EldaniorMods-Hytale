package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendRichWarrior extends TitleModel {
    public LegendRichWarrior() { super("legend_rich_warrior", "Guerrier Fortune", "Niveau 100+ et 50000+ Or.", Rarity.LEGENDARY, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 100 && data.getMoney() >= 50000; }
}
