package com.eldanior.system.classes.definitions.luo;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.ArrayList;
import java.util.List;

/**
 * Voie martiale (enseignee par le dojo du Clan Luo, demandee le 2026-11-08, classes et competences validees le 2026-11-09,
 * docs/plan-clan-luo.md) : une chaine de classes OUVERTE A TOUS (on peut etre artiste martial sans etre du clan) :
 *   Apprenti en Arts Martiaux < Disciple Martial (100) < Expert Martial (180) < Maitre Martial (280)
 *   < Grand Maitre Martial (400) < Saint Martial (550) < Maitre Supreme (750) < Dieu Martial (999).
 * On y entre seulement par le Rouleau de la Voie Martiale (luo.VoieMartialePage, retour de test du 2026-11-09) : le
 * personnage perd tout sauf 5 % de ses caracteristiques et recommence Apprenti au niveau 1. Aucune autre classe ne mene a la voie ; elle n'est jamais proposee
 * comme classe de depart (ClassManager.isEvolution). Chaque palier n'a qu'une suite.
 *
 * Aucun passif emprunte : les techniques du Clan Luo (runes du type Luo) s'apprennent dans leurs manuels secrets
 * (Eldanior_Manuel_Luo_*, spells.LuoManuels), remis au serment puis a chaque promotion. Ki de l'Epee et Meditation
 * martiale evoluent avec le palier (tier), plus une competence nouvelle par palier a partir du Disciple ; les precedentes
 * restent. Competences de palier (validees le 2026-11-10) : Renforcement corporel, Pas leger, Sens martial, Projection d'epee
 * d'Aura, Vol sur epee de Ki, Incarnation de l'Ame, Domaine Divin. Les 8 anciennes runes de palier (Paume de Ki ... Unite du
 * Ciel et de la Terre) sont devenues les techniques des clans de l'Empire Luo (luo.LuoClans.TECHNIQUES).
 */
public final class VoieMartiale {

    private VoieMartiale() {}

    public static final String FAMILY = "luo";
    public static final String FIRST = "apprenti_arts_martiaux";
    /** Dieu Martial : hors des evolutions depuis le 2026-11-18, attribue par commande (classe du Patriarche Luo). */
    public static final String GOD = "dieu_martial";
    public static final String KI = "Rune_KiEpee", MEDITATION = "Rune_MeditationMartiale";

    /** id, nom, rang, rarete, niveau de la promotion suivante, bonus FOR VIT INT END AGI CHA, rune du palier, description. */
    private record Step(String id, String name, String rank, Rarity rarity, int promotion, int[] stats, String rune, String text) {}

    private static final List<Step> STEPS = List.of(
            new Step(FIRST, "Apprenti en Arts Martiaux", "Troisième Ordre", Rarity.RARE, 100, new int[]{22, 16, 4, 14, 20, 6},
                    null, "Admis au dojo du Clan Luo, l'apprenti apprend les bases du combat, de la méditation et du Ki."),
            new Step("disciple_martial", "Disciple Martial", "Deuxième Ordre", Rarity.RARE, 180, new int[]{30, 20, 5, 18, 28, 8},
                    "Rune_RenforcementCorps", "Le disciple commence à maîtriser la circulation du Ki dans ses méridiens."),
            new Step("expert_martial", "Expert Martial", "Premier Ordre", Rarity.EPIC, 280, new int[]{38, 24, 6, 22, 36, 10},
                    "Rune_PasLeger", "L'expert maîtrise son arme et peut y insuffler son Ki."),
            new Step("maitre_martial", "Maître Martial", "Zénith", Rarity.EPIC, 400, new int[]{46, 28, 8, 26, 44, 12},
                    "Rune_SensMartial", "Le maître atteint le sommet des arts martiaux ordinaires et peut projeter son énergie."),
            new Step("grand_maitre_martial", "Grand Maître Martial", "Sommet Suprême", Rarity.UNIQUE, 550, new int[]{56, 34, 10, 30, 54, 14},
                    "Rune_ProjectionEpeeAura", "Le grand maître maîtrise la force de son arme et domine les autres maîtres."),
            new Step("saint_martial", "Saint Martial", "Transcendance", Rarity.UNIQUE, 750, new int[]{66, 40, 12, 36, 64, 16},
                    "Rune_VolEpeeKi", "Le saint dépasse les limites humaines et atteint une maîtrise absolue du Ki."),
            new Step("maitre_supreme", "Maître Suprême", "Hwakyung", Rarity.LEGENDARY, 999, new int[]{78, 46, 14, 42, 76, 18},
                    "Rune_IncarnationAme", "Le maître suprême subit une transformation physique et spirituelle : son Ki devient une énergie supérieure."),
            new Step("dieu_martial", "Dieu Martial", "Unité Naturelle", Rarity.LEGENDARY, 9999, new int[]{92, 54, 16, 50, 90, 22},
                    "Rune_DomaineDivin", "Le Dieu Martial atteint l'unité parfaite avec la nature et transcende les limites de l'humanité."));

