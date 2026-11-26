package com.eldanior.system.titles.nobility.family.definitions.clan;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

/** Clan Hanazakura : un des quatre grands clans de l'Empire Luo (famille sans territoire, rang de comte : elle peut posseder des villes). */
public class Hanazakura extends NobleFamilyModel {

    public static final String ID = "hanazakura";

    public Hanazakura() {
        super(ID, "Hanazakura", "Qu'un seul petale tombe, et la lame a deja frappe.",
                "Les Hanazakura sont les epeistes elegants de la vallee des Mille Cerisiers. Anciens gardiens des cols (les Swiftquiver de la couronne), ils ont pris le nom des fleurs qu'ils veillent. Leurs techniques florales, rapides et precises, imitent la chute des petales. Leur Doyen siege au conseil de l'Empereur en kimono violet ; leurs epeistes portent le kimono Sakura.",
                Rarity.EPIC, NobilityRank.COMTE);
    }
}
