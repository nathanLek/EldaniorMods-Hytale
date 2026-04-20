package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class NightHunter extends TitleModel {
    public NightHunter() { super("night_hunter", "Chasseur Nocturne", "Vous avez purge les morts-vivants.", Rarity.RARE, TitleCategory.COMBAT, new TitleBonus(2, 0, 0, 2, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("skeleton") >= 100 && data.getMobKillCountContaining("zombie") >= 100; }
}
