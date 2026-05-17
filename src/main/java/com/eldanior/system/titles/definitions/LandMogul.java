package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class LandMogul extends TitleModel {
    public LandMogul() { super("land_mogul", "Magnat Foncier", "Posseder 10 parcelles.", Rarity.EPIC, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
}
