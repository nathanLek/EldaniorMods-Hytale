package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DignityRising extends TitleModel {
    public DignityRising() { super("dignity_rising", "Dignite Montante", "Atteindre 20 de dignite.", Rarity.RARE, TitleCategory.DIGNITE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDignity() >= 20; }
}
