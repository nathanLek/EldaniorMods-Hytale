package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PublicEnemy extends TitleModel {
    public PublicEnemy() { super("public_enemy", "Ennemi Public", "Avoir une prime de 10000+ Or.", Rarity.LEGENDARY, TitleCategory.PVP, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getBounty() >= 10000; }
}
