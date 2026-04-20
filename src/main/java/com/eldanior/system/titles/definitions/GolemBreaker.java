package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GolemBreaker extends TitleModel {
    public GolemBreaker() { super("golem_breaker", "Briseur de Golems", "Vous avez fissure la pierre vivante.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 0, 0, 3, 0, 0), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("golem") >= 50; }
}