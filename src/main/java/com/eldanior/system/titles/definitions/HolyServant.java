package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class HolyServant extends TitleModel {
    public HolyServant() { super("holy_servant", "Serviteur Sacre", "Vous servez la lumiere divine.", Rarity.RARE, TitleCategory.SOCIAL, new TitleBonus(0, 0, 2, 0, 0, 2), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) {
        String rank = data.getChurchRank();
        return rank != null && !rank.equals("LAIQUE") && !rank.equals("RELIGIEUX");
    }
}