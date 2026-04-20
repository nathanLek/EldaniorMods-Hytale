package com.eldanior.system.titles.definitions;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.TitleBonus;
import com.eldanior.system.titles.models.TitleModel;

import java.util.List;

public class NoviceTitle extends TitleModel {

    public NoviceTitle() {
        super(
                "novice",
                "Novice",
                "Le titre de depart de tout aventurier.",
                Rarity.COMMON,
                TitleCategory.QUEST,
                TitleBonus.NONE,
                List.of()
        );
    }
}