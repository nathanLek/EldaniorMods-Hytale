package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class VoidWalker extends TitleModel {
    public VoidWalker() { super("void_walker", "Marcheur du Vide", "Vous avez arpente les terres du neant.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 0, 3, 0, 0, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("void") >= 50; }
}