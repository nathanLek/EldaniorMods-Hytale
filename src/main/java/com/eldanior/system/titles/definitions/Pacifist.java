package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Pacifist extends TitleModel {
    public Pacifist() { super("pacifist", "Pacifiste", "Vous n'avez jamais tue une race pacifique.", Rarity.EPIC, TitleCategory.SOCIAL, new TitleBonus(0, 0, 3, 0, 0, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("feran") == 0 && data.getMobKillCountContaining("slothian") == 0 && data.getMobKillCountContaining("kweebec") == 0 && data.getLevel() >= 100; }
}
