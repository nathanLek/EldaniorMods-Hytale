package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpTyrant extends TitleModel {
    public PvpTyrant() { super("pvp_tyrant", "Tyran", "1000 joueurs ecrases.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(8, 0, 0, 3, 8, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 1000; }
}
