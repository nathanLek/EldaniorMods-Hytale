package com.eldanior.system.quest.chapitres;

import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.chapitres.ChapterQuests.Art;
import com.eldanior.system.quest.chapitres.ChapterQuests.Chain;
import com.eldanior.system.quest.chapitres.ChapterQuests.Kind;
import com.eldanior.system.quest.chapitres.ChapterQuests.MissionType;
import com.eldanior.system.quest.chapitres.ChapterQuests.Objective;
import com.eldanior.system.quest.chapitres.ChapterQuests.Reward;

import java.util.List;

/**
 * Quete du Roi, « L'Epopee d'un Roi » (2026-11-14, concue avec l'utilisateur), etape 1 et ses trois temoins.
 *
 * Etape 1 « Les Rumeurs du Roi Fou » (principale, niveau 250) : un civil raconte les rumeurs et demande des preuves, les trois
 * indices. Il a entendu parler d'un homme qui vit loin (> 5 000 blocs) : le Vieux Garde. Chaque temoin donne une quete
 * EXCLUSIVE (debloquee seulement si la precedente est commencee ou rendue : Chain.requires) qui rapporte un indice, puis dit
 * ou trouver le suivant ; le troisieme renvoie au premier civil.
 *
 * Toutes ces quetes sont dans la bibliotheque : un admin les ajoute aux civils qu'il a poses (carte « + »), les temoins a plus
 * de 5 000 blocs les uns des autres (reponse A de l'utilisateur). Les textes disent pres de quel lieu vit le suivant
 * ({lieu:<chaine>}, ChapterQuests.withPlaces), sans distance ni direction.
 * Rangs : niveau 250 = rang B pour l'utilisateur, d'ou B et C (pas D).
 */
final class QueteRoi {

    private QueteRoi() {}

    static final String MAIN = "principale_epopee", GARDE = "temoin_garde", SERVANTE = "temoin_servante", ERMITE = "temoin_ermite";
    static final int LEVEL = 250;

    static final String INDICE_GARDE = "Eldanior_Indice_Journal_Garde", INDICE_REINE = "Eldanior_Indice_Lettre_Reine",
            INDICE_GRIMOIRE = "Eldanior_Indice_Page_Grimoire";

    static List<Chain> all() {
        return List.of(rumeurs(), garde(), servante(), ermite());
    }

    private static Chain rumeurs() {
        return new Chain(MAIN, ChapterQuests.LIBRARY, "L'Épopée d'un Roi", "Eldanior_Histoire_Roi_Fou", MissionType.PRINCIPALE)
                .minLevel(LEVEL)
                .act("Les Rumeurs du Roi Fou", Art.RUMEURS, QuestDifficulty.B, 0,
                        "« Tu as entendu parler du Roi Fou, toi aussi ? Ici, on en parle à voix basse. Il paraît qu'il a voulu "
                                + "la force des dragons, et qu'il l'a eue. Après, plus personne ne l'a reconnu. Le trône est vide "
                                + "depuis, mais je ne crois pas aux histoires qu'on raconte au coin du feu. Je veux des preuves. "
                                + "On dit qu'un vieil homme, un ancien garde du palais, vit près de {lieu:" + GARDE + "}. Il sait "
                                + "des choses, lui. Va le trouver, et rapporte-moi ce qu'il te confiera. »",
                        List.of("« Le vieux garde se méfie des curieux. Rends-lui service, il parlera. »",
                                "« Trois preuves, pas une de moins. Après, je te dirai ce que j'en pense. »"),
                        List.of(new Objective(Kind.ITEM, INDICE_GARDE, 1, "Rapporter le journal du garde"),
                                new Objective(Kind.ITEM, INDICE_REINE, 1, "Rapporter la lettre de la reine"),
                                new Objective(Kind.ITEM, INDICE_GRIMOIRE, 1, "Rapporter la page du grimoire")),
                        List.of(Reward.xp(6000), Reward.money(5000)));
    }

