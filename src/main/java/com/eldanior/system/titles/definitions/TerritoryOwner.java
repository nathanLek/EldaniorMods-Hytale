package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class TerritoryOwner extends TitleModel {
    public TerritoryOwner() { super("territory_owner", "Seigneur des Terres", "Posseder un territoire.", Rarity.LEGENDARY, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
}
