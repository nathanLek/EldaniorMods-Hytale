package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ShadowVanquisher extends TitleModel {
    public ShadowVanquisher() { super("shadow_vanquisher", "Vainqueur de l'Ombre", "Vous avez triomphe du chevalier de l'ombre.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(5, 0, 0, 0, 5, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("shadow knight") >= 10; }
}