package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Polyvalent extends TitleModel {
    public Polyvalent() { super("polyvalent", "Polyvalent", "Vous avez tue au moins 100 creatures de 5 especes differentes.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(2, 2, 2, 2, 2, 2), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) {
        int speciesOver100 = 0;
        for (String key : new String[]{"skeleton", "goblin", "zombie", "trork", "outlander", "void", "spirit", "golem", "scarak", "dragon", "saurian"}) {
            if (data.getMobKillCountContaining(key) >= 100) speciesOver100++;
        }
        return speciesOver100 >= 5;
    }
}