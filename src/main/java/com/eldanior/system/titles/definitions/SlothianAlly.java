package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class SlothianAlly extends TitleModel {
    public SlothianAlly() { super("slothian_ally", "Allie des Slothians", "Vous n'avez jamais tue un Slothian.", Rarity.RARE, TitleCategory.SOCIAL, new TitleBonus(0, 0, 2, 0, 0, 1), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("slothian") == 0 && data.getLevel() >= 50; }
}