    private static final class Tier extends ClassModel {
        Tier(Step s, String next, List<String> runes) {
            super(s.id(), s.name(), s.rank() + ". " + s.text(), s.rarity(), ClassType.WARRIOR, List.of(),
                    List.of(WeaponMastery.SWORD, WeaponMastery.DAGGER),
                    next != null ? List.of(next) : List.of(), s.promotion(), false,
                    s.stats()[0], s.stats()[1], s.stats()[2], s.stats()[3], s.stats()[4], s.stats()[5]);
            runes(runes.toArray(String[]::new));
        }
    }

    /**
     * Les 8 classes de la voie, chacune menant a la suivante. Runes : celle du palier d'abord (la carte de classe en montre 3),
     * puis Ki et Meditation, puis celles des paliers precedents (une classe permet toujours ses propres runes).
     */
    public static List<ClassModel> classes() {
        List<ClassModel> out = new ArrayList<>();
        for (int i = 0; i < STEPS.size(); i++) {
            List<String> runes = new ArrayList<>();
            if (STEPS.get(i).rune() != null) runes.add(STEPS.get(i).rune());
            runes.add(KI);
            runes.add(MEDITATION);
            for (int j = i - 1; j >= 0; j--) if (STEPS.get(j).rune() != null) runes.add(STEPS.get(j).rune());
            // 2026-11-18 : le Maitre Supreme ne monte plus au Dieu Martial (plafond des joueurs et des PNJ) ; Dieu Martial reste une
            // classe a part, celle du Patriarche Luo, donnee par commande comme Dragon (/es setclass ... dieu_martial)
            String next = i + 1 < STEPS.size() && !GOD.equals(STEPS.get(i + 1).id()) ? STEPS.get(i + 1).id() : null;
            out.add(new Tier(STEPS.get(i), next, runes));
        }
        return out;
    }

    /** Palier de la voie martiale (1 Apprenti ... 8 Dieu Martial), 0 hors de la voie (Guerrier compris). */
    public static int tier(String classId) {
        for (int i = 0; i < STEPS.size(); i++) if (STEPS.get(i).id().equals(classId)) return i + 1;
        return 0;
    }

    public static int tier(PlayerLevelData data) { return data == null ? 0 : tier(data.getPlayerClassId()); }

    /** Nom du rang d'un palier (Troisieme Ordre ... Unite Naturelle). */
    public static String rank(int tier) { return tier >= 1 && tier <= STEPS.size() ? STEPS.get(tier - 1).rank() : ""; }

    public static boolean isMartial(String classId) { return tier(classId) > 0; }

    /** Identifiants des 8 classes de la voie (quete de classe des artistes martiaux : ChapterQuests.senseiEpreuves). */
    public static String[] classIds() { return STEPS.stream().map(s -> s.id()).toArray(String[]::new); }

    private static boolean member(PlayerLevelData data) {
        return data != null && FAMILY.equals(data.getNobleFamilyId());
    }

    /** true pour un membre du Clan Luo (seuls eux peuvent preter serment). */
    public static boolean isMember(PlayerLevelData data) { return member(data); }

    /** Nom de la classe d'un palier (Apprenti en Arts Martiaux ... Dieu Martial). */
    public static String className(int tier) { return tier >= 1 && tier <= STEPS.size() ? STEPS.get(tier - 1).name() : ""; }

    /** Rune enseignee au palier (1 a 8), sinon null. */
    public static String tierRune(int tier) { return tier >= 1 && tier <= STEPS.size() ? STEPS.get(tier - 1).rune() : null; }

    /** Palier auquel une technique s'apprend : 1 pour le Ki et la Meditation, le palier de sa classe sinon, 0 hors voie. */
    public static int runeTier(String runeId) {
        if (KI.equals(runeId) || MEDITATION.equals(runeId)) return 1;
        for (int i = 0; i < STEPS.size(); i++) if (runeId != null && runeId.equals(STEPS.get(i).rune())) return i + 1;
        return 0;
    }

    /**
     * Evolutions tirables : la voie martiale n'apparait que dans les suites de ses propres classes (on y entre par le Rouleau),
     * donc rien a filtrer ; garde pour les ecrans d'evolution.
     */
    public static List<String> filter(List<String> pool, PlayerLevelData data) { return pool; }

    /** Choix proposes : inchanges (la voie martiale n'est plus liee au Clan Luo, retour de test du 2026-11-09). */
    public static List<String> ensure(List<String> proposed, List<String> pool, PlayerLevelData data) { return proposed; }
}
