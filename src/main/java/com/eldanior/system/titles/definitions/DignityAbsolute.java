package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DignityAbsolute extends TitleModel {
    public DignityAbsolute() { super("dignity_absolute", "Dignite Absolue", "Atteindre 100 de dignite.", Rarity.UNIQUE, TitleCategory.DIGNITE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getDignity() >= 100; }
}
