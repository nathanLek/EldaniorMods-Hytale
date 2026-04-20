package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class NobleBlood extends TitleModel {
    public NobleBlood() { super("noble_blood", "Sang Noble", "Le sang de la noblesse coule dans vos veines.", Rarity.RARE, TitleCategory.SOCIAL, new TitleBonus(2, 2, 0, 0, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) {
        String rank = data.getNobilityRank();
        return rank != null && !rank.equals("ROTURIER") && !rank.equals("CHEVALIER");
    }
}