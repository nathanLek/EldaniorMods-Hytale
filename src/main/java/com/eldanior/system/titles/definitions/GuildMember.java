package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class GuildMember extends TitleModel {
    public GuildMember() { super("guild_member", "Membre de Guilde", "Rejoindre une guilde.", Rarity.COMMON, TitleCategory.GUILDE, TitleBonus.NONE, List.of()); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return !data.getGuildId().isEmpty(); }
}
