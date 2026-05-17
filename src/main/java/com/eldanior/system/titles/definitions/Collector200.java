package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Collector200 extends TitleModel {
    public Collector200() { super("collector_200", "Encyclopedie Vivante", "Debloquer 200 titres.", Rarity.UNIQUE, TitleCategory.COLLECTION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedTitles().size() >= 200; }
}
