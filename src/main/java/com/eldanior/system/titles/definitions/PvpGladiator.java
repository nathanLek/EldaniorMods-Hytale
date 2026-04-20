package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PvpGladiator extends TitleModel {
    public PvpGladiator() { super("pvp_gladiator", "Gladiateur", "250 joueurs tombes sous votre lame.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 0, 5, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 250; }
}
