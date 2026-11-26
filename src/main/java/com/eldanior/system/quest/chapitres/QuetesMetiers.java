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
 * Quetes des PNJ de metier (2026-11-14, concues avec l'utilisateur, skill quest-creation) : 5 secondaires et 3 exclusives.
 * Donneur = cle de ChapterQuests.chainsOf, choisie par NpcServices.chapterGiver : metier du commercant en minuscules
 * (« eleveur », « alchimiste », « forgeron »), « marchand_luo », « ambulant », « maitre_guilde », « forgeron_legendaire ».
 * Rangs et recompenses selon le tableau du skill (F 0-60 ... S 800+). Une illustration par etape (gen_chapitres_ui.py).
 */
final class QuetesMetiers {

    private QuetesMetiers() {}

    /** Secondaires refaisables apres 24 h ; 5 min tant que le test est actif (comme le Dresseur). */
    static final boolean SECONDAIRES_TEST = true;
    static final int DAY_MINUTES = 24 * 60;

    private static int repeat() { return SECONDAIRES_TEST ? ChapterQuests.REPEAT_TEST_MINUTES : DAY_MINUTES; }

    static List<Chain> all() {
        return List.of(crocsLisiere(), remedesMarais(), braisesForge(), mortsDesert(), tropheesCollectionneur(),
                droitEntree(), oeilVide(), acierOmbre());
    }

    // ==================== SECONDAIRES ====================

    private static Chain crocsLisiere() {
        return new Chain("crocs_lisiere", "eleveur", "Les Crocs de la Lisière", "Butin_Croc_Loup", MissionType.SECONDAIRE)
                .repeat(repeat())
                .act("Les Crocs de la Lisière", Art.LISIERE, QuestDifficulty.F, 0,
                        "« Trois agneaux en une semaine. Les loups descendent de la lisière dès que la brume se lève, et mes "
                                + "chiens n'osent plus sortir de la grange. Je ne te demande pas de vider la forêt : rapporte-moi "
                                + "leurs peaux et leurs crocs, je les clouerai sur la clôture. Ceux qui restent comprendront. »",
                        List.of("« Ils chassent en meute à la tombée du jour : ne te laisse pas encercler. »",
                                "« Le tanneur me rachètera les peaux. Toi, tu seras payé, et mes bêtes dormiront. »"),
                        List.of(new Objective(Kind.ITEM, "Butin_Peau_Loup", 8, "Rapporter 8 peaux de loup"),
                                new Objective(Kind.ITEM, "Butin_Croc_Loup", 4, "Rapporter 4 crocs de loup")),
                        List.of(Reward.xp(300), Reward.money(100)));
    }

    private static Chain remedesMarais() {
        return new Chain("remedes_marais", "alchimiste", "Remèdes du Marais", "Butin_Glande_Crapaud", MissionType.SECONDAIRE)
                .repeat(repeat())
                .act("Remèdes du Marais", Art.MARAIS, QuestDifficulty.E, 0,
                        "« Une fièvre court dans les fermes du sud. Je connais le remède, mais il me manque deux ingrédients "
                                + "qu'aucun marchand ne vend : la glande d'un crapaud rhino, qui fait tomber la fièvre, et la graine "
                                + "d'un ronceux, qui l'empêche de revenir. Les deux vivent dans les marais. Les deux mordent. »",
                        List.of("« Les crapauds rhinos chargent tête baissée : esquive, puis frappe. »",
                                "« Un ronceux ressemble à un buisson tant qu'il ne bouge pas. Méfie-toi des haies trop vertes. »"),
                        List.of(new Objective(Kind.ITEM, "Butin_Glande_Crapaud", 5, "Rapporter 5 glandes de crapaud"),
                                new Objective(Kind.ITEM, "Butin_Graine_Maudite", 3, "Rapporter 3 graines maudites")),
                        List.of(Reward.xp(800), Reward.money(300)));
    }

    private static Chain braisesForge() {
        return new Chain("braises_forge", "forgeron", "Braises pour la Forge", "Butin_Coeur_Braise", MissionType.SECONDAIRE)
                .repeat(repeat())
                .act("Braises pour la Forge", Art.FORGE, QuestDifficulty.D, 0,
                        "« Le charbon ne suffit plus. Pour tremper une lame qui ne casse pas, il faut un feu qui ne meurt pas. "
                                + "Les os des squelettes brûlés gardent la chaleur des siècles, et le cœur d'un emberwulf brûle "
                                + "encore longtemps après sa mort. Apporte-m'en, et ma forge chantera de nouveau. »",
                        List.of("« Les squelettes brûlés hantent les terres calcinées. Leurs coups enflamment : garde tes distances. »",
                                "« Un emberwulf ne se laisse pas approcher. Surprends-le, ou épuise-le. »"),
                        List.of(new Objective(Kind.ITEM, "Butin_Os_Calcine", 10, "Rapporter 10 os calcinés"),
                                new Objective(Kind.ITEM, "Butin_Coeur_Braise", 3, "Rapporter 3 cœurs de braise")),
                        List.of(Reward.xp(1500), Reward.money(800)));
    }

    private static Chain mortsDesert() {
        return new Chain("morts_desert", "marchand_luo", "Les Morts du Désert", "Butin_Bandage_Ensable", MissionType.SECONDAIRE)
                .repeat(repeat())
                .act("Les Morts du Désert", Art.DUNES, QuestDifficulty.C, 0,
                        "« Ma caravane devait traverser les dunes en trois jours. Elle n'est jamais arrivée. Les rares qui sont "
                                + "revenus parlent de morts qui se lèvent du sable à la nuit tombée, enveloppés de bandages, "
                                + "affamés. Tant qu'ils rôderont, aucune caravane ne repartira. Abats-en quinze, et la route "
                                + "s'ouvrira de nouveau. »",
                        List.of("« Ils sortent du sable quand le soleil se couche. »",
                                "« Ne compte pas sur leurs blessures pour les ralentir : ils ne sentent plus rien. »"),
                        List.of(new Objective(Kind.KILL, "zombie_sand", 15, "Abattre 15 zombies des sables")),
                        List.of(Reward.xp(3000), Reward.money(2000)));
    }

    private static Chain tropheesCollectionneur() {
        return new Chain("trophees_collectionneur", "ambulant", "Trophées pour un Collectionneur", "Butin_Trophee_Raid",
                MissionType.SECONDAIRE)
                .repeat(repeat())
                .act("Trophées pour un Collectionneur", Art.OUTLANDERS, QuestDifficulty.B, 0,
                        "« Un noble de la capitale m'a passé commande. Pour son cabinet de curiosités, il veut des trophées "
                                + "arrachés aux Outlanders, ces pillards venus du nord, et le talisman d'os d'un de leurs "
                                + "mystiques. Il paie bien, et je partage avec toi, à condition que tu ramènes la marchandise en "
                                + "un seul morceau. Toi aussi, d'ailleurs. »",
                        List.of("« Leurs mystiques restent en retrait et soignent les autres : abats-les en premier. »",
                                "« Le noble est pressé. Ne traîne pas sur les routes. »"),
                        List.of(new Objective(Kind.ITEM, "Butin_Trophee_Raid", 5, "Rapporter 5 trophées de raid"),
                                new Objective(Kind.ITEM, "Butin_Talisman_Os", 1, "Rapporter un talisman d'os")),
                        List.of(Reward.xp(6000), Reward.money(5000)));
    }

    // ==================== EXCLUSIVES (une seule fois par joueur) ====================

    private static Chain droitEntree() {
        return new Chain("droit_entree", "maitre_guilde", "Le Droit d'Entrée", "Eldanior_Weapon_Sword_Gardien", MissionType.EXCLUSIVE)
                .act("Le Droit d'Entrée", Art.ARENE, QuestDifficulty.D, 0,
                        "« Une guilde, ce n'est pas une taverne. Ceux qui portent notre écusson se battent pour lui, et contre "
                                + "d'autres aventuriers s'il le faut. Avant de t'ouvrir nos portes, je veux te voir gagner trois "
                                + "duels contre de vrais adversaires, des aventuriers comme toi, pas des mannequins de paille. "
                                + "Reviens avec trois victoires, et je te confierai une lame digne d'un membre. »",
                        List.of("« Un duel se propose et s'accepte : choisis tes adversaires, personne ne t'en voudra. »",
                                "« Les défaites ne comptent pas. Elles t'apprendront pourtant plus que les victoires. »"),
                        List.of(new Objective(Kind.DUEL_PLAYER, null, 3, "Gagner 3 duels contre des joueurs")),
                        List.of(Reward.item("Eldanior_Weapon_Sword_Gardien", 1, "Épée du Gardien (rare)"), Reward.xp(1500),
                                Reward.money(400)));
    }

    private static Chain oeilVide() {
        return new Chain("oeil_vide", "alchimiste", "L'Œil du Vide", "Butin_Pupille_Vide", MissionType.EXCLUSIVE)
                .act("L'Œil du Vide", Art.VIDE, QuestDifficulty.B, 0,
                        "« On dit qu'un œil arraché au Vide voit ce qui n'est pas encore arrivé. Je n'y crois qu'à moitié, mais "
                                + "la moitié suffit pour tenter l'expérience. Il me faut cinq pupilles d'Œil du Vide et deux "
                                + "prismes taillés dans les cristaux d'anomalie. Si l'élixir fonctionne, je t'en confierai le "
                                + "secret : un parchemin que je n'ai jamais montré à personne. »",
                        List.of("« Ne fixe pas un Œil du Vide trop longtemps. Ceux qui l'ont fait parlent encore de ce qu'ils y ont vu. »",
                                "« Les prismes se fendent au moindre faux coup. Achève les cristaux avec patience. »"),
                        List.of(new Objective(Kind.ITEM, "Butin_Pupille_Vide", 5, "Rapporter 5 pupilles du Vide"),
                                new Objective(Kind.ITEM, "Butin_Prisme_Instable", 2, "Rapporter 2 prismes instables")),
                        List.of(Reward.item("Eldanior_Manuel_Magie_Temps_Presage", 1, "Manuel du Temps : Présage (épique)"), Reward.xp(6000),
                                Reward.money(2500)));
    }

    private static Chain acierOmbre() {
        return new Chain("acier_ombre", "forgeron_legendaire", "L'Acier de l'Ombre", "Artefact_Sword_Croc_Du_Neant",
                MissionType.EXCLUSIVE)
                .act("L'Acier de l'Ombre", Art.OMBRE, QuestDifficulty.A, 0,
                        "« J'ai forgé pour des rois et pour des dragons. Une seule lame m'a toujours résisté : celle qui boit "
                                + "la force de ceux qu'elle frappe. Pour la tremper, il faut le noyau d'un golem de firesteel, le "
                                + "métal le plus chaud du monde, et le heaume du Chevalier de l'Ombre, le plus froid. "
                                + "Rapporte-les-moi, et le Croc du Néant sera à toi. Personne d'autre ne le portera. »",
                        List.of("« Les golems de firesteel gardent les forges anciennes ; leur noyau brûle encore quand ils tombent. »",
                                "« Le Chevalier de l'Ombre ne se montre qu'à ceux qui le cherchent. Ne le cherche pas seul. »"),
                        List.of(new Objective(Kind.ITEM, "Butin_Noyau_Firesteel", 3, "Rapporter 3 noyaux de firesteel"),
                                new Objective(Kind.ITEM, "Butin_Heaume_L_Ombre", 1, "Rapporter le heaume du Chevalier de l'Ombre")),
                        List.of(Reward.item("Artefact_Sword_Croc_Du_Neant", 1, "Croc du Néant (unique)"), Reward.xp(12000),
                                Reward.money(6000)));
    }
}
