package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Speedster extends TitleModel {
    public Speedster() { super("speedster", "Eclaireur Supreme", "Personne ne peut vous rattraper.", Rarity.LEGENDARY, TitleCategory.SPECIAL, new TitleBonus(0, 0, 0, 0, 10, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getAgility() >= 100; }
}
