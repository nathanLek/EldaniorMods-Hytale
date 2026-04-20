package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ChampionTitle extends TitleModel {
    public ChampionTitle() { super("champion", "Champion", "Votre renommee traverse les frontieres.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(4,4,4,4,4,4), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 200; }
}
