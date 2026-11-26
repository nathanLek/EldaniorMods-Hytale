package com.eldanior.system.titles.nobility.family.definitions.marquis;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

/**
 * Clan Luo, ex-famille Luminara (identifiant "luminara" migre vers "luo" au chargement, voir FamilyManager.migrateId).
 * Marquisat du sud-ouest, aussi craint que la royaute. Territoire gere par l'admin.
 */
public class Luo extends NobleFamilyModel {

    public static final String ID = "luo";

    public Luo() {
        super(
                ID,
                "Luo",
                "Une lame, un serment, une lignee.",
                "Aux premiers ages, le Dieu-Dragon d'Or avala le soleil : ce fut la premiere eclipse. "
                + "Puis il prit forme humaine et marcha parmi les mortels. Son fils, Luo Tenryu, franchit les montagnes "
                + "du sud-ouest et fonda le clan dans la vallee des Mille Cerisiers. "
                + "Reclus derriere leurs cols, les Luo vivent pour trois choses : les arts martiaux, le sabre et l'or. "
                + "Aucun etranger n'entre dans la vallee ; seul le Comptoir de la Porte du Dragon, a son seuil, "
                + "les tolere le temps d'un marche. "
                + "Depuis que les Runkandel forgerent Ryukiri pour abattre le Dieu-Dragon, le sang coule entre les deux "
                + "maisons, et cette guerre ne connaitra pas de treve. "
                + "Simple marquisat sur les registres du royaume, le Clan Luo est craint autant que la couronne elle-meme.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS
        );
    }
}
