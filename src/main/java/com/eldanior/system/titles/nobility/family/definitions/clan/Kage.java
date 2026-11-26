package com.eldanior.system.titles.nobility.family.definitions.clan;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

/** Clan Kage : un des quatre grands clans de l'Empire Luo (famille sans territoire, rang de comte : elle peut posseder des villes). */
public class Kage extends NobleFamilyModel {

    public static final String ID = "kage";

    public Kage() {
        super(ID, "Kage", "Nous sommes l'ombre que l'Empereur projette.",
                "Les Kage sont les assassins de l'Empire Luo : infiltration, discretion et mise a mort sans laisser de trace. Leur fondatrice, Dame Nyx (que la couronne connaissait sous le nom de Nighthollow), fut sauvee par Luo Tenryu au pied de la Porte du Dragon et lui jura le sang de sa lignee. Leur Doyen siege au conseil en kimono noir ; leurs lames portent la tenue de l'Ombre.",
                Rarity.EPIC, NobilityRank.COMTE);
    }
}
