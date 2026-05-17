package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LegendGodSlayer extends TitleModel {
    public LegendGodSlayer() { super("legend_god_slayer", "Tueur de Dieux", "1000 dragons tues et niveau 300+.", Rarity.DIVINE, TitleCategory.LEGENDAIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("dragon") >= 1000 && data.getLevel() >= 300; }
}
