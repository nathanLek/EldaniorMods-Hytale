package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class KweebecFriend extends TitleModel {
    public KweebecFriend() { super("kweebec_friend", "Ami des Kweebecs", "Atteindre le niveau 50 sans tuer de kweebec.", Rarity.EPIC, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("kweebec") == 0 && data.getLevel() >= 50; }
}
