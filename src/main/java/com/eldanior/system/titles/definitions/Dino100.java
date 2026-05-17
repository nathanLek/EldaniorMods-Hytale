package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Dino100 extends TitleModel {
    public Dino100() { super("dino_100", "Terreur du Jurassique", "Tuer 100 dinosaures.", Rarity.RARE, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("raptor") + data.getMobKillCountContaining("trilobite") + data.getMobKillCountContaining("pterodactyl") >= 100; }
}
