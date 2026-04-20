package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class YetiConqueror extends TitleModel {
    public YetiConqueror() { super("yeti_conqueror", "Conquerant du Yeti", "Meme le froid eternel ne vous arrete pas.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 0, 0, 3, 0, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "yeti", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("yeti") >= 10; }
}