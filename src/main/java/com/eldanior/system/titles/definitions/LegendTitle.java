package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendTitle extends TitleModel {
    public LegendTitle() { super("legend", "Legende", "Votre nom restera dans l'histoire.", Rarity.LEGENDARY, TitleCategory.QUEST, new TitleBonus(8, 8, 8, 8, 8, 8), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 500; }
}