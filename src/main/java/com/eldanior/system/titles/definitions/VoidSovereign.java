package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class VoidSovereign extends TitleModel {
    public VoidSovereign() { super("void_sovereign", "Souverain du Neant", "Vous regnez sur le vide lui-meme.", Rarity.DIVINE, TitleCategory.COMBAT, new TitleBonus(0, 0, 10, 0, 0, 10), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB, "void", 0.25))); }
    @Override public boolean checkUnlockCondition(PlayerLevelData data) { return data.getMobKillCountContaining("void") >= 5000; }
}