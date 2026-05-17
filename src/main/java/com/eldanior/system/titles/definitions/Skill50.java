package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Skill50 extends TitleModel {
    public Skill50() { super("skill_50", "Arsenal Vivant", "Debloquer 50 competences.", Rarity.LEGENDARY, TitleCategory.COMPETENCES, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedSkills().size() >= 50; }
}
