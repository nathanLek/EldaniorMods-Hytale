package com.eldanior.system.familiers;

/**
 * Competence d'un familier (2026-11-13, decision de l'utilisateur) : une bete n'en a aucune avant l'age adulte. Adulte, son
 * maitre la fait evoluer depuis la fenetre de gestion (bouton « Faire evoluer ») : une competence est tiree au hasard parmi
 * celles de son espece et celle de son element (Espece.skillPool : le type de l'animal et sa rarete).
 * L'identifiant (name) est celui de FamilierSpells.skill. Les competences d'element portent le nom de l'element.
 */
public enum Competence {
    // ----- Betes (toutes raretes) -----
    MORSURE("Morsure sauvage", "Une morsure profonde sur sa cible, qui l'affaiblit.", "#e8c8a0"),
    BOND("Bond du chasseur", "Il bondit dans le dos de sa proie et la lacère.", "#e8c8a0"),
    RUGISSEMENT("Rugissement", "Un cri qui ébranle et terrifie les monstres autour de lui.", "#e8c8a0"),
    COUP_DE_PATTE("Coup de patte", "Un revers puissant qui balaie les ennemis devant lui.", "#e8c8a0"),
    HURLEMENT_MEUTE("Hurlement de meute", "Un hurlement qui affaiblit les monstres proches.", "#e8c8a0"),
    PIQUE("Piqué", "Il fond du ciel dans le dos de sa proie, serres en avant.", "#e8c8a0"),
    CRI("Cri perçant", "Un cri strident qui terrifie les monstres autour de lui.", "#e8c8a0"),
    RUADE("Ruade", "Une ruade puissante qui projette les ennemis devant lui.", "#e8c8a0"),
    // ----- Fenrir -----
    GLACE("Hurlement glacial", "Un hurlement de givre : dégâts de glace et ralentissement autour de lui.", "#9fe8ff"),
    CROC("Croc du Ragnarök", "Une morsure géante qui gèle sa cible sur place.", "#9fe8ff"),
    // ----- Elements (betes rares et au-dela, Element) -----
    EAU("Vague déferlante", "Une vague qui trempe les monstres autour de lui.", "#5fb4ff"),
    FEU("Crocs de braise", "Une morsure brûlante qui embrase sa cible et ses voisins.", "#ff8a4a"),
    TERRE("Secousse", "Le sol tremble et enracine les monstres proches.", "#b08850"),
    AIR("Rafale", "Une bourrasque qui repousse les monstres autour de lui.", "#bfeee0"),
    ECLAIR("Foudre bondissante", "La foudre frappe sa cible et étourdit ses voisins.", "#ffe46a"),
    LUMIERE("Éclat sacré", "Une lumière aveuglante qui soigne aussi son maître.", "#fff2b0"),
    TENEBRES("Morsure d'ombre", "Une morsure qui corrompt sa cible et le soigne.", "#a07aff"),
    NATURE("Ronces", "Des ronces jaillissent et retiennent les monstres proches.", "#7fd36a"),
    SPATIAL("Bond spatial", "Il traverse l'espace jusque dans le dos de sa cible.", "#8ad7ff"),
    TEMPS("Instant figé", "Le temps se fige autour de lui.", "#ffd27a"),
    ESPACE("Singularité", "Il attire les monstres proches vers lui.", "#c08aff"),
    DRAGONIC("Souffle draconique", "Un souffle de flammes en cône devant lui.", "#ff5a3a");

    public final String label, description, color;

    Competence(String label, String description, String color) {
        this.label = label; this.description = description; this.color = color;
    }

    public static Competence of(String s) {
        if (s == null || s.isEmpty()) return null;
        for (Competence c : values()) if (c.name().equalsIgnoreCase(s)) return c;
        return null;
    }
}
