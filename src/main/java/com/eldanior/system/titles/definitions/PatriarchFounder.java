package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class PatriarchFounder extends TitleModel {
    public PatriarchFounder() { super("patriarch_founder", "Fondateur de Lignee", "Vous avez fonde votre propre lignee noble.", Rarity.EPIC, TitleCategory.SOCIAL, new TitleBonus(3, 3, 0, 0, 0, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.isPatriarch(); }
}