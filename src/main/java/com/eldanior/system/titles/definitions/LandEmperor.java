package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LandEmperor extends TitleModel {
    public LandEmperor() { super("land_emperor", "Empereur des Terres", "Posseder 25 parcelles.", Rarity.LEGENDARY, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
}
