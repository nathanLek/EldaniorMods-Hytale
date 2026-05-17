package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Dino10 extends TitleModel {
    public Dino10() { super("dino_10", "Chasseur Prehistorique", "Tuer 10 dinosaures.", Rarity.COMMON, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("raptor") + data.getMobKillCountContaining("trilobite") + data.getMobKillCountContaining("pterodactyl") >= 10; }
}
