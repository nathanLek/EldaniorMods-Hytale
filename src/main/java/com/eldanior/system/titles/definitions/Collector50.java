package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Collector50 extends TitleModel {
    public Collector50() { super("collector_50", "Collectionneur", "Debloquer 50 titres.", Rarity.RARE, TitleCategory.COLLECTION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedTitles().size() >= 50; }
}
