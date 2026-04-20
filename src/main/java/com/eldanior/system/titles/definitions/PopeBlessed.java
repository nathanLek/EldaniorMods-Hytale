package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PopeBlessed extends TitleModel {
    public PopeBlessed() { super("pope_blessed", "Beni du Pape", "La benediction papale vous protege.", Rarity.EPIC, TitleCategory.SOCIAL, new TitleBonus(0, 0, 3, 0, 0, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) {
        String rank = data.getChurchRank();
        return rank != null && (rank.equals("CARDINAL") || rank.equals("PAPE"));
    }
}