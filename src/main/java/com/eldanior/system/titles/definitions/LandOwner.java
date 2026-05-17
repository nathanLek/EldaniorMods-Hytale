package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LandOwner extends TitleModel {
    public LandOwner() { super("land_owner", "Proprietaire", "Acheter votre premiere parcelle.", Rarity.COMMON, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
}
