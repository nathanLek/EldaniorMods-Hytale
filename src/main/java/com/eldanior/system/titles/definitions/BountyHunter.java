package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class BountyHunter extends TitleModel {
    public BountyHunter() { super("bounty_hunter", "Chasseur de Primes", "Avoir une prime de 100+ Or.", Rarity.RARE, TitleCategory.PVP, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBounty() >= 100; }
}
