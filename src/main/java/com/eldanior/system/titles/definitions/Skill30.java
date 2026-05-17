package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Skill30 extends TitleModel {
    public Skill30() { super("skill_30", "Maitre des Competences", "Debloquer 30 competences.", Rarity.UNIQUE, TitleCategory.COMPETENCES, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedSkills().size() >= 30; }
}
