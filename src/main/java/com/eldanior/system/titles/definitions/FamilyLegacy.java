package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FamilyLegacy extends TitleModel {
    public FamilyLegacy() { super("family_legacy", "Heritage Familial", "Membre d'une famille noble de haut rang.", Rarity.EPIC, TitleCategory.SOCIAL, new TitleBonus(3, 3, 0, 0, 0, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { String f = data.getNobleFamilyId(); return f != null && !f.isEmpty() && data.getLevel() >= 100; }
}
