package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Immortal extends TitleModel {
    public Immortal() { super("immortal", "Immortel", "Vous avez atteint le level 100 sans mourir.", Rarity.DIVINE, TitleCategory.SPECIAL, new TitleBonus(0, 10, 0, 0, 10, 0), List.of(new TitleEffect(TitleEffect.TitleEffectType.DAMAGE_REDUCTION_FROM_MOB, "all", 0.05))); }
}