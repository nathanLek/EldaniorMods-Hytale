package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class BossMaster extends TitleModel {
    public BossMaster() { super("boss_master", "Maitre des Boss", "Vous avez vaincu tous les boss.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(8, 8, 8, 8, 8, 8), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) {
        return data.getMobKillCountContaining("wraith") >= 10 && data.getMobKillCountContaining("werewolf") >= 10 && data.getMobKillCountContaining("yeti") >= 10 && data.getMobKillCountContaining("hedera") >= 10 && data.getMobKillCountContaining("shadow knight") >= 10 && data.getMobKillCountContaining("golem guardian void") >= 10;
    }
}
