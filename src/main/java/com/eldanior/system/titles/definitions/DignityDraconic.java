package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DignityDraconic extends TitleModel {
    public DignityDraconic() { super("dignity_draconic", "Dignite Draconique", "Atteindre 1000 de dignite.", Rarity.DIVINE, TitleCategory.DIGNITE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDignity() >= 1000; }
}
