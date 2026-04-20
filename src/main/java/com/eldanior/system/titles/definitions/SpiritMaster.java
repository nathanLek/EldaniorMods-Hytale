package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class SpiritMaster extends TitleModel {
    public SpiritMaster() { super("spirit_master", "Maitre des Esprits", "Les elements obeissent a votre volonte.", Rarity.EPIC, TitleCategory.COMBAT, new TitleBonus(0, 0, 5, 0, 0, 3), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "spirit", 0.10))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("spirit") >= 500; }
}