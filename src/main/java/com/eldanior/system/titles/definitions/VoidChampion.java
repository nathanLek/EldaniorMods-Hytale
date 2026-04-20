package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class VoidChampion extends TitleModel {
    public VoidChampion() { super("void_champion", "Champion du Neant", "Vous avez vaincu le gardien du vide.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(10, 10, 10, 10, 10, 10), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("golem guardian void") >= 10; }
}