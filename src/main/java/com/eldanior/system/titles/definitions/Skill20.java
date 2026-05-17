package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Skill20 extends TitleModel {
    public Skill20() { super("skill_20", "Expert en Competences", "Debloquer 20 competences.", Rarity.EPIC, TitleCategory.COMPETENCES, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedSkills().size() >= 20; }
}
