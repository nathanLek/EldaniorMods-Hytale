package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class DoubleTitle extends TitleModel {
    public DoubleTitle() { super("double_crown", "Double Couronne", "Noble et membre du clerge.", Rarity.LEGENDARY, TitleCategory.SOCIAL, new TitleBonus(5, 5, 5, 5, 5, 5), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { String n = data.getNobilityRank(); String c = data.getChurchRank(); return n != null && !n.equals("ROTURIER") && !n.equals("CHEVALIER") && c != null && !c.equals("LAIQUE") && !c.equals("RELIGIEUX"); }
}
