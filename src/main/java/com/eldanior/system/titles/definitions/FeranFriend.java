package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FeranFriend extends TitleModel {
    public FeranFriend() { super("feran_friend", "Ami des Ferans", "Vous n'avez jamais tue un Feran.", Rarity.RARE, TitleCategory.SOCIAL, new TitleBonus(0, 0, 0, 0, 0, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("feran") == 0 && data.getLevel() >= 50; }
}
