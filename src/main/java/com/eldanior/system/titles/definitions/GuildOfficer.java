package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GuildOfficer extends TitleModel {
    public GuildOfficer() { super("guild_officer", "Officier", "Devenir officier de guilde.", Rarity.RARE, TitleCategory.GUILDE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return "OFFICER".equalsIgnoreCase(data.getGuildRole()) || "LEADER".equalsIgnoreCase(data.getGuildRole()); }
}
