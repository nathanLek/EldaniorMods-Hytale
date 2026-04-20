package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Mythic extends TitleModel {
    public Mythic() { super("mythic", "Mythique", "Les bardes chantent vos exploits.", Rarity.LEGENDARY, TitleCategory.QUEST, new TitleBonus(9,9,9,9,9,9), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getLevel() >= 600; }
}
