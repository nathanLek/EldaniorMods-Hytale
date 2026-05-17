package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Vit200 extends TitleModel {
    public Vit200() { super("vit_200", "Vitalite Eternelle", "Atteindre 200 en Vitalite.", Rarity.LEGENDARY, TitleCategory.STATS, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getVitality() >= 200; }
}
