package com.eldanior.system.titles.definitions;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;
import java.util.List;
public class Untouchable extends TitleModel {
    public Untouchable() { super("untouchable", "Intouchable", "Vous avez atteint le level 50 sans mourir.", Rarity.EPIC, TitleCategory.SPECIAL, new TitleBonus(0, 5, 0, 0, 5, 0), List.of()); }
}