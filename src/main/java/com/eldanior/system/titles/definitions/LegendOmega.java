package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendOmega extends TitleModel {
    public LegendOmega() { super("legend_omega", "Omega", "200 titres debloques et niveau 500+.", Rarity.DIVINE, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedTitles().size() >= 200 && data.getLevel() >= 500; }
}
