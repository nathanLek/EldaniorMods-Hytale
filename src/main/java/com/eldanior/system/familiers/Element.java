package com.eldanior.system.familiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Element d'un familier (decision de l'utilisateur, 2026-11-13) : il depend de la rarete de la bete.
 *   Commun, Inhabituel : aucun ; Rare : Eau, Feu, Terre, Air ; Epique : Eclair, Lumiere, Tenebres, Nature ;
 *   Legendaire : Spatial, Temps, Espace, Dragonic.
 * Tire au hasard dans la liste de sa rarete a l'eclosion, sauf pour les betes legendaires et divines : leur element est fixe
 * par l'espece (Espece.element ; Fenrir : Glace, element hors listes). Chaque element donne a la bete une competence du meme
 * type et de la meme rarete (FamilierSpells.elementSkill, meme identifiant que l'element).
 */
public enum Element {
    EAU("Eau", Rarete.RARE, "Vague déferlante", "#5fb4ff"),
    FEU("Feu", Rarete.RARE, "Crocs de braise", "#ff8a4a"),
    TERRE("Terre", Rarete.RARE, "Secousse", "#b08850"),
    AIR("Air", Rarete.RARE, "Rafale", "#bfeee0"),
    ECLAIR("Éclair", Rarete.EPIQUE, "Foudre bondissante", "#ffe46a"),
    LUMIERE("Lumière", Rarete.EPIQUE, "Éclat sacré", "#fff2b0"),
    TENEBRES("Ténèbres", Rarete.EPIQUE, "Morsure d'ombre", "#a07aff"),
    NATURE("Nature", Rarete.EPIQUE, "Ronces", "#7fd36a"),
    SPATIAL("Spatial", Rarete.LEGENDAIRE, "Bond spatial", "#8ad7ff"),
    TEMPS("Temps", Rarete.LEGENDAIRE, "Instant figé", "#ffd27a"),
    ESPACE("Espace", Rarete.LEGENDAIRE, "Singularité", "#c08aff"),
    DRAGONIC("Dragonic", Rarete.LEGENDAIRE, "Souffle draconique", "#ff5a3a"),
    /** Hors tirage : element propre du Fenrir (2026-11-13). Sa competence est le Hurlement glacial. */
    GLACE("Glace", null, "Hurlement glacial", "#9fe8ff");

    public final String label, skill, color;
    public final Rarete rarete;

    Element(String label, Rarete rarete, String skill, String color) {
        this.label = label; this.rarete = rarete; this.skill = skill; this.color = color;
    }

    /** Element tire au hasard parmi ceux de la rarete (null : Commun, Inhabituel). */
    public static Element roll(Rarete r, Random rng) {
        List<Element> pool = new ArrayList<>();
        for (Element e : values()) if (e.rarete == r) pool.add(e);
        return pool.isEmpty() ? null : pool.get(rng.nextInt(pool.size()));
    }

    public static Element of(String s) {
        if (s == null || s.isEmpty()) return null;
        for (Element e : values()) if (e.name().equalsIgnoreCase(s) || e.label.equalsIgnoreCase(s)) return e;
        return null;
    }

    /** Raretes des betes (ordre du mod : Commun < ... < Legendaire < Divin) et niveau maximal de chacune (2026-11-13). */
    public enum Rarete {
        COMMUN("Commun", "#c8c8c8", 150), INHABITUEL("Inhabituel", "#7fd36a", 150), RARE("Rare", "#5fa8ff", 350),
        EPIQUE("Épique", "#b07aff", 600), LEGENDAIRE("Légendaire", "#ffb030", 900), DIVIN("Divin", "#fff3a0", 999);

        public final String label, color;
        public final int maxLevel;

        Rarete(String label, String color, int maxLevel) { this.label = label; this.color = color; this.maxLevel = maxLevel; }

        /** Element fixe par l'espece (pas de tirage) : betes legendaires et divines. */
        public boolean fixedElement() { return this == LEGENDAIRE || this == DIVIN; }
    }
}
