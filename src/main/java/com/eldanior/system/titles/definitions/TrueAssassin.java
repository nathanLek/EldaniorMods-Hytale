package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TrueAssassin extends TitleModel {
    public TrueAssassin() { super("true_assassin", "Vrai Assassin", "L'ombre est votre alliee.", Rarity.EPIC, TitleCategory.QUEST, new TitleBonus(3, 0, 0, 0, 5, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerClassId().contains("assassin") && data.getLevel() >= 100; }
}
