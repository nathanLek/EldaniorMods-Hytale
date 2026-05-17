package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Dino1000 extends TitleModel {
    public Dino1000() { super("dino_1000", "Extinction de Masse", "Tuer 1000 dinosaures.", Rarity.EPIC, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("raptor") + data.getMobKillCountContaining("trilobite") + data.getMobKillCountContaining("pterodactyl") >= 1000; }
}
