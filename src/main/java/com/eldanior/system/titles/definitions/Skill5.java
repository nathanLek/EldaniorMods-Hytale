package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Skill5 extends TitleModel {
    public Skill5() { super("skill_5", "Apprenti Competent", "Debloquer 5 competences.", Rarity.COMMON, TitleCategory.COMPETENCES, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedSkills().size() >= 5; }
}
