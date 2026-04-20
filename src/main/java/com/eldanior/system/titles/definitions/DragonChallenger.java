package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DragonChallenger extends TitleModel {
    public DragonChallenger() { super("dragon_challenger", "Defiant des Dragons", "Vous avez ose defier les dragons.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(5, 5, 0, 0, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("dragon") >= 10; }
}