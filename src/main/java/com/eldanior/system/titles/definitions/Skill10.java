package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Skill10 extends TitleModel {
    public Skill10() { super("skill_10", "Polyvalent", "Debloquer 10 competences.", Rarity.RARE, TitleCategory.COMPETENCES, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getUnlockedSkills().size() >= 10; }
}
