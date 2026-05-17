package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GuildLeader extends TitleModel {
    public GuildLeader() { super("guild_leader", "Chef de Guilde", "Devenir chef de guilde.", Rarity.EPIC, TitleCategory.GUILDE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return "CHEF".equalsIgnoreCase(data.getGuildRole()); }
}
