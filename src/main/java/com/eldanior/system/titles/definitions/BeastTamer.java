package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class BeastTamer extends TitleModel {
    public BeastTamer() { super("beast_tamer", "Dompteur de Betes", "Vous connaissez les creatures sauvages.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(0, 0, 0, 0, 2, 2), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("feran") >= 50 && data.getMobKillCountContaining("saurian") >= 50; }
}
