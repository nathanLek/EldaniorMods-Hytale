package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FirstMarquis extends TitleModel {
    public FirstMarquis() { super("first_marquis", "Premier Marquis", "Le tout premier Marquis d'Eldanior.", Rarity.LEGENDARY, TitleCategory.SPECIAL, new TitleBonus(3, 3, 3, 3, 3, 3), List.of()); }
}