    /** Temoin 1 : un probleme a regler (hameau envahi par les morts), a la mesure d'un niveau 250. */
    private static Chain garde() {
        return new Chain(GARDE, ChapterQuests.LIBRARY, "Le Vieux Garde", INDICE_GARDE, MissionType.EXCLUSIVE)
                .minLevel(LEVEL).requires(MAIN)
                .act("Le Vieux Garde", Art.HAMEAU, QuestDifficulty.C, 0,
                        "« Le Roi Fou ? Qui t'envoie ? … Bon. J'ai servi dans sa garde, oui, et j'ai tout noté, chaque nuit, "
                                + "dans un journal. Mais je ne le donne pas au premier venu. Mon hameau a été pris par les morts, "
                                + "le mois dernier : des zombies, des dizaines, qui sortent des tombes comme s'ils attendaient "
                                + "quelqu'un. Chasse-les, rapporte-moi de quoi prouver que c'est fini, et le journal est à toi. »",
                        List.of("« Ils reviennent chaque nuit. Frappe fort, et ne reste pas seul quand le soleil se couche. »",
                                "« Après ça, va voir la servante de la reine, près de {lieu:" + SERVANTE + "}. Elle en sait plus que moi. »"),
                        List.of(new Objective(Kind.KILL, "zombie", 25, "Abattre 25 zombies"),
                                new Objective(Kind.ITEM, "Butin_Cerveau_Fletri", 3, "Rapporter 3 cerveaux flétris")),
                        List.of(Reward.item(INDICE_GARDE, 1, "Indice : le journal du garde"), Reward.xp(3000), Reward.money(2000)));
    }

    /** Temoin 2 : de la recolte (encre d'azur qui revele la lettre ecrite a l'encre invisible). */
    private static Chain servante() {
        return new Chain(SERVANTE, ChapterQuests.LIBRARY, "La Servante de la Reine", INDICE_REINE, MissionType.EXCLUSIVE)
                .minLevel(LEVEL).requires(GARDE)
                .act("La Servante de la Reine", Art.AZUR, QuestDifficulty.C, 0,
                        "« J'ai gardé la dernière lettre de ma reine pendant toutes ces années. Regarde : la page est blanche. "
                                + "Elle écrivait ses secrets à l'encre invisible, et seule une encre d'azur les fait réapparaître. "
                                + "Je sais la préparer, mais mes mains tremblent trop pour aller cueillir. Rapporte-moi des fruits "
                                + "d'azur, de la sève et du bois d'azur, et nous lirons ensemble ce qu'elle a voulu cacher. »",
                        List.of("« Les arbres d'azur poussent là où la forêt devient bleue. Leurs fruits brillent un peu la nuit. »",
                                "« Ensuite, l'ermite, près de {lieu:" + ERMITE + "}. Il gardait les livres du roi. »"),
                        List.of(new Objective(Kind.ITEM, "Plant_Fruit_Azure", 12, "Cueillir 12 fruits d'azur"),
                                new Objective(Kind.ITEM, "Ingredient_Tree_Sap", 8, "Récolter 8 sèves d'arbre"),
                                new Objective(Kind.ITEM, "Wood_Azure_Trunk", 20, "Couper 20 bûches d'azur")),
                        List.of(Reward.item(INDICE_REINE, 1, "Indice : la lettre de la reine"), Reward.xp(3000), Reward.money(2000)));
    }

    /** Temoin 3 : des minerais (cle forgee pour le coffret du grimoire). */
    private static Chain ermite() {
        return new Chain(ERMITE, ChapterQuests.LIBRARY, "L'Ermite des Livres", INDICE_GRIMOIRE, MissionType.EXCLUSIVE)
                .minLevel(LEVEL).requires(SERVANTE)
                .act("L'Ermite des Livres", Art.MINE, QuestDifficulty.B, 0,
                        "« Le grimoire du roi ? Je l'ai déchiré moi-même, la nuit où il est devenu fou, et j'ai enfermé la "
                                + "dernière page dans un coffret scellé. La clé a fondu dans l'incendie du palais. Pour en forger une "
                                + "nouvelle, il me faut du cobalt et du thorium, beaucoup, et je suis trop vieux pour descendre dans "
                                + "les mines. Apporte-les-moi, et tu liras ce que le roi a fait pour obtenir son pouvoir. »",
                        List.of("« Le cobalt se cache dans l'ardoise et le schiste, le thorium dans la boue et le grès. »",
                                "« Quand tu auras les trois preuves, retourne voir celui qui t'a envoyé, près de {lieu:" + MAIN + "}. »"),
                        List.of(new Objective(Kind.ITEM, "Ore_Cobalt", 15, "Rapporter 15 minerais de cobalt"),
                                new Objective(Kind.ITEM, "Ore_Thorium", 10, "Rapporter 10 minerais de thorium")),
                        List.of(Reward.item(INDICE_GRIMOIRE, 1, "Indice : la page du grimoire"), Reward.xp(6000), Reward.money(5000)));
    }
}
