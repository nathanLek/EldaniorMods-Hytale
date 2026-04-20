package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class BossHunter extends TitleModel {
    public BossHunter() { super("boss_hunter", "Chasseur de Boss", "Vous avez vaincu au moins 3 types de boss.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(3, 3, 3, 3, 3, 3), List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) {
        int count = 0;
        if (data.getMobKillCountContaining("wraith") >= 10) count++;
        if (data.getMobKillCountContaining("werewolf") >= 10) count++;
        if (data.getMobKillCountContaining("yeti") >= 10) count++;
        if (data.getMobKillCountContaining("hedera") >= 10) count++;
        if (data.getMobKillCountContaining("whale") >= 10) count++;
        if (data.getMobKillCountContaining("shadow knight") >= 10) count++;
        if (data.getMobKillCountContaining("golem guardian void") >= 10) count++;
        return count >= 3;
    }
}
