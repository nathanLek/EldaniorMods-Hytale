package com.eldanior.system.titles.nobility.family.definitions.clan;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

/** Clan Sama : un des quatre grands clans de l'Empire Luo (famille sans territoire, rang de comte : elle peut posseder des villes). */
public class Sama extends NobleFamilyModel {

    public static final String ID = "sama";

    public Sama() {
        super(ID, "Sama", "Le poing est la premiere lame ; le corps, le premier fourreau.",
                "Les Sama sont les pugilistes legendaires de l'Empire Luo. Leur fondateur, Sama Gōtetsu, aurait arrete a mains nues la lame d'un Runkandel. Ils forgent leur corps et leur Ki jusqu'a en faire des armes : on dit qu'un Sama endurci ne craint ni l'acier ni le feu. Leur Doyen siege au conseil de l'Empereur, en kimono cramoisi, et porte au combat le haori de la Garde d'Honneur.",
                Rarity.EPIC, NobilityRank.COMTE);
    }
}
