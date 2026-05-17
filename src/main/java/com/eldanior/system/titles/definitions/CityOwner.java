package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class CityOwner extends TitleModel {
    public CityOwner() { super("city_owner", "Gouverneur", "Posseder une ville.", Rarity.EPIC, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
}
