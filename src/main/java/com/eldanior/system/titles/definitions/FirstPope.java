package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FirstPope extends TitleModel {
    public FirstPope() { super("first_pope", "Premier Pape", "Le tout premier guide spirituel d'Eldanior.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(0, 0, 5, 0, 0, 5), List.of()); }
}