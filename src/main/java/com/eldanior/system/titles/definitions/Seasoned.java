package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Seasoned extends TitleModel {
    public Seasoned() { super("seasoned", "Aguerri", "Les batailles vous ont endurci.", Rarity.RARE, TitleCategory.QUEST, new TitleBonus(3,3,3,3,3,3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 150; }
}
