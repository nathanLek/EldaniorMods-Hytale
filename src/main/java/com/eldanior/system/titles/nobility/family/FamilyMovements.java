package com.eldanior.system.titles.nobility.family;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Les Mouvements de famille (demande de l'utilisateur, 2026-11-12) : chaque famille a environ 12 competences propres, les
 * « Mouvements », enseignees par des manuels de famille (Eldanior_Mouvement_<Famille>_<NN>, modele stylise par famille,
 * scripts/familles/gen_voie_martiale_objets.py). Chaque Mouvement est une technique evolutive selon le rang martial.
 *
 *  - en rejoignant la famille, le membre recoit le manuel du 1er Mouvement (locks.FamilyKeySystem, une seule fois :
 *    FamilyRegistry.welcomed) ;
 *  - les suivants s'apprennent aupres du Sensei de la famille, qui ne montre que ceux que le rang martial du joueur permet
 *    (guards.Sensei) ; hors de la voie martiale, on compte comme Apprenti ;
 *  - un Mouvement sans technique (rune null) a deja son manuel : la lecture dit « technique a venir », le manuel reste.
 *
 * Rangs du Clan Luo (choix de l'utilisateur, « gros mouvements en fin de voie ») : Apprenti 1, Disciple 2, Expert 3, Maitre 4,
 * Grand Maitre 5, Saint 6-7, Maitre Supreme 8-10, Dieu Martial 11-12.
 * A garder en accord avec MOVEMENTS dans gen_voie_martiale_objets.py (identifiants, rangs, runes).
 */
public final class FamilyMovements {

    private FamilyMovements() {}

    public static final String ITEM_PREFIX = "Eldanior_Mouvement_";

    /**
     * Un Mouvement : famille, numero (1 a 12), rang martial requis (1 Apprenti a 8 Dieu Martial), rune enseignee (null : technique
     * a venir), nom (null : pas encore nomme).
     */
    public record Movement(String familyId, int number, int tier, String rune, String name) {
        /** Identifiant du manuel : Eldanior_Mouvement_Luo_01. */
        public String item() {
            return ITEM_PREFIX + Character.toUpperCase(familyId.charAt(0)) + familyId.substring(1) + "_" + (number < 10 ? "0" : "") + number;
        }

        public boolean ready() { return rune != null; }

        /** « 1er Mouvement : Exécution Céleste », « 4e Mouvement ». */
        public String title() {
            return ordinal(number) + " Mouvement" + (name != null ? " : " + name : "");
        }
    }

    public static String ordinal(int n) { return n == 1 ? "1er" : n + "e"; }

    private static final int[] LUO_TIERS = {1, 2, 3, 4, 5, 6, 6, 7, 7, 7, 8, 8};

    private static final Map<String, List<Movement>> BY_FAMILY = Map.of("luo", luo());

    private static List<Movement> luo() {
        List<Movement> out = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String rune = null, name = null;
            if (i == 1) { rune = "Rune_ExecutionCeleste"; name = "Exécution Céleste"; }   // geste a refaire (retour du 2026-11-12)
            out.add(new Movement("luo", i, LUO_TIERS[i - 1], rune, name));
        }
        return List.copyOf(out);
    }

    /** Mouvements d'une famille, dans l'ordre (liste vide si la famille n'en a pas encore). */
    public static List<Movement> of(String familyId) {
        return familyId == null ? List.of() : BY_FAMILY.getOrDefault(FamilyManager.migrateId(familyId).toLowerCase(), List.of());
    }

    /** Mouvement dont c'est le manuel, sinon null. */
    public static Movement ofItem(String itemId) {
        if (itemId == null || !itemId.startsWith(ITEM_PREFIX)) return null;
        for (List<Movement> l : BY_FAMILY.values())
            for (Movement m : l) if (m.item().equals(itemId)) return m;
        return null;
    }

    /** Mouvement qui enseigne cette rune, sinon null. */
    public static Movement ofRune(String runeId) {
        if (runeId == null) return null;
        for (List<Movement> l : BY_FAMILY.values())
            for (Movement m : l) if (runeId.equals(m.rune())) return m;
        return null;
    }

    /** Premier Mouvement de la famille (remis a l'entree), sinon null. */
    public static Movement first(String familyId) {
        List<Movement> l = of(familyId);
        return l.isEmpty() ? null : l.get(0);
    }
}
