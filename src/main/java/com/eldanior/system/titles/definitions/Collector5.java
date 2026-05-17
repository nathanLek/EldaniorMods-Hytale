package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Collector5 extends TitleModel {
    public Collector5() { super("collector_5", "Collecteur", "Debloquer 5 titres.", Rarity.COMMON, TitleCategory.COLLECTION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedTitles().size() >= 5; }
}
