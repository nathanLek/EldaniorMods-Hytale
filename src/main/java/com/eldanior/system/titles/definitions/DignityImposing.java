package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DignityImposing extends TitleModel {
    public DignityImposing() { super("dignity_imposing", "Presence Imposante", "Atteindre 50 de dignite.", Rarity.EPIC, TitleCategory.DIGNITE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDignity() >= 50; }
}
