package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LandBaron extends TitleModel {
    public LandBaron() { super("land_baron", "Baron Foncier", "Posseder 5 parcelles.", Rarity.RARE, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
}
