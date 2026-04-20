package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Avenger extends TitleModel {
    public Avenger() { super("avenger", "Vengeur", "Plus de kills PvP que de morts, avec 100+ de chaque.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 2, 0, 2, 3, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getPlayerKills() >= 100 && data.getPlayerDeaths() >= 100 && data.getPlayerKills() > data.getPlayerDeaths(); }
}
