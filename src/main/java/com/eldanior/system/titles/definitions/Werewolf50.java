package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Werewolf50 extends TitleModel {
    public Werewolf50() { super("werewolf_50", "Chasseur de Loup-Garous", "Tuer 50 loup-garous.", Rarity.RARE, TitleCategory.COMBAT, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("werewolf") >= 50; }
}
