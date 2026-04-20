package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FirstKing extends TitleModel {
    public FirstKing() { super("first_king", "Premier Roi", "Le tout premier souverain d'Eldanior.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(5, 5, 5, 5, 5, 5), List.of()); }
}