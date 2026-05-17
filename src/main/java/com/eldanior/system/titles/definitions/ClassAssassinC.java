package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class ClassAssassinC extends TitleModel {
    public ClassAssassinC() { super("class_assassin_c", "Voie de l'Assassin", "Choisir la classe Assassin.", Rarity.COMMON, TitleCategory.CLASSE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return "assassin".equalsIgnoreCase(data.getPlayerClassId()); }
}
