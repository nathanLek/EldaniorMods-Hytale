package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FirstDuc extends TitleModel {
    public FirstDuc() { super("first_duc", "Premier Duc", "Le tout premier Duc d'Eldanior.", Rarity.EPIC, TitleCategory.SPECIAL, new TitleBonus(2, 2, 2, 2, 2, 2), List.of()); }
}
