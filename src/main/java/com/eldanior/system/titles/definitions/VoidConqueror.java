package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class VoidConqueror extends TitleModel {
    public VoidConqueror() { super("void_conqueror", "Conquerant du Vide", "Le neant n'a plus de secrets pour vous.", Rarity.LEGENDARY, TitleCategory.COMBAT, new TitleBonus(0, 0, 6, 0, 0, 6), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "void", 0.15))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("void") >= 500; }
}