package com.eldanior.system.titles.nobility.family.definitions.clan;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

/** Clan Setsuka : un des quatre grands clans de l'Empire Luo (famille sans territoire, rang de comte : elle peut posseder des villes). */
public class Setsuka extends NobleFamilyModel {

    public static final String ID = "setsuka";

    public Setsuka() {
        super(ID, "Setsuka", "La lune ne frappe pas : elle repond.",
                "Les Setsuka sont les maitres de l'epee de l'Empire Luo : defense, contre-attaques et manipulation avancee du Ki. Ils s'entrainent la nuit, a la seule lueur de la lune, et laissent toujours l'adversaire frapper le premier. Leur Doyen siege au conseil en kimono blanc ; leurs epeistes portent le kimono blanc aux motifs de lune.",
                Rarity.EPIC, NobilityRank.COMTE);
    }
}
