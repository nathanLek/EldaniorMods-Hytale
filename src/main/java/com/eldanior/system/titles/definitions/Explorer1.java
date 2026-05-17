package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Explorer1 extends TitleModel {
    public Explorer1() { super("explorer_1", "Premier Pas d'Explorateur", "Decouvrir votre premiere zone.", Rarity.COMMON, TitleCategory.TERRITOIRE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getTerritoriesDiscovered() >= 1; }
}
