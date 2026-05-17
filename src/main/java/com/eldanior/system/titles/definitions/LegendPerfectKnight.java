package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendPerfectKnight extends TitleModel {
    public LegendPerfectKnight() { super("legend_perfect_knight", "Chevalier Parfait", "Niveau 50+, 50+ dignite et rang noble.", Rarity.LEGENDARY, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 50 && data.getDignity() >= 50 && data.getNobilityRank() != null && !"ROTURIER".equals(data.getNobilityRank()); }
}
