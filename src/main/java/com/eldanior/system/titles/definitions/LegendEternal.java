package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendEternal extends TitleModel {
    public LegendEternal() { super("legend_eternal", "Eternel", "Niveau 999, 1000 dignite, 1000000 Or.", Rarity.DIVINE, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 999 && data.getDignity() >= 1000 && data.getMoney() >= 1000000; }
}
