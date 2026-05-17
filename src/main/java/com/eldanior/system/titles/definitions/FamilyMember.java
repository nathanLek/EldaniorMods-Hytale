package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class FamilyMember extends TitleModel {
    public FamilyMember() { super("family_member", "Membre de Famille", "Appartenir a une famille noble.", Rarity.COMMON, TitleCategory.FAMILLE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty(); }
}
