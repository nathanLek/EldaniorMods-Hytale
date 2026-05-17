package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Collector150 extends TitleModel {
    public Collector150() { super("collector_150", "Obsede des Titres", "Debloquer 150 titres.", Rarity.UNIQUE, TitleCategory.COLLECTION, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedTitles().size() >= 150; }
}
