package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DignityAwakening extends TitleModel {
    public DignityAwakening() { super("dignity_awakening", "Eveil de Dignite", "Atteindre 5 de dignite.", Rarity.COMMON, TitleCategory.DIGNITE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDignity() >= 5; }
}
